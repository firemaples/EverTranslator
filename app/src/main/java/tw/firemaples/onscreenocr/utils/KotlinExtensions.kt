package tw.firemaples.onscreenocr.utils

import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import tw.firemaples.onscreenocr.CoreApplication
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

fun View.getView(id: Int): View = this.findViewById(id)
fun View.getTextView(id: Int): TextView = this.findViewById(id)
fun View.removeFromParent() = (this.parent as? ViewGroup)?.removeView(this)
fun View.onViewPrepared(callback: (View) -> Unit) {
    ViewPreparedWaiter().waitView(this, callback)
}

fun Int.asStringRes(): String = CoreApplication.instance.getString(this)