package tw.firemaples.onscreenocr.floatings.readme

import android.content.Context
import android.view.View
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.dialog.DialogView

class ReadmeView(context: Context) :
    DialogView(context) {

    companion object {
        var VERSION = "2.2.0"
    }

    init {
        setTitle("How to use")
        setDialogType(DialogType.CONFIRM_ONLY)
        setContentView(View.inflate(context, R.layout.view_help, null))
    }
}
