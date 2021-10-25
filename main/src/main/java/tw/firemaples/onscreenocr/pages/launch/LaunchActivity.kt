package tw.firemaples.onscreenocr.pages.launch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigManager
import tw.firemaples.onscreenocr.utils.MoPubAdManager

class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        MoPubAdManager.loadPermissionPageBanner(this, findViewById(R.id.ad_permissionPage))

        RemoteConfigManager.tryFetchNew()
    }
}
