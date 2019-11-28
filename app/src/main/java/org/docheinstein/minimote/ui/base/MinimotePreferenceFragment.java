package org.docheinstein.minimote.base;

import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import org.docheinstein.minimote.utils.ResUtils;

public abstract class MinimotePreferenceFragment extends PreferenceFragmentCompat {

    protected void setEditTextPreferenceInputType(@StringRes int prefKey, final int inputTypeFlags) {
        String serverPortPrefKey = ResUtils.getString(prefKey, getContext());
        if (serverPortPrefKey != null) {
            EditTextPreference serverPortPref = getPreferenceManager().findPreference(serverPortPrefKey);
            if (serverPortPref != null) {
                serverPortPref.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                    @Override
                    public void onBindEditText(@NonNull EditText editText) {
                        editText.setInputType(inputTypeFlags);
                    }
                });
            }
        }
    }
}
