package com.gigforce.app.modules.preferences

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.gigforce.app.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}