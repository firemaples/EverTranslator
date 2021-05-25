package tw.firemaples.onscreenocr.ui.permissions

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.ui.BaseFragment
import tw.firemaples.onscreenocr.utils.PermissionUtil
import tw.firemaples.onscreenocr.utils.Toaster
import kotlin.reflect.KClass

class PermissionFloatWindowFragment : BaseFragment<PermissionFloatWindowViewModel>() {

    override val layoutId: Int
        get() = R.layout.permission_float_window_fragment

    override val vmClass: KClass<PermissionFloatWindowViewModel>
        get() = PermissionFloatWindowViewModel::class

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val granted = PermissionUtil.canDrawOverlays(requireContext())

        if (granted) {
            goCaptureScreenPage()
        }

        setViews(view)
    }

    private fun setViews(view: View) {
        view.findViewById<Button>(R.id.bt_requestPermission).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                goPermissionPage()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun goPermissionPage() {
        var intent =
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${requireContext().packageName}"))

        try {
            resultLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            intent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
            resultLauncher.launch(intent)
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (PermissionUtil.canDrawOverlays(requireContext())) {
            goCaptureScreenPage()
        } else {
            Toaster.show(R.string.msg_evertranslator_needs_display_over_other_apps_permission_to_show_a_floating_window_for_easily_using)
        }
    }

    private fun goCaptureScreenPage() {
        val action = PermissionFloatWindowFragmentDirections.actionRequestCaptureScreenPage()
        findNavController().navigate(action)
    }
}
