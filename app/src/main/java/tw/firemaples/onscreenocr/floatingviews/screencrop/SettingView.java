package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import tw.firemaples.onscreenocr.BuildConfig;
import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;
import tw.firemaples.onscreenocr.ocr.tesseract.OCRFileUtil;
import tw.firemaples.onscreenocr.ocr.tesseract.TessDataLocation;
import tw.firemaples.onscreenocr.log.FirebaseEvent;
import tw.firemaples.onscreenocr.utils.SettingUtil;

/**
 * Created by firemaples on 04/11/2016.
 */

public class SettingView extends FloatingView {
    private static final Logger logger = LoggerFactory.getLogger(SettingView.class);

    private SettingUtil spUtil;
    private OCRFileUtil ocrNTranslateUtils = OCRFileUtil.INSTANCE;

    public SettingView(Context context) {
        super(context);
        spUtil = SettingUtil.INSTANCE;
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
        CheckBox cb_debugMode = getRootView().findViewById(R.id.cb_debugMode);
        CheckBox cb_saveOcrEngineToExternalStorage = getRootView().findViewById(R.id.cb_saveOcrEngineToExternalStorageFirst);
        CheckBox cb_startingWithSelectionMode = getRootView().findViewById(R.id.cb_startingWithSelectionMode);
        CheckBox cb_rememberLastSelection = getRootView().findViewById(R.id.cb_rememberLastSelection);
        CheckBox cb_removeLineBreaks = getRootView().findViewById(R.id.cb_removeLineBreaks);
        CheckBox cb_autoCopyOCRResult = getRootView().findViewById(R.id.cb_autoCopyOCRResult);
        CheckBox cb_autoCloseAppWhenSpenInserted = getRootView().findViewById(R.id.cb_autoCloseAppWhenSpenInserted);
        getRootView().findViewById(R.id.bt_close).setOnClickListener(onClickListener);

        cb_debugMode.setOnCheckedChangeListener(onCheckChangeListener);
        cb_saveOcrEngineToExternalStorage.setOnCheckedChangeListener(onCheckChangeListener);
        cb_startingWithSelectionMode.setOnCheckedChangeListener(onCheckChangeListener);
        cb_rememberLastSelection.setOnCheckedChangeListener(onCheckChangeListener);
        cb_removeLineBreaks.setOnCheckedChangeListener(onCheckChangeListener);
        cb_autoCopyOCRResult.setOnCheckedChangeListener(onCheckChangeListener);
        cb_autoCloseAppWhenSpenInserted.setOnCheckedChangeListener(onCheckChangeListener);

        cb_debugMode.setChecked(spUtil.isDebugMode());
        cb_saveOcrEngineToExternalStorage.setChecked(ocrNTranslateUtils.getTessDataLocation() == TessDataLocation.EXTERNAL_STORAGE);
        cb_startingWithSelectionMode.setChecked(spUtil.getStartingWithSelectionMode());
        cb_rememberLastSelection.setChecked(spUtil.isRememberLastSelection());
        cb_removeLineBreaks.setChecked(spUtil.getRemoveLineBreaks());
        cb_autoCopyOCRResult.setChecked(spUtil.getAutoCopyOCRResult());
        cb_autoCloseAppWhenSpenInserted.setChecked(spUtil.getAutoCloseAppWhenSpenInserted());

        if (!ocrNTranslateUtils.isExternalStorageWritable()) {
            cb_saveOcrEngineToExternalStorage.setEnabled(false);
        }

        if (!BuildConfig.DEBUG) {
            cb_debugMode.setVisibility(View.GONE);
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
            } else if (id == R.id.cb_startingWithSelectionMode) {
                spUtil.setStartingWithSelectionMode(isChecked);
            } else if (id == R.id.cb_removeLineBreaks) {
                spUtil.setRemoveLineBreaks(isChecked);
            } else if (id == R.id.cb_saveOcrEngineToExternalStorageFirst) {
                TessDataLocation currentSelectedLocation = isChecked ? TessDataLocation.EXTERNAL_STORAGE : TessDataLocation.INTERNAL_STORAGE;
                TessDataLocation savedLocation = ocrNTranslateUtils.getTessDataLocation();
                if (currentSelectedLocation != savedLocation) {
                    if (savedLocation.getSaveDir().exists()) {
                        new MovingOcrEngineFileTask().execute(savedLocation.getSaveDir(), currentSelectedLocation.getSaveDir());
                        ocrNTranslateUtils.setTessDataLocation(currentSelectedLocation);
                    } else {
                        ocrNTranslateUtils.setTessDataLocation(currentSelectedLocation);
                    }
                }
            } else if (id == R.id.cb_rememberLastSelection) {
                spUtil.setRememberLastSelection(isChecked);
            } else if (id == R.id.cb_autoCopyOCRResult) {
                spUtil.setAutoCopyOCRResult(isChecked);
            } else if (id == R.id.cb_autoCloseAppWhenSpenInserted) {
                spUtil.setAutoCloseAppWhenSpenInserted(isChecked);
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
            File[] fromFiles = fileFrom.listFiles();
            if (fromFiles != null) {
                for (File file : fromFiles) {
                    try {
                        logger.info("Start move ocr file from:" + file.getAbsolutePath() + " to:" + fileTo.getAbsolutePath());
                        publishProgress(getContext().getString(R.string.progress_movingFile) + file.getName());
                        moveFile(file, fileTo);
                    } catch (IOException e) {
                        e.printStackTrace();
                        FirebaseEvent.INSTANCE.logException(new Exception("Move OCR files from settings failed", e));
                    }

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
}
