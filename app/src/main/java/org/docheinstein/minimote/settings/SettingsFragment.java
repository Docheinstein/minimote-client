package org.docheinstein.minimote.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import org.docheinstein.minimote.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
