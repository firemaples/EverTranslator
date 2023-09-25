package tw.firemaples.onscreenocr.floatings.dialog

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import kotlinx.coroutines.suspendCancellableCoroutine
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.base.FloatingView
import tw.firemaples.onscreenocr.utils.clickOnce
import tw.firemaples.onscreenocr.utils.setTextOrGone
import tw.firemaples.onscreenocr.utils.showOrHide
import kotlin.coroutines.resume

open class DialogView(context: Context) :
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
    private val btOk: Button = rootView.findViewById(R.id.bt_ok)
    private val btCancel: Button = rootView.findViewById(R.id.bt_cancel)

    var onButtonOkClicked: (() -> Unit)? = null
    var onButtonCancelClicked: (() -> Unit)? = null

    init {
        setViews()
    }

    private fun setViews() {
        btOk.clickOnce {
            detachFromScreen()
            onButtonOkClicked?.invoke()
        }
        btCancel.clickOnce {
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

    fun setButtonOkText(text: String) {
        btOk.text = text
    }

    fun setButtonCancelText(text: String) {
        btCancel.text = text
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
            rootView.clickOnce {
                detachFromScreen()
                onButtonCancelClicked?.invoke()
            }
        } else {
            rootView.clickOnce {}
        }
    }

    enum class DialogType {
        CONFIRM_ONLY,
        CANCEL_ONLY,
        CONFIRM_CANCEL,
    }
}

suspend fun Context.showDialog(
    title: String? = null,
    message: String,
    dialogType: DialogView.DialogType,
    cancelByClickingOutside: Boolean = false,
): Boolean = suspendCancellableCoroutine { cont ->
    DialogView(this).apply {
        setTitle(title)
        setMessage(message)
        setDialogType(dialogType)
        setCancelByClickingOutside(cancelByClickingOutside)

        onButtonOkClicked = {
            cont.resume(true)
        }

        onButtonCancelClicked = {
            cont.resume(false)
        }

        cont.invokeOnCancellation { detachFromScreen() }

        attachToScreen()
    }
}

suspend fun Context.showErrorDialog(error: String): Boolean =
    suspendCancellableCoroutine { cont ->
        DialogView(this).apply {
            setTitle(getString(R.string.title_error))
            setMessage(error)
            setDialogType(DialogView.DialogType.CONFIRM_ONLY)
            setCancelByClickingOutside(false)

            onButtonOkClicked = { cont.resume(false) }

            cont.invokeOnCancellation { detachFromScreen() }
        }.attachToScreen()
    }
