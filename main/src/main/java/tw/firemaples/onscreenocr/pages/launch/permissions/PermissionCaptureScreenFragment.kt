package tw.firemaples.onscreenocr.pages.launch.permissions

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import tw.firemaples.onscreenocr.databinding.PermissionCaptureScreenFragmentBinding
import tw.firemaples.onscreenocr.floatings.ViewHolderService
import tw.firemaples.onscreenocr.pages.setting.SettingManager
import tw.firemaples.onscreenocr.screenshot.ScreenExtractor
import tw.firemaples.onscreenocr.utils.clickOnce

class PermissionCaptureScreenFragment : Fragment() {

    private var _binding: PermissionCaptureScreenFragmentBinding? = null
    private val binding: PermissionCaptureScreenFragmentBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return PermissionCaptureScreenFragmentBinding.inflate(inflater, container, false).apply {
            _binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()

        if (ScreenExtractor.isGranted) {
            startService()
        }
    }

    private fun setViews() {
        binding.btRequestPermission.clickOnce {
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
                ScreenExtractor.onMediaProjectionGranted(
                    intent = intent,
                    keepMediaProjection = SettingManager.keepMediaProjectionResources,
                )
                startService()
            }
        }

    private fun startService() {
        ViewHolderService.showViews(requireActivity())
        requireActivity().finishAffinity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
