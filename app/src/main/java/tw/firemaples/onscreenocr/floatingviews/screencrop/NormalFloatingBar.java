package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.utils.SharePreferenceUtil;

public class NormalFloatingBar extends FloatingBar {

    public NormalFloatingBar(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_floating_bar_normal;
    }

    @Override
    public void attachToWindow() {
        super.attachToWindow();

        if (!SharePreferenceUtil.getInstance().isHowToUseAlreadyShown()) {
            new HelpView(getContext()).attachToWindow();
        }
    }

    @Override
    protected void syncBtnState(BtnState btnState) {
        switch (btnState) {
            case Normal:
                bt_selectArea.setEnabled(true);
                bt_translation.setEnabled(false);
                bt_clear.setEnabled(false);
                break;
            case AreaSelecting:
                bt_selectArea.setEnabled(false);
                bt_translation.setEnabled(false);
                bt_clear.setEnabled(true);
                break;
            case AreaSelected:
                bt_selectArea.setEnabled(false);
                bt_translation.setEnabled(true);
                bt_clear.setEnabled(true);
                break;
            case Translating:
                bt_selectArea.setEnabled(false);
                bt_translation.setEnabled(false);
                bt_clear.setEnabled(true);
                break;
            case Translated:
                bt_selectArea.setEnabled(true);
                bt_translation.setEnabled(false);
                bt_clear.setEnabled(true);
                break;
        }
    }
}
