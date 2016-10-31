package tw.firemaples.onscreenocr.captureview.fullscreen;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.ocr.OcrResult;

/**
 * Created by firem_000 on 2016/3/8.
 */
public class OcrResultItemDetailDialog {
    private AlertDialog ad;
    private Context context;
    private View rootView;
    private ImageView iv_oriImageCrop;
    private EditText et_orcText, et_translatedText;
    private View bt_oriTextVocal, bt_oriTextDict, bt_transTextVocal, bt_transTextDict;

    private OcrResult ocrResult;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
        }
    };

    public OcrResultItemDetailDialog(Context context, OcrResult ocrResult) {
        this.context = context;
        AlertDialog.Builder ab = new AlertDialog.Builder(context,android.R.style.Theme_Material_Light_Dialog_Alert);
        rootView = View.inflate(context, R.layout.dialog_item_detail, null);
        initView();
        setOcrResult(ocrResult);
        ab.setView(rootView);
        ab.setPositiveButton(R.string.confirm,null);
        ab.setNegativeButton(R.string.cancel,null);
        ab.setCancelable(false);
        ad = ab.create();
        ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    }

    private void initView() {
        iv_oriImageCrop = (ImageView) rootView.findViewById(R.id.iv_oriImageCrop);
        et_orcText = (EditText) rootView.findViewById(R.id.et_orcText);
        et_translatedText = (EditText) rootView.findViewById(R.id.et_translatedText);
        bt_oriTextVocal = rootView.findViewById(R.id.bt_oriTextVocal);
        bt_oriTextDict = rootView.findViewById(R.id.bt_oriTextDict);
        bt_transTextVocal = rootView.findViewById(R.id.bt_transTextVocal);
        bt_transTextDict = rootView.findViewById(R.id.bt_transTextDict);

        bt_oriTextVocal.setOnClickListener(onClickListener);
        bt_oriTextDict.setOnClickListener(onClickListener);
        bt_transTextVocal.setOnClickListener(onClickListener);
        bt_transTextDict.setOnClickListener(onClickListener);
    }

    public OcrResultItemDetailDialog setOcrResult(OcrResult ocrResult) {
        this.ocrResult = ocrResult;

        et_orcText.setText(ocrResult.getText());
        et_translatedText.setText(ocrResult.getTranslatedText());
        return this;
    }

    public void show() {
        ad.show();
    }
}
