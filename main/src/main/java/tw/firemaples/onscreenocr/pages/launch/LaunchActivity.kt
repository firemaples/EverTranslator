package tw.firemaples.onscreenocr.pages.launch

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tw.firemaples.onscreenocr.databinding.ActivityLaunchBinding
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigManager
import tw.firemaples.onscreenocr.utils.AdManager
import tw.firemaples.onscreenocr.utils.DeviceInfoChecker

class LaunchActivity : AppCompatActivity() {

    companion object {
        fun getLaunchIntent(context: Context): Intent =
            Intent(context, LaunchActivity::class.java).apply {
                flags += Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        fun getLaunchPendingIntent(context: Context): PendingIntent =
            PendingIntent.getActivity(
                context,
                1,
                getLaunchIntent(context = context),
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
    }

    private lateinit var binding: ActivityLaunchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AdManager.loadBanner(binding.admobAd.root)

//        MoPubAdManager.loadPermissionPageBanner(this, findViewById(R.id.ad_permissionPage))

        RemoteConfigManager.tryFetchNew()

        DeviceInfoChecker.check()
    }
}
