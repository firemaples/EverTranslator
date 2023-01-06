package tw.firemaples.onscreenocr.pages.launch.permissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import tw.firemaples.onscreenocr.databinding.PermissionCaptureScreenFragmentBinding
import tw.firemaples.onscreenocr.floatings.ViewHolderService
import tw.firemaples.onscreenocr.screenshot.ScreenExtractor
import tw.firemaples.onscreenocr.utils.Utils
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

        binding.btRequestBatteryOpt.clickOnce {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.batteryOptimizationBlock.isVisible = Utils.batteryOptimized(requireContext())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
