package tw.firemaples.onscreenocr.pages.launch.permissions

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.ViewHolderService
import tw.firemaples.onscreenocr.screenshot.ScreenExtractor
import tw.firemaples.onscreenocr.utils.clickOnce

class PermissionCaptureScreenFragment : Fragment(R.layout.permission_capture_screen_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews(view)

        if (ScreenExtractor.isGranted) {
            startService()
        }
    }

    private fun setViews(view: View) {
        view.findViewById<View>(R.id.bt_requestPermission).clickOnce {
            requestMediaProject()
        }
    }

    private fun requestMediaProject() {
        val manager =
            requireContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        resultLauncher.launch(manager.createScreenCaptureIntent())
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val intent = it.data
            if (it.resultCode == Activity.RESULT_OK && intent != null) {
                ScreenExtractor.onMediaProjectionGranted(intent)
                startService()
            }
        }

    private fun startService() {
        ViewHolderService.showViews(requireActivity())
        requireActivity().finishAffinity()
    }
}
