package tw.firemaples.onscreenocr.pages.launch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.databinding.ActivityLaunchBinding
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigManager
import tw.firemaples.onscreenocr.utils.AdManager
import tw.firemaples.onscreenocr.utils.MoPubAdManager

class LaunchActivity : AppCompatActivity() {

    companion object {
        fun getLaunchIntent(context: Context): Intent =
            Intent(context, LaunchActivity::class.java).apply {
                flags += Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
    }

    private lateinit var binding: ActivityLaunchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AdManager.loadBanner(binding.admobAd)

//        MoPubAdManager.loadPermissionPageBanner(this, findViewById(R.id.ad_permissionPage))

        RemoteConfigManager.tryFetchNew()
    }
}
