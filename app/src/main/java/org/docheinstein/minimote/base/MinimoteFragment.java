package org.docheinstein.minimote.base;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.docheinstein.minimote.R;
import org.docheinstein.minimote.commons.conf.Conf;
import org.docheinstein.minimote.utils.PrefUtils;

public abstract class MinimoteFragment extends Fragment {

    private static final String TAG = "MinimoteFragment";

    protected void showBackButton() {
        Activity a = getActivity();
        if (a == null)
            return;

        ActionBar ab = a.getActionBar();
        if (ab == null)
            return;

        ab.setDisplayShowTitleEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    protected void ui(Runnable runnable) {
        Activity a = getActivity();
        if (a != null)
            a.runOnUiThread(runnable);
    }

    protected void goBack() {
        Log.v(TAG, "MinimoteFragment.goBack()");
        NavHostFragment.findNavController(this).navigateUp();
    }

    public void setToolbarTitle(String title) {
        Activity activity = getActivity();
        if (activity != null) {
            final Toolbar toolbar = activity.findViewById(R.id.uiToolbar);
            if (toolbar != null) {
                toolbar.setTitle(title);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.v(TAG, "MinimoteFragment.onDestroyView()");
        hideKeyboard(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "MinimoteFragment.onDestroy()");
    }

    protected void hideKeyboard(Activity activity) {
        if (activity == null)
            return;

        InputMethodManager inputManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputManager == null)
            return;

        View currentFocusedView = activity.getCurrentFocus();

        if (currentFocusedView == null)
            return;

        inputManager.hideSoftInputFromWindow(
                currentFocusedView.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS
        );
    }
}
