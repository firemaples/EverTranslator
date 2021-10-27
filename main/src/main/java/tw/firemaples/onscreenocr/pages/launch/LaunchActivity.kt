package tw.firemaples.onscreenocr.pages.launch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigManager
import tw.firemaples.onscreenocr.utils.MoPubAdManager

class LaunchActivity : AppCompatActivity() {

    companion object {
        fun getLaunchIntent(context: Context): Intent =
            Intent(context, LaunchActivity::class.java).apply {
                flags += Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        MoPubAdManager.loadPermissionPageBanner(this, findViewById(R.id.ad_permissionPage))

        RemoteConfigManager.tryFetchNew()
    }
}
