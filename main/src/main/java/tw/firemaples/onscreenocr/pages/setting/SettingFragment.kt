package tw.firemaples.onscreenocr.pages.setting

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.utils.Utils

class SettingFragment : PreferenceFragmentCompat() {

    private val bypassBatteryOptimization: Preference?
        get() = findPreference("pref_bypass_battery_optimization")

    private val myMemoryEmail: EditTextPreference?
        get() = findPreference("pref_mymemory_email")

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.perference, rootKey)

        bypassBatteryOptimization?.setOnPreferenceClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
            return@setOnPreferenceClickListener true
        }

        myMemoryEmail?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
    }

    override fun onResume() {
        super.onResume()
        bypassBatteryOptimization?.isVisible = Utils.batteryOptimized(requireContext())
    }
}
