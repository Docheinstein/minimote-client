package org.docheinstein.minimote.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import org.docheinstein.minimote.R;
import org.docheinstein.minimote.utils.ViewUtils;

import static android.content.Context.INPUT_METHOD_SERVICE;

public abstract class MinimoteFragment extends Fragment {

    private static final String TAG = "MinimoteFragment";

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.v(TAG, "MinimoteFragment.onDestroyView()");
        hideKeyboard();
    }

    protected void ui(Runnable runnable) {
        Activity a = getActivity();
        if (a != null)
            a.runOnUiThread(runnable);
    }

    protected void goBack() {
        goBack(null);
    }

    protected void goBack(Bundle args) {
        Log.v(TAG, "MinimoteFragment.goBack()");
        NavController navController = NavHostFragment.findNavController(this);

        if (args != null) {
            Activity parentActivity = requireActivity();
            if (parentActivity instanceof FragmentResultListener) {
                Log.v(TAG, "Providing fragment result to nav controller");
                ((FragmentResultListener) parentActivity).onFragmentResult(this, args);
            }
        }

        navController.navigateUp();
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

    protected void hideKeyboard() {
        Activity activity = requireActivity();

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

    protected void showSoftKeyboard(View keyboardBoundView) {
        ViewUtils.show(keyboardBoundView);

        if (!keyboardBoundView.requestFocus()) {
            Log.w(TAG, "Failed to acquire focus on view bound to keyboard");
            return;
        }

        Activity a = getActivity();
        if (a == null) {
            Log.w(TAG, "Null activity?");
            return;
        }

        InputMethodManager imm = (InputMethodManager) a.getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(keyboardBoundView, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
