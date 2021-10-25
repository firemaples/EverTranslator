package tw.firemaples.onscreenocr.pages.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.utils.MoPubAdManager

class SettingActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, SettingActivity::class.java).apply {
                flags =
                    flags or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        MoPubAdManager.loadPermissionPageBanner(this, findViewById(R.id.ad_settingPage))
    }
}
