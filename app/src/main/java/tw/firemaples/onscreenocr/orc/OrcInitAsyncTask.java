package tw.firemaples.onscreenocr.orc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.SettingsActivity;
import tw.firemaples.onscreenocr.captureview.CaptureView;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firemaples on 2016/3/2.
 */
public class OrcInitAsyncTask extends AsyncTask<Void, String, Boolean> {

    private final Context context;
    private final TessBaseAPI baseAPI;
    private final String recognitionLang;
    private final String recognitionLangName;
    private final CaptureView captureView;

    private final File tessRootDir;
    private final File tessDataDir;

    private static final String URL_TRAINE_DATA_DOWNLOAD_TEMPLATES = "https://github.com/tesseract-ocr/tessdata/raw/master/%s.traineddata";

    private int pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_AUTO_OSD;
    private OnOrcInitAsyncTaskCallback callback;

    public OrcInitAsyncTask(Context context, TessBaseAPI baseAPI, String recognitionLang, String recognitionLangName, CaptureView captureView) {
        this.context = context;
        this.baseAPI = baseAPI;
        this.recognitionLang = recognitionLang;
        this.recognitionLangName = recognitionLangName;
        this.captureView = captureView;

        this.tessRootDir = new File(context.getFilesDir() + File.separator + "tesseract");
        this.tessDataDir = new File(tessRootDir.getPath() + File.separator + "tessdata");
    }

    public OrcInitAsyncTask setCallback(OnOrcInitAsyncTaskCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onProgressUpdate("Orc engine initializing...");
        getPreferences();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (!tessDataDir.exists() && !tessDataDir.mkdirs()) {
            Tool.LogError("Make dir failed: " + tessDataDir.getPath());
            return false;
        }

        File tessDataFile = new File(tessDataDir, recognitionLang + ".traineddata");
        if (!tessDataFile.exists()) {
            if (!downloadTrainedata(recognitionLang, tessDataFile)) {
                Tool.LogError("Download TraneData Failed");
                return false;
            }
        }

        baseAPI.init(tessRootDir.getAbsolutePath(), recognitionLang, TessBaseAPI.OEM_TESSERACT_ONLY);
        baseAPI.setPageSegMode(pageSegmentationMode);

        return true;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        Tool.LogInfo(values[0]);
        captureView.setProgressMode(true, values[0]);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        captureView.setProgressMode(false, null);
        if (result) {
            if (callback != null)
                callback.onOrcInitialized();
        }
    }

    private boolean downloadTrainedata(String languageCode, File destFile) {
        String downloadUrl = String.format(Locale.getDefault(), URL_TRAINE_DATA_DOWNLOAD_TEMPLATES, languageCode);
        return this.downloadFile(downloadUrl, destFile);
    }

    private boolean downloadFile(String urlString, File destFile) {
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
                Tool.LogError("Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
                Tool.ShowErrorMsg(context, "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
                return false;
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(destFile.getAbsolutePath());

            byte data[] = new byte[4096];
            long total = 0;
            int count;

            int loopTimes = 0;
            float fileLengthMB = (float) fileLength / 1024 / 1024;

            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return false;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0 && loopTimes++ % 25 == 0) // only if total length is known
                    publishProgress(String.format(Locale.getDefault(), "Downloading data for Language %s (%.2fMB/%.2fMB)(%d%%)", recognitionLangName, (float) total / 1024 / 1024, fileLengthMB, (int) (total * 100 / fileLength)));
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            Tool.LogError(e.toString());
            Tool.ShowErrorMsg(context, e.toString());
            return false;
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return true;
    }

    private void getPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        // Retrieve from preferences, and set in this Activity, the page segmentation mode preference
        String[] pageSegmentationModes = context.getResources().getStringArray(R.array.pagesegmentationmodes);
        String pageSegmentationModeName = preferences.getString(SettingsActivity.KEY_PAGE_SEGMENTATION_MODE, pageSegmentationModes[0]);
        int searchIndex = Arrays.asList(pageSegmentationModes).indexOf(pageSegmentationModeName);
        switch (searchIndex) {
            case 0:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_AUTO_OSD;
                break;
            case 1:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_AUTO;
                break;
            case 2:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK;
                break;
            case 3:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR;
                break;
            case 4:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_COLUMN;
                break;
            case 5:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_LINE;
                break;
            case 6:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_WORD;
                break;
            case 7:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK_VERT_TEXT;
                break;
            case 8:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT;
                break;
        }
    }

    public interface OnOrcInitAsyncTaskCallback {
        void onOrcInitialized();
    }
}
