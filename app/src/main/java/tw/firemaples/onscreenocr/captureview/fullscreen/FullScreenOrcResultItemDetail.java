package tw.firemaples.onscreenocr.captureview.fullscreen;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.orc.OrcResult;

/**
 * Created by firem_000 on 2016/3/8.
 */
public class FullScreenOrcResultItemDetail {
    private final View rootView;
    private ImageView iv_oriImageCrop;
    private EditText et_orcText, et_translatedText;
    private View bt_closeItemDetail, bt_oriTextVocal, bt_oriTextDict, bt_transTextVocal, bt_transTextDict;

    private FullScreenCaptureView fullScreenCaptureView;
    private OrcResult orcResult;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.bt_closeItemDetail) {
                hide();
            }
        }
    };

    public FullScreenOrcResultItemDetail(FullScreenCaptureView fullScreenCaptureView, View rootView) {
        this.fullScreenCaptureView = fullScreenCaptureView;
        this.rootView = rootView;
        initView();
    }

    private void initView() {
        iv_oriImageCrop = (ImageView) rootView.findViewById(R.id.iv_oriImageCrop);
        et_orcText = (EditText) rootView.findViewById(R.id.et_orcText);
        et_translatedText = (EditText) rootView.findViewById(R.id.et_translatedText);
        bt_closeItemDetail = rootView.findViewById(R.id.bt_closeItemDetail);
        bt_oriTextVocal = rootView.findViewById(R.id.bt_oriTextVocal);
        bt_oriTextDict = rootView.findViewById(R.id.bt_oriTextDict);
        bt_transTextVocal = rootView.findViewById(R.id.bt_transTextVocal);
        bt_transTextDict = rootView.findViewById(R.id.bt_transTextDict);

        bt_closeItemDetail.setOnClickListener(onClickListener);
        bt_oriTextVocal.setOnClickListener(onClickListener);
        bt_oriTextDict.setOnClickListener(onClickListener);
        bt_transTextVocal.setOnClickListener(onClickListener);
        bt_transTextDict.setOnClickListener(onClickListener);
    }

    public void setOrcResult(OrcResult orcResult) {
        this.orcResult = orcResult;

        et_orcText.setText(orcResult.getText());
        et_translatedText.setText(orcResult.getTranslatedText());
    }

    public void show() {
        rootView.setVisibility(View.VISIBLE);
    }

    public void hide() {
        rootView.setVisibility(View.GONE);
    }
}
