package org.docheinstein.minimote.ui.settings;

import android.os.Bundle;

import org.docheinstein.minimote.R;
import org.docheinstein.minimote.ui.base.MinimotePreferenceFragment;

public class SettingsFragment extends MinimotePreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
