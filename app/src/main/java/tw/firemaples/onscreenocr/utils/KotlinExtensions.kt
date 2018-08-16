package tw.firemaples.onscreenocr.utils

import android.view.View
import android.widget.ListView
import tw.firemaples.onscreenocr.R

fun ListView.select(position: Int) {
//    if (this.isSkipNextSelect(true))
//        return
//    else
//        this.skipNextSelect()

    this.setItemChecked(position, true)
    this.setSelection(position)
}

fun View.skipNextSelect() {
    this.setTag(R.id.skipNextSelect, true)
}

fun View.isSkipNextSelect(clear: Boolean = false): Boolean {
    val result = this.getTag(R.id.skipNextSelect) == true
    if (clear) this.clearSkipNextSelect()

    return result
}

fun View.clearSkipNextSelect() = this.setTag(R.id.skipNextSelect, false)

fun View.setVisible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun Any?.equalsAny(vararg others: Any): Boolean =
        others.any { it == this@equalsAny }