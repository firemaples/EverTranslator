package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.SharePreferenceUtil;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firemaples on 04/11/2016.
 */

public class SettingView extends FloatingView {
    private Tool tool;
    private SharePreferenceUtil spUtil;
    private OcrNTranslateUtils ocrNTranslateUtils;
    private OnSettingChangedCallback callback;

    public SettingView(Context context, OnSettingChangedCallback callback) {
        super(context);
        this.callback = callback;
        tool = Tool.getInstance();
        spUtil = SharePreferenceUtil.getInstance();
        ocrNTranslateUtils = OcrNTranslateUtils.getInstance();
        setViews();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_setting;
    }

    @Override
    protected int getLayoutSize() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    private void setViews() {
        CheckBox cb_debugMode = (CheckBox) getRootView().findViewById(R.id.cb_debugMode);
        CheckBox cb_enableTranslation = (CheckBox) getRootView().findViewById(R.id.cb_enableTranslation);
        CheckBox cb_saveOcrEngineToExternalStorage = (CheckBox) getRootView().findViewById(R.id.cb_saveOcrEngineToExternalStorage);
        CheckBox cb_startingWithSelectionMode = (CheckBox) getRootView().findViewById(R.id.cb_startingWithSelectionMode);
        CheckBox cb_removeLineBreaks = (CheckBox) getRootView().findViewById(R.id.cb_removeLineBreaks);
        getRootView().findViewById(R.id.bt_close).setOnClickListener(onClickListener);

        cb_debugMode.setOnCheckedChangeListener(onCheckChangeListener);
        cb_enableTranslation.setOnCheckedChangeListener(onCheckChangeListener);
        cb_saveOcrEngineToExternalStorage.setOnCheckedChangeListener(onCheckChangeListener);
        cb_startingWithSelectionMode.setOnCheckedChangeListener(onCheckChangeListener);
        cb_removeLineBreaks.setOnCheckedChangeListener(onCheckChangeListener);

        cb_debugMode.setChecked(spUtil.isDebugMode());
        cb_enableTranslation.setChecked(spUtil.isEnableTranslation());
        cb_saveOcrEngineToExternalStorage.setChecked(ocrNTranslateUtils.getTessDataLocation() == OcrNTranslateUtils.TessDataLocation.EXTERNAL_STORAGE);
        cb_startingWithSelectionMode.setChecked(spUtil.startingWithSelectionMode());
        cb_removeLineBreaks.setChecked(spUtil.removeLineBreaks());

        if (!ocrNTranslateUtils.isExternalStorageWritable()) {
            cb_saveOcrEngineToExternalStorage.setEnabled(false);
        }
    }

    @Override
    public boolean onBackButtonPressed() {
        detachFromWindow();
        return true;
    }

    private CompoundButton.OnCheckedChangeListener onCheckChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            if (id == R.id.cb_debugMode) {
                spUtil.setDebugMode(isChecked);
            } else if (id == R.id.cb_enableTranslation) {
                spUtil.setEnableTranslation(isChecked);
                if (callback != null) {
                    callback.onEnableTranslationChanged(isChecked);
                }
            } else if (id == R.id.cb_startingWithSelectionMode) {
                spUtil.setStartingWithSelectionMode(isChecked);
            } else if (id == R.id.cb_removeLineBreaks) {
                spUtil.setRemoveLineBreaks(isChecked);
            } else if (id == R.id.cb_saveOcrEngineToExternalStorage) {
                OcrNTranslateUtils.TessDataLocation currentSelectedLocation = isChecked ? OcrNTranslateUtils.TessDataLocation.EXTERNAL_STORAGE : OcrNTranslateUtils.TessDataLocation.INTERNAL_STORAGE;
                OcrNTranslateUtils.TessDataLocation savedLocation = ocrNTranslateUtils.getTessDataLocation();
                if (currentSelectedLocation != savedLocation) {
                    if (savedLocation.getSaveDir().exists()) {
                        new MovingOcrEngineFileTask().execute(savedLocation.getSaveDir(), currentSelectedLocation.getSaveDir());
                        ocrNTranslateUtils.setTessDataLocation(currentSelectedLocation);
                    } else {
                        ocrNTranslateUtils.setTessDataLocation(currentSelectedLocation);
                    }
                }
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.bt_close) {
                SettingView.this.detachFromWindow();
            }
        }
    };

    private class MovingOcrEngineFileTask extends AsyncTask<File, String, Void> {
        private ProgressView progressView;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressView = new ProgressView(getContext());
            progressView.showMessage(getContext().getString(R.string.progress_startMovingOcrEngineFiles));
            progressView.attachToWindow();
        }

        @Override
        protected Void doInBackground(File... params) {
            File fileFrom = params[0];
            File fileTo = params[1];
            if (!fileTo.exists()) {
                fileTo.mkdirs();
            }
            for (File file : fileFrom.listFiles()) {
                try {
                    Tool.logInfo("Start move ocr file from:" + file.getAbsolutePath() + " to:" + fileTo.getAbsolutePath());
                    publishProgress(getContext().getString(R.string.progress_movingFile) + file.getName());
                    moveFile(file, fileTo);
                } catch (IOException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressView.showMessage(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressView.detachFromWindow();
            progressView = null;
        }

        private void moveFile(File file, File dir) throws IOException {
            File newFile = new File(dir, file.getName());
            if (newFile.getAbsolutePath().equals(file.getAbsolutePath())) {
                return;
            }
            FileChannel outputChannel = null;
            FileChannel inputChannel = null;
            try {
                outputChannel = new FileOutputStream(newFile).getChannel();
                inputChannel = new FileInputStream(file).getChannel();
                inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                inputChannel.close();
                file.delete();
            } finally {
                if (inputChannel != null) {
                    inputChannel.close();
                }
                if (outputChannel != null) {
                    outputChannel.close();
                }
            }
        }
    }

    public interface OnSettingChangedCallback {
        void onEnableTranslationChanged(boolean enableTranslation);
    }
}
