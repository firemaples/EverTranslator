package tw.firemaples.onscreenocr.pages.launch.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import tw.firemaples.onscreenocr.R
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
            requestNotificationPermissionOrStartService()
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
                requestNotificationPermissionOrStartService()
            }
        }

    private fun requestNotificationPermissionOrStartService() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startService()
        } else {
            requestNotificationPermission {
                startService()
            }
        }
    }

    private val notificationResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            startService()
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission(
        onResult: () -> Unit,
    ) {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                onResult.invoke()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission) -> {
                AlertDialog.Builder(requireContext())
                    .setMessage(getString(R.string.msg_grant_posting_notification_permission_rationale))
                    .setPositiveButton(getString(R.string.btn_request_again)) { _, _ ->
                        notificationResultLauncher.launch(permission)
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ ->
                        onResult.invoke()
                    }
                    .show()
            }

            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                notificationResultLauncher.launch(permission)
            }
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
