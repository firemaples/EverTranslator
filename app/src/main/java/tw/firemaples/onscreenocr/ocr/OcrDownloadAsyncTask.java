package tw.firemaples.onscreenocr.ocr;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firemaples on 2016/3/2.
 */
public class OcrDownloadAsyncTask extends AsyncTask<Void, Long, Boolean> {

    private final Context context;
    private final String recognitionLang;
    private final String recognitionLangName;

    private final File tessDataDir;

    private static final String URL_TRAINE_DATA_DOWNLOAD_TEMPLATES = "https://github.com/tesseract-ocr/tessdata/raw/master/%s.traineddata";

    private OnOcrDownloadAsyncTaskCallback callback;

    public OcrDownloadAsyncTask(Context context, OnOcrDownloadAsyncTaskCallback callback) {
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

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        callback.onDownloadStart();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (checkOcrFiles(recognitionLang)) {
            Tool.logInfo("OCR file found");
            return true;
        }

        if (!tessDataDir.exists() && !tessDataDir.mkdirs()) {
            Tool.logError("Make dir failed: " + tessDataDir.getPath());
            callback.onError("Make dir failed");
            return false;
        }

        File tessDataTempFile = new File(tessDataDir, recognitionLang + ".tmp");
        if (tessDataTempFile.exists()) {
            if (!tessDataTempFile.delete()) {
                Tool.logError("Delete temp file failed");
                callback.onError("Delete temp file failed");
                return false;
            }
        }

        File tessDataFile = new File(tessDataDir, recognitionLang + ".traineddata");
        if (!downloadTrainedata(recognitionLang, tessDataTempFile, tessDataFile)) {
            Tool.logError("Download OCR file failed");
            callback.onError("Download OCR file failed");
            return false;
        }

        return true;
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        super.onProgressUpdate(values);
        long currentFileLength = values[0];
        long totalFileLength = values[1];

        String msg = String.format(Locale.getDefault(),
                "Downloading data for Language %s (%.2fMB/%.2fMB)(%d%%)",
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
        if (result) {
            if (callback != null) {
                callback.onDownloadFinished();
            }
        }
    }

    private boolean downloadTrainedata(String languageCode, File tmpFile, File destFile) {
        String downloadUrl = String.format(Locale.getDefault(), URL_TRAINE_DATA_DOWNLOAD_TEMPLATES, languageCode);
        return this.downloadFile(downloadUrl, tmpFile, destFile);
    }

    private boolean downloadFile(String urlString, File tmpFile, File destFile) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Tool.logError("Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
                Tool.getInstance().showErrorMsg("Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
                return false;
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(tmpFile.getAbsolutePath());

            byte data[] = new byte[4096];
            long total = 0;
            int count;

            int loopTimes = 0;

            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    tmpFile.deleteOnExit();
                    return false;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0 && loopTimes++ % 25 == 0) // only if total length is known
                {
                    publishProgress(total, (long) fileLength);
                }
                output.write(data, 0, count);
            }

            if (!tmpFile.renameTo(destFile)) {
                Tool.logError("Move file failed");
                callback.onError("Move file failed");
                return false;
            }
        } catch (Exception e) {
            Tool.logError(e.toString());
            callback.onError(e.getLocalizedMessage());
            return false;
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException ignored) {
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
        return true;
    }

    public interface OnOcrDownloadAsyncTaskCallback {
        void onDownloadStart();

        void onDownloadFinished();

        void downloadProgressing(long currentDownloaded, long totalSize, String msg);

        void onError(String errorMessage);
    }
}
