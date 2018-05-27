package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;

import tw.firemaples.onscreenocr.R;

/**
 * Created by louis1chen on 26/03/2018.
 */

public class HelpLiteView extends HelpView {
    public static String VERSION = "2.0.0";

    public HelpLiteView(Context context) {
        super(context);
    }

    @Override
    int getContentLayoutId() {
        return R.layout.content_help_lite;
    }

    @Override
    String getTitle() {
        return getContext().getString(R.string.title_help_lite);
    }
}
