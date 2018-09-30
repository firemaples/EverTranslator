package tw.firemaples.onscreenocr.floatingviews.screencrop

import android.content.Context
import tw.firemaples.onscreenocr.R

/**
 * Created by louis1chen on 26/03/2018.
 */

class HelpView(context: Context) : InfoDialogView(context) {

    companion object {
        var VERSION = "2.2.0"
    }

    internal override fun getContentLayoutId(): Int = R.layout.content_help

    internal override fun getTitle(): String = context.getString(R.string.title_help)

    internal override fun getButtonMode(): Int = InfoDialogView.MODE_CLOSE
}
