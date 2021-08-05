package tw.firemaples.onscreenocr.floatings.main

import android.content.Context
import android.view.View
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.base.MovableFloatingView

class MainBar(context: Context) : MovableFloatingView(context) {
    override val layoutId: Int
        get() = R.layout.floating_main_bar

    private val viewTest = rootView.findViewById<View>(R.id.view_test)

    override val moveToEdgeAfterMoved: Boolean
        get() = true

    override val fadeOutAfterMoved: Boolean
        get() = true

    init {
        setDragView(viewTest)
    }
}
