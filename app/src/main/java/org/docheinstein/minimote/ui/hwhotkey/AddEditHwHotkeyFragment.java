package org.docheinstein.minimote.ui.hwhotkey;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.docheinstein.minimote.R;
import org.docheinstein.minimote.buttons.ButtonType;
import org.docheinstein.minimote.database.DB;
import org.docheinstein.minimote.database.hotkey.HotkeyEntity;
import org.docheinstein.minimote.database.hwhotkey.HwHotkeyEntity;
import org.docheinstein.minimote.keys.MinimoteKeyType;
import org.docheinstein.minimote.ui.base.MinimoteFragment;

import java.util.concurrent.ExecutionException;

public class AddEditHwHotkeyFragment extends MinimoteFragment {

    private enum FragmentMode {
        Add,
        Edit
    }

    public static final int ADD_HW_HOTKEY_MAGIC_ACTION_ID = -1;

    public static final String FRAGMENT_TAG = "add_edit_hwhotkey_fragment";

    private static final String TAG = "HwHotkeysFragment";

    private CheckBox uiModifierAlt;
    private CheckBox uiModifierAltGr;
    private CheckBox uiModifierCtrl;
    private CheckBox uiModifierMeta;
    private CheckBox uiModifierShift;

    private Spinner uiButton;
    private Spinner uiKey;

    private Integer mEditHotkeyId = null;
    private MenuItem mDeleteHwHotkeyMenuButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_edit_hwhotkey, container, false);

        uiButton = view.findViewById(R.id.uiButton);
        uiModifierAlt = view.findViewById(R.id.uiModifierAlt);
        uiModifierAltGr = view.findViewById(R.id.uiModifierAltGr);
        uiModifierCtrl = view.findViewById(R.id.uiModifierCtrl);
        uiModifierMeta = view.findViewById(R.id.uiModifierMeta);
        uiModifierShift = view.findViewById(R.id.uiModifierShift);
        uiKey = view.findViewById(R.id.uiKey);

        // Check if in add or edit mode

        Bundle args = getArguments();

        if (args != null) {
            mEditHotkeyId = AddEditHwHotkeyFragmentArgs.fromBundle(args).getHwhotkeyId();
            Log.d(TAG, "mEditHotkeyId: " + mEditHotkeyId);

            if (getCurrentFragmentMode() == FragmentMode.Edit) {
                DB.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        HwHotkeyEntity h = DB.getInstance().hwhotkeys().getById(mEditHotkeyId);
                        if (h == null)
                            return;
                        Log.d(TAG, "Editing valid hotkey, updating UI");
                        uiModifierAlt.setChecked(h.alt);
                        uiModifierAltGr.setChecked(h.altgr);
                        uiModifierCtrl.setChecked(h.ctrl);
                        uiModifierMeta.setChecked(h.meta);
                        uiModifierShift.setChecked(h.shift);
                        uiKey.setSelection(((ArrayAdapter<String>) uiKey.getAdapter()).getPosition(h.key));
                        uiButton.setSelection(((ArrayAdapter<String>) uiButton.getAdapter()).getPosition(h.button));
                        if (mDeleteHwHotkeyMenuButton != null)
                            mDeleteHwHotkeyMenuButton.setVisible(true);
                    }
                });
            }
        }

        setToolbarTitle(
                getCurrentFragmentMode() == FragmentMode.Edit ?
                        "Edit physical hotkey" :
                        "Add physical hotkey");

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.add_edit_hw_hotkey, menu);
        mDeleteHwHotkeyMenuButton = menu.findItem(R.id.uiDeleteHwHotkeyMenuItem);

        if (getCurrentFragmentMode() == FragmentMode.Edit && mDeleteHwHotkeyMenuButton != null) {
            mDeleteHwHotkeyMenuButton.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.uiDeleteHwHotkeyMenuItem:
                handleDeleteButtonClick();
                return true;
            case R.id.uiSaveHwHotkeyMenuItem:
                handleSaveButtonClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void handleDeleteButtonClick() {
        Log.v(TAG, "Clicked on delete button");

        DB.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if (mEditHotkeyId == null || getCurrentFragmentMode() == FragmentMode.Add) {
                    Log.w(TAG, "Hotkey id should not be null if delete button is enabled");
                    return;
                }

                DB.getInstance().hwhotkeys().deleteById(mEditHotkeyId);
            }
        });

        goBack();
    }

    private void handleSaveButtonClick() {
        Log.v(TAG, "Clicked on save button");

        final boolean alt = uiModifierAlt.isChecked();
        final boolean altgr = uiModifierAltGr.isChecked();
        final boolean ctrl = uiModifierCtrl.isChecked();
        final boolean meta = uiModifierMeta.isChecked();
        final boolean shift = uiModifierShift.isChecked();

        String keyStr = uiKey.getSelectedItem().toString();
        final MinimoteKeyType key = MinimoteKeyType.fromString(keyStr);

        String buttonStr = uiButton.getSelectedItem().toString();
        final ButtonType button = ButtonType.fromString(buttonStr);

        Log.d(TAG, "alt: " + alt);
        Log.d(TAG, "altgr: " + altgr);
        Log.d(TAG, "ctrl: " + ctrl);
        Log.d(TAG, "meta: " + meta);
        Log.d(TAG, "shift: " + shift);
        Log.d(TAG, "key: " + key);
        Log.d(TAG, "button: " + button);

        try {
            Log.d(TAG, "Submitting hw hotkey add/edit");
            DB.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Creating hw hotkey");

                    HwHotkeyEntity hwhotkey = new HwHotkeyEntity(
                            getCurrentFragmentMode() == FragmentMode.Add ? 0 : mEditHotkeyId,
                            button.toString(),
                            shift, ctrl, alt, altgr, meta,
                            key.toString()
                    );
                    Log.d(TAG, "Pushing hw hotkey to DB\n" + hwhotkey);

                    DB.getInstance().hwhotkeys().addOrReplace(hwhotkey);
                }
            }).get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error while adding hw hotkey", e);
        }

        goBack();
    }

    private FragmentMode getCurrentFragmentMode() {
        return mEditHotkeyId == ADD_HW_HOTKEY_MAGIC_ACTION_ID ? FragmentMode.Add : FragmentMode.Edit;
    }
}