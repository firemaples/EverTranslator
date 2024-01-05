package tw.firemaples.onscreenocr.floatings.compose.base

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.Gravity
import android.view.OrientationEventListener
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.PermissionUtil
import tw.firemaples.onscreenocr.utils.UIUtils
import tw.firemaples.onscreenocr.wigets.HomeButtonWatcher
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

abstract class ComposeFloatingView(protected val context: Context) {

    companion object {
        private val attachedFloatingViews: MutableList<ComposeFloatingView> = mutableListOf()

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

    private val viewModelStore = ViewModelStore()
    private val viewModelStoreOwner = object : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore
            get() = this@ComposeFloatingView.viewModelStore
    }

    @Composable
    abstract fun RootContent()

    //    abstract val layoutId: Int
//    protected lateinit var rootLayout: View
//    protected val rootView: BackButtonTrackerView by lazy {
//        BackButtonTrackerView(
//            context = context,
//            onAttachedToWindow = { onAttachedToScreen() },
//            onDetachedFromWindow = { onDetachedFromScreen() },
//            onBackButtonPressed = { onBackButtonPressed() },
//        ).apply {
//            rootLayout = ComposeView(context).apply {
//                setContent {
//                    RootContent()
//                }
//
//                setViewTreeLifecycleOwner(lifecycleOwner)
//                setViewTreeSavedStateRegistryOwner(lifecycleOwner)
//
//                setViewTreeViewModelStoreOwner(viewModelStoreOwner)
//            }
////            rootLayout = context.getThemedLayoutInflater().inflate(layoutId, null)
//            addView(
//                rootLayout,
//                ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT
//                )
//            )
//        }
//    }
    protected val rootView by lazy {
        ComposeView(context).apply {

            setContent {
                AppTheme {
                    logger.debug("is dark theme: ${isSystemInDarkTheme()}")
                    RootContent()
                }
            }

            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            setViewTreeViewModelStoreOwner(viewModelStoreOwner)
        }
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

        with(lifecycleOwner) {
            handleLifecycleEvent(Lifecycle.Event.ON_START)
            handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

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

        with(lifecycleOwner) {
            handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }

        attachedFloatingViews.remove(this)

        if (enableDeviceDirectionTracker)
            orientationEventListener.disable()

        attached = false
    }

    open fun release() {
        detachFromScreen()
        with(lifecycleOwner) {
            handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
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

    protected val lifecycleOwner: FloatingViewLifecycleOwner =
        FloatingViewLifecycleOwner().apply {
            performRestore(null)
            handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
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

    protected class FloatingViewLifecycleOwner : SavedStateRegistryOwner {
        private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
        private var savedStateRegistryController: SavedStateRegistryController =
            SavedStateRegistryController.create(this)

        val isInitialized: Boolean
            get() = true

        override val lifecycle: Lifecycle
            get() = lifecycleRegistry

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }

        override val savedStateRegistry: SavedStateRegistry
            get() = savedStateRegistryController.savedStateRegistry

        fun performRestore(savedState: Bundle?) {
            savedStateRegistryController.performRestore(savedState)
        }

        fun performSave(outBundle: Bundle) {
            savedStateRegistryController.performSave(outBundle)
        }
    }

    protected fun Int.fixXPosition(): Int =
        this.coerceAtLeast(0)
            .coerceAtMost(UIUtils.screenSize[0] - rootView.width)

    protected fun Int.fixYPosition(): Int =
        this.coerceAtLeast(0)
            .coerceAtMost(UIUtils.screenSize[1] - rootView.height)
}
