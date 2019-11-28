package org.docheinstein.minimote.base;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.docheinstein.minimote.R;

public abstract class MinimoteFragment extends Fragment {

    private static final String TAG = "MinimoteFragment";

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.v(TAG, "MinimoteFragment.onDestroyView()");
        hideKeyboard(getActivity());
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

    protected void setToolbarTitle(String title) {
        Activity activity = getActivity();
        if (activity != null) {
            final Toolbar toolbar = activity.findViewById(R.id.uiToolbar);
            if (toolbar != null) {
                toolbar.setTitle(title);
            }
        }
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
