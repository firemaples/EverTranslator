package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;

import tw.firemaples.onscreenocr.R;

/**
 * Created by louis1chen on 26/03/2018.
 */

public class HelpView extends InfoDialogView {
    public static String VERSION = "2.1.0";

    public HelpView(Context context) {
        super(context);
    }

    @Override
    int getButtonMode() {
        return MODE_CLOSE;
    }

    @Override
    int getContentLayoutId() {
        return R.layout.content_help;
    }

    @Override
    String getTitle() {
        return getContext().getString(R.string.title_help);
    }


}
