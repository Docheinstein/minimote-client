package org.docheinstein.minimote.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import org.docheinstein.minimote.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}