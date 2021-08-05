package tw.firemaples.onscreenocr.floatings.base

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.PermissionUtil
import tw.firemaples.onscreenocr.wigets.BackButtonTrackerView
import tw.firemaples.onscreenocr.wigets.HomeButtonWatcher
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

abstract class FloatingView(protected val context: Context) {

    private val logger: Logger by lazy { Logger(this::class) }

    private val windowManager: WindowManager by lazy { context.getSystemService(Context.WINDOW_SERVICE) as WindowManager }

    open val layoutWidth: Int = WindowManager.LayoutParams.WRAP_CONTENT
    open val layoutHeight: Int = WindowManager.LayoutParams.WRAP_CONTENT
    open val layoutFocusable: Boolean = false
    open val layoutCanMoveOutsideScreen: Boolean = false
    open val fullscreenMode: Boolean = false
    open val layoutGravity: Int = Gravity.TOP or Gravity.LEFT
    open val enableHomeButtonWatcher: Boolean = false

    protected val params: WindowManager.LayoutParams by lazy {
        val type =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE

        var flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        if (!layoutFocusable)
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        if (layoutCanMoveOutsideScreen)
            flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        if (fullscreenMode)
            flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

        WindowManager.LayoutParams(layoutWidth, layoutHeight, type, flags, PixelFormat.TRANSLUCENT)
            .apply {
                gravity = layoutGravity
            }
    }

    private val homeButtonWatcher: HomeButtonWatcher by lazy {
        HomeButtonWatcher(
            context = context,
            onHomeButtonPressed = { onHomeButtonPressed() },
            onHomeButtonLongPressed = { onHomeButtonLongPressed() },
        )
    }

    abstract val layoutId: Int
    protected val rootView: BackButtonTrackerView by lazy {
        BackButtonTrackerView(
            context = context,
            onAttachedToWindow = { onAttachedToScreen() }).apply {
            val innerView = LayoutInflater.from(context).inflate(layoutId, null)
            addView(
                innerView,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    private var attached: Boolean = false

    @MainThread
    fun attachToScreen() {
        if (attached) return
        if (!PermissionUtil.canDrawOverlays(context)) {
            logger.warn("You should obtain the draw overlays permission first!")
            return
        }
        if (Looper.myLooper() != Looper.getMainLooper()) {
            logger.warn("attachToWindow() should be called in main thread")
            return
        }

        windowManager.addView(rootView, params)
        attached = true
    }

    @MainThread
    fun detachFromScreen() {
        if (!attached) return
        if (Looper.myLooper() != Looper.getMainLooper()) {
            logger.warn("attachToWindow() should be called in main thread")
            return
        }

        if (enableHomeButtonWatcher) {
            homeButtonWatcher.stopWatch()
        }

        viewScope.cancel()

        windowManager.removeView(rootView)
        attached = false
    }

    @CallSuper
    open fun onAttachedToScreen() {
        if (enableHomeButtonWatcher) {
            homeButtonWatcher.startWatch()
        }
    }

    fun changeViewPosition(x: Int, y: Int) {
        params.x = x
        params.y = y
        updateViewLayout()
    }

    fun updateViewLayout() {
        try {
            windowManager.updateViewLayout(rootView, params)
        } catch (e: Exception) {
            logger.warn(t = e)
        }
    }

    open fun onHomeButtonPressed() {

    }

    open fun onHomeButtonLongPressed() {

    }

//    private val tasks = mutableListOf<WeakReference<Closeable>>()

    protected val viewScope: CoroutineScope by lazy {
        FloatingViewCoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).apply {
//            tasks.add(WeakReference(this))
        }
    }

    private class FloatingViewCoroutineScope(context: CoroutineContext) :
        Closeable, CoroutineScope {
        override val coroutineContext: CoroutineContext = context

        override fun close() {
            coroutineContext.cancel()
        }
    }
}
