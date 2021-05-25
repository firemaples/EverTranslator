package tw.firemaples.onscreenocr.ui.permissions

import android.os.Bundle
import android.view.View
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.ui.BaseFragment
import kotlin.reflect.KClass

class PermissionCaptureScreenFragment : BaseFragment<PermissionCaptureScreenViewModel>() {

    override val layoutId: Int
        get() = R.layout.permission_capture_screen_fragment

    override val vmClass: KClass<PermissionCaptureScreenViewModel>
        get() = PermissionCaptureScreenViewModel::class

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}
