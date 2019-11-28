package org.docheinstein.minimote.settings;

import android.os.Bundle;
import android.text.InputType;

import org.docheinstein.minimote.R;
import org.docheinstein.minimote.base.MinimotePreferenceFragment;

public class SettingsFragment extends MinimotePreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
