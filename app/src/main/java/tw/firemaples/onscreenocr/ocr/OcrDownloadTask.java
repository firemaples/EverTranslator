package tw.firemaples.onscreenocr.ocr;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import java.io.File;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.database.DatabaseManager;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firemaples on 2016/3/2.
 */
@SuppressWarnings("SpellCheckingInspection")
public class OcrDownloadTask {
    private final Context context;
    private final String recognitionLang;
    private final String recognitionLangName;

    private final File tessDataDir;

    private static final String URL_TRAINE_DATA_DOWNLOAD_TEMPLATES = "https://github.com/firemaples/tessdata/raw/master/%s.traineddata";

    private OnOcrDownloadAsyncTaskCallback callback;

    private OCRPrehandleAsyncTask ocrPrehandleAsyncTask;
    private AsyncHttpClient asyncHttpClient;
    private MoveFileAsyncTask moveFileAsyncTask;

    File tessDataTempFile;
    File tessDataFile;

    public OcrDownloadTask(@NonNull Context context, OnOcrDownloadAsyncTaskCallback callback) {
        this.context = context;
        this.callback = callback;

        OcrNTranslateUtils ocrNTranslateUtils = OcrNTranslateUtils.getInstance();
        this.recognitionLang = ocrNTranslateUtils.getOcrLang();
        this.recognitionLangName = ocrNTranslateUtils.getOcrLangDisplayName();

        this.tessDataDir = OcrNTranslateUtils.getInstance().getTessDataDir();
    }

    public static boolean checkOcrFiles(String recognitionLang) {
        File tessDataDir = OcrNTranslateUtils.getInstance().getTessDataDir();
        if (!tessDataDir.exists()) {
            Tool.logInfo("checkOcrFiles(): tess dir not found");
            return false;
        }

        File tessDataFile = new File(tessDataDir, recognitionLang + ".traineddata");
        if (!tessDataFile.exists()) {
            Tool.logInfo("checkOcrFiles(): target OCR file not found");
            return false;
        }

        return true;
    }

    public void startDownload() {
        cancel();

        ocrPrehandleAsyncTask = new OCRPrehandleAsyncTask();
        ocrPrehandleAsyncTask.execute();
    }

    public void cancel() {
        if (ocrPrehandleAsyncTask != null) {
            ocrPrehandleAsyncTask.cancel(true);
        }
        if (asyncHttpClient != null) {
            asyncHttpClient.cancelAllRequests(true);
        }
        if (moveFileAsyncTask != null) {
            moveFileAsyncTask.cancel(true);
        }
    }

    private void downloadTrainedata(String languageCode, File tmpFile, File destFile) {
        String urlFormat = DatabaseManager.getInstance().getTranslateServiceHolder().ocrDataUrl;
        if (urlFormat == null) {
            urlFormat = URL_TRAINE_DATA_DOWNLOAD_TEMPLATES;
        }
        String downloadUrl = String.format(Locale.getDefault(), urlFormat, languageCode);
//        return this.downloadFile(downloadUrl, tmpFile, destFile);
        asyncDownloadFile(downloadUrl, tmpFile, destFile);
    }

    private void asyncDownloadFile(String urlString, final File tmpFile, final File destFile) {
        if (asyncHttpClient == null) {
            asyncHttpClient = new AsyncHttpClient();
        }
        asyncHttpClient.get(context, urlString, new FileAsyncHttpResponseHandler(tmpFile) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                if (callback != null) {
                    throwable.printStackTrace();
                    callback.onError("Download OCR file failed: " + throwable.getMessage());
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                moveFileAsyncTask = new MoveFileAsyncTask(tmpFile, destFile);
                moveFileAsyncTask.execute();
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);

                String msg = String.format(Locale.getDefault(),
                        Tool.getContext().getString(R.string.dialog_content_progressingDownloadOCRFile),
                        recognitionLangName,
                        (float) bytesWritten / 1024f / 1024f,
                        (float) totalSize / 1024f / 1024f,
                        (int) (bytesWritten * 100 / totalSize));

                Tool.logInfo(msg);

                if (callback != null) {
                    callback.downloadProgressing(bytesWritten, totalSize, msg);
                }
            }
        });
    }

    public class OCRPrehandleAsyncTask extends AsyncTask<Void, Long, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            callback.onDownloadStart();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (checkOcrFiles(recognitionLang)) {
                Tool.logInfo("OCR file found");
                return null;
            }

            if (!tessDataDir.exists() && !tessDataDir.mkdirs()) {
                Tool.logError("Making folder failed: " + tessDataDir.getAbsolutePath());
                callback.onError(
                        String.format(
                                Locale.getDefault(),
                                Tool.getContext().getString(R.string.error_makingFolderFailed),
                                tessDataDir.getAbsolutePath()));
                return false;
            }

            tessDataTempFile = new File(tessDataDir, recognitionLang + ".tmp");
            if (tessDataTempFile.exists()) {
                if (!tessDataTempFile.delete()) {
                    Tool.logError("Delete temp file failed: " + tessDataTempFile.getAbsolutePath());
                    callback.onError(
                            String.format(
                                    Locale.getDefault(),
                                    Tool.getContext().getString(R.string.error_deleteTempFileFailed),
                                    tessDataTempFile.getAbsolutePath()));
                    return false;
                }
            }

            tessDataFile = new File(tessDataDir, recognitionLang + ".traineddata");

            return true;
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            long currentFileLength = values[0];
            long totalFileLength = values[1];

            String msg = String.format(Locale.getDefault(),
                    Tool.getContext().getString(R.string.dialog_content_progressingDownloadOCRFile),
                    recognitionLangName,
                    (float) currentFileLength / 1024f / 1024f,
                    (float) totalFileLength / 1024f / 1024f,
                    (int) (currentFileLength * 100 / totalFileLength));

            Tool.logInfo(msg);

            if (callback != null) {
                callback.downloadProgressing(currentFileLength, totalFileLength, msg);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result == null) {
                if (callback != null) {
                    callback.onDownloadFinished();
                }
            } else if (result) {
                downloadTrainedata(recognitionLang, tessDataTempFile, tessDataFile);
            }
        }
    }

    public class MoveFileAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private final File tmpFile;
        private final File destFile;

        public MoveFileAsyncTask(File tmpFile, File destFile) {
            this.tmpFile = tmpFile;
            this.destFile = destFile;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return tmpFile.renameTo(destFile);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result != null && result) {
                if (callback != null) {
                    callback.onDownloadFinished();
                }
            } else {
                Tool.logError("Move file failed");
                if (callback != null) {
                    callback.onError("Move file failed: from:" + tmpFile.getAbsolutePath() + " to:" + destFile.getAbsolutePath());
                }
            }
        }
    }

    public interface OnOcrDownloadAsyncTaskCallback {
        void onDownloadStart();

        void onDownloadFinished();

        void downloadProgressing(long currentDownloaded, long totalSize, String msg);

        void onError(String errorMessage);
    }
}
