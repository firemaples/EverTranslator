package tw.firemaples.onscreenocr.pages.setting

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import tw.firemaples.onscreenocr.R

class SettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.perference, rootKey)
    }
}
