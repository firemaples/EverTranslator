package tw.firemaples.onscreenocr.floatings.base

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.Looper
import android.view.*
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlinx.coroutines.*
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.PermissionUtil
import tw.firemaples.onscreenocr.utils.UIUtils
import tw.firemaples.onscreenocr.wigets.BackButtonTrackerView
import tw.firemaples.onscreenocr.wigets.HomeButtonWatcher
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

abstract class FloatingView(protected val context: Context) {

    companion object {
        private val attachedFloatingViews: MutableList<FloatingView> = mutableListOf()

        fun detachAllFloatingViews() {
            attachedFloatingViews.toList().forEach { it.detachFromScreen() }
        }
    }

    private val logger: Logger by lazy { Logger(this::class) }

    private val windowManager: WindowManager by lazy { context.getSystemService(Context.WINDOW_SERVICE) as WindowManager }

    open val initialPosition: Point = Point(0, 0)
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
                val initPoint = initialPosition
                x = initPoint.x.fixXPosition()
                y = initPoint.y.fixYPosition()
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
    protected var rootLayout: View
    protected val rootView: BackButtonTrackerView =
        BackButtonTrackerView(
            context = context,
            onAttachedToWindow = { onAttachedToScreen() },
            onDetachedFromWindow = { onDetachedFromScreen() },
            onBackButtonPressed = { onBackButtonPressed() },
        ).apply {
            rootLayout = LayoutInflater.from(context).inflate(layoutId, null)
            addView(
                rootLayout,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

    private var lastScreenWidth: Int = -1
    open val enableDeviceDirectionTracker: Boolean = false
    private val orientationEventListener = object : OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {
            val screenWidth = UIUtils.screenSize[0]
            if (screenWidth != lastScreenWidth) {
                lastScreenWidth = screenWidth
                onDeviceDirectionChanged()
            }
        }
    }

    var attached: Boolean = false
        private set

    var onAttached: (() -> Unit)? = null
    var onDetached: (() -> Unit)? = null

    @MainThread
    open fun attachToScreen() {
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

        lifecycleOwner.onStateChanged(Lifecycle.State.RESUMED)

        attachedFloatingViews.add(this)

        if (enableDeviceDirectionTracker)
            orientationEventListener.enable()

        attached = true
    }

    @MainThread
    open fun detachFromScreen() {
        if (!attached) return
        if (Looper.myLooper() != Looper.getMainLooper()) {
            logger.warn("attachToWindow() should be called in main thread")
            return
        }

        if (enableHomeButtonWatcher) {
            homeButtonWatcher.stopWatch()
        }

        viewScope.coroutineContext.cancelChildren()

        windowManager.removeView(rootView)

        lifecycleOwner.onStateChanged(Lifecycle.State.CREATED)

        attachedFloatingViews.remove(this)

        if (enableDeviceDirectionTracker)
            orientationEventListener.disable()

        attached = false
    }

    open fun release() {
        detachFromScreen()
        lifecycleOwner.onStateChanged(Lifecycle.State.DESTROYED)
    }

    protected open fun onDeviceDirectionChanged() {
        params.x = params.x.fixXPosition()
        params.y = params.y.fixYPosition()
        updateViewLayout()
    }

    @CallSuper
    protected open fun onAttachedToScreen() {
        if (enableHomeButtonWatcher) {
            homeButtonWatcher.startWatch()
        }

        onAttached?.invoke()
    }

    @CallSuper
    protected open fun onDetachedFromScreen() {
        onDetached?.invoke()
    }

    fun changeViewPosition(x: Int, y: Int) {
        params.x = x
        params.y = y
        updateViewLayout()
    }

    private fun updateViewLayout() {
        try {
            windowManager.updateViewLayout(rootView, params)
        } catch (e: Exception) {
//            logger.warn(t = e)
        }
    }

    open fun onBackButtonPressed(): Boolean = false

    open fun onHomeButtonPressed() {

    }

    open fun onHomeButtonLongPressed() {

    }

    protected val lifecycleOwner: FloatingViewLifecycleOwner = FloatingViewLifecycleOwner()

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

    protected class FloatingViewLifecycleOwner : LifecycleOwner {
        private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

        override val lifecycle: Lifecycle
            get() = lifecycleRegistry
        fun onStateChanged(state: Lifecycle.State) {
            lifecycleRegistry.currentState = state
        }
    }

    protected fun Int.fixXPosition(): Int =
        this.coerceAtLeast(0)
            .coerceAtMost(UIUtils.screenSize[0] - rootView.width)

    protected fun Int.fixYPosition(): Int =
        this.coerceAtLeast(0)
            .coerceAtMost(UIUtils.screenSize[1] - rootView.height)
}
