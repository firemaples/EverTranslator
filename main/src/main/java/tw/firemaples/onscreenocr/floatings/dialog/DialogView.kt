package tw.firemaples.onscreenocr.floatings.dialog

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.base.FloatingView
import tw.firemaples.onscreenocr.utils.setTextOrGone
import tw.firemaples.onscreenocr.utils.showOrHide

open class DialogView(context: Context, override val layoutFocusable: Boolean = false) :
    FloatingView(context) {
    override val layoutId: Int
        get() = R.layout.floating_dialog
    override val layoutWidth: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT
    override val layoutHeight: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    private val viewRoot: View = rootView.findViewById(R.id.viewRoot)
    private val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
    private val tvMessage: TextView = rootView.findViewById(R.id.tv_message)
    private val viewContentViewWrapper: FrameLayout =
        rootView.findViewById(R.id.view_contentViewWrapper)
    private val btOk: View = rootView.findViewById(R.id.bt_ok)
    private val btCancel: View = rootView.findViewById(R.id.bt_cancel)

    var onButtonOkClicked: (() -> Unit)? = null
    var onButtonCancelClicked: (() -> Unit)? = null

    init {
        setViews()
    }

    private fun setViews() {
        btOk.setOnClickListener {
            detachFromScreen()
            onButtonOkClicked?.invoke()
        }
        btCancel.setOnClickListener {
            detachFromScreen()
            onButtonCancelClicked?.invoke()
        }
    }

    fun setTitle(title: String?) {
        tvTitle.setTextOrGone(title)
    }

    fun setMessage(message: String) {
        tvMessage.setTextOrGone(message)
    }

    fun setContentView(
        view: View,
        layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
    ) {
        tvMessage.visibility = View.GONE
        viewContentViewWrapper.removeAllViews()
        viewContentViewWrapper.addView(view, layoutParams)
        viewContentViewWrapper.visibility = View.VISIBLE
    }

    fun setDialogType(dialogType: DialogType) {
        btOk.showOrHide(dialogType == DialogType.CONFIRM_ONLY || dialogType == DialogType.CONFIRM_CANCEL)
        btCancel.showOrHide(dialogType == DialogType.CANCEL_ONLY || dialogType == DialogType.CONFIRM_CANCEL)
    }

    fun setCancelByClickingOutside(cancelByClickingOutside: Boolean) {
        if (cancelByClickingOutside) {
            rootView.setOnClickListener { detachFromScreen() }
        } else {
            rootView.setOnClickListener(null)
        }
    }

    enum class DialogType {
        CONFIRM_ONLY,
        CANCEL_ONLY,
        CONFIRM_CANCEL,
    }
}
