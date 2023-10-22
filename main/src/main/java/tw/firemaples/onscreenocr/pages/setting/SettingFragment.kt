package tw.firemaples.onscreenocr.pages.setting

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.utils.Toaster
import tw.firemaples.onscreenocr.utils.Utils

class SettingFragment : PreferenceFragmentCompat() {

    private val bypassBatteryOptimization: Preference?
        get() = findPreference("pref_bypass_battery_optimization")

    private val myMemoryEmail: EditTextPreference?
        get() = findPreference(SettingManager.PREF_MYMEMORY_EMAIL)

    private val keepMediaProjectionResources: SwitchPreference?
        get() = findPreference(SettingManager.PREF_KEEP_MEDIA_PROJECTION_RESOURCES)

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

        keepMediaProjectionResources?.setOnPreferenceChangeListener { _, _ ->
            Toaster.show(getString(R.string.msg_please_restart_the_app_to_take_effect))
            true
        }
    }

    override fun onResume() {
        super.onResume()
        bypassBatteryOptimization?.isVisible = Utils.batteryOptimized(requireContext())
    }
}
