package org.docheinstein.minimote.hotkeys;

import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.docheinstein.minimote.R;
import org.docheinstein.minimote.base.MinimoteFragment;
import org.docheinstein.minimote.database.DB;
import org.docheinstein.minimote.database.hotkey.HotkeyEntity;
import org.docheinstein.minimote.keys.MinimoteKeyType;
import org.docheinstein.minimote.settings.SettingsManager;
import org.docheinstein.minimote.utils.StringUtils;

public class AddEditHotkeyFragment extends MinimoteFragment {
    private static final String TAG = "HotkeysFragment";

    public static final int ADD_HOTKEY_MAGIC_ACTIOPN_ID = -1;

    TextView uiHotkeyPreview;
    EditText uiHotkeyName;

    CheckBox uiModifierAlt;
    CheckBox uiModifierAltGr;
    CheckBox uiModifierCtrl;
    CheckBox uiModifierMeta;
    CheckBox uiModifierShift;

    Spinner uiKey;

    SeekBar uiTextSize;
    SeekBar uiPadding;

    private Integer mEditHotkeyId = null;
    private MenuItem mDeleteHotkeyMenuButton;

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
        View view = inflater.inflate(R.layout.add_edit_hotkey, container, false);

        uiHotkeyPreview = view.findViewById(R.id.uiHotkeyPreview);
        uiHotkeyName = view.findViewById(R.id.uiHotkeyName);
        uiModifierAlt = view.findViewById(R.id.uiModifierAlt);
        uiModifierAltGr = view.findViewById(R.id.uiModifierAltGr);
        uiModifierCtrl = view.findViewById(R.id.uiModifierCtrl);
        uiModifierMeta = view.findViewById(R.id.uiModifierMeta);
        uiModifierShift = view.findViewById(R.id.uiModifierShift);
        uiKey = view.findViewById(R.id.uiKey);
        uiTextSize = view.findViewById(R.id.uiTextSize);
        uiPadding = view.findViewById(R.id.uiPadding);

        int initialTextSize = uiTextSize.getProgress() * 2;
        uiHotkeyPreview.setTextSize(initialTextSize);

        int initialPadding = uiPadding.getProgress() * 2;
        uiHotkeyPreview.setPadding(initialPadding, initialPadding, initialPadding, initialPadding);

        uiHotkeyPreview.setText(getCurrentHotkeyName());

        GradientDrawable hotkeyBackground = new GradientDrawable();
        hotkeyBackground.setStroke(3, SettingsManager.hotkeyBorderColor(getContext()));
        hotkeyBackground.setCornerRadius(10);
        hotkeyBackground.setColor(SettingsManager.hotkeyBackgroundColor(getContext()));

        GradientDrawable hotkeyPressedBackground = new GradientDrawable();
        hotkeyPressedBackground.setStroke(3, SettingsManager.hotkeyBorderColor(getContext()));
        hotkeyPressedBackground.setCornerRadius(10);
        hotkeyPressedBackground.setColor(SettingsManager.hotkeyPressedBackgroundColor(getContext()));


        StateListDrawable hotkeySelector = new StateListDrawable();
        hotkeySelector.addState(new int[] {android.R.attr.state_pressed}, hotkeyPressedBackground);
        hotkeySelector.addState(new int[] {}, hotkeyBackground);

        uiHotkeyPreview.setBackground(hotkeySelector);
        uiHotkeyPreview.setTextColor(SettingsManager.hotkeyTextColor(getContext()));

        setModifierListener(uiModifierAlt);
        setModifierListener(uiModifierAltGr);
        setModifierListener(uiModifierCtrl);
        setModifierListener(uiModifierMeta);
        setModifierListener(uiModifierShift);

        uiPadding.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.v(TAG, "Padding change, updating preview");
                progress = progress * 2;
                uiHotkeyPreview.setPadding(progress, progress, progress, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        uiTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.v(TAG, "Padding change, updating preview");
                progress = progress * 2;
                uiHotkeyPreview.setTextSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        uiHotkeyName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                uiHotkeyPreview.setText(getCurrentHotkeyName());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        uiKey.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                uiHotkeyPreview.setText(getCurrentHotkeyName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // Check if add/edit

        Bundle args = getArguments();

        if (args != null) {
            mEditHotkeyId = AddEditHotkeyFragmentArgs.fromBundle(args).getHotkeyId();
            if (mEditHotkeyId != ADD_HOTKEY_MAGIC_ACTIOPN_ID) {
                DB.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        HotkeyEntity h = DB.getInstance().hotkeyEntityDao().getById(mEditHotkeyId);
                        if (h == null)
                            return;
                        Log.d(TAG, "Editing of valid hotkey, updating UI");
                        uiHotkeyName.setText(h.name);
                        uiModifierAlt.setChecked(h.alt);
                        uiModifierAltGr.setChecked(h.altgr);
                        uiModifierCtrl.setChecked(h.ctrl);
                        uiModifierMeta.setChecked(h.meta);
                        uiModifierShift.setChecked(h.shift);
                        uiKey.setSelection(((ArrayAdapter<String>) uiKey.getAdapter()).getPosition(h.key));
                        uiTextSize.setProgress(h.textSize / 2);
                        uiPadding.setProgress(h.padding / 2);
                        if (mDeleteHotkeyMenuButton != null)
                            mDeleteHotkeyMenuButton.setVisible(true);
                    }
                });
            }

        }

        setToolbarTitle(mEditHotkeyId != ADD_HOTKEY_MAGIC_ACTIOPN_ID ? "Edit hotkey" : "Add hotkey");

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.add_edit_hotkey_menu, menu);
        mDeleteHotkeyMenuButton = menu.findItem(R.id.uiDeleteHotkeyMenuItem);

        if (mEditHotkeyId != ADD_HOTKEY_MAGIC_ACTIOPN_ID && mDeleteHotkeyMenuButton != null) {
            mDeleteHotkeyMenuButton.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.uiDeleteHotkeyMenuItem:
                handleDeleteButtonClick();
                return true;
            case R.id.uiSaveHotkeyMenuItem:
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
                if (mEditHotkeyId == ADD_HOTKEY_MAGIC_ACTIOPN_ID) {
                    Log.w(TAG, "Hotkey id should not be null if delete button is enabled");
                    return;
                }

                DB.getInstance().hotkeyEntityDao().deleteById(mEditHotkeyId);
            }
        });

        goBack();
    }

    private void handleSaveButtonClick() {
        Log.v(TAG, "Clicked on save button");

        final String name = getCurrentHotkeyName();
        final boolean alt = uiModifierAlt.isChecked();
        final boolean altgr = uiModifierAltGr.isChecked();
        final boolean ctrl = uiModifierCtrl.isChecked();
        final boolean meta = uiModifierMeta.isChecked();
        final boolean shift = uiModifierShift.isChecked();

        String keyStr = uiKey.getSelectedItem().toString();
        final MinimoteKeyType key = MinimoteKeyType.fromString(keyStr);

        Log.d(TAG, "name: " + name);
        Log.d(TAG, "alt: " + alt);
        Log.d(TAG, "altgr: " + altgr);
        Log.d(TAG, "ctrl: " + ctrl);
        Log.d(TAG, "meta: " + meta);
        Log.d(TAG, "shift: " + shift);
        Log.d(TAG, "key: " + key);

        DB.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                HotkeyEntity hotkey = new HotkeyEntity(
                        mEditHotkeyId != ADD_HOTKEY_MAGIC_ACTIOPN_ID ? mEditHotkeyId : 0,
                        name,
                        shift, ctrl, alt, altgr, meta,
                        key.toString(),
                        uiTextSize.getProgress() * 2,
//                        0xff32a852,
//                        0xffcfd0e3,
//                        3, 0xffd18b28,
                        uiPadding.getProgress() * 2,
                        0.1, 10
                );
                Log.v(TAG, "Pushing hotkey to DB\n" + hotkey);

                DB.getInstance().hotkeyEntityDao().addOrReplace(hotkey);
            }
        });

        goBack();
    }

    private String getCurrentHotkeyName() {
        String s = uiHotkeyName.getText().toString();
        if (StringUtils.isValid(s))
            return s;

        StringBuilder sb = new StringBuilder();
        if (uiModifierCtrl.isChecked())
            sb.append("CTRL");

        if (uiModifierAlt.isChecked()) {
            if (sb.length() > 0)
                sb.append("+");
            sb.append("ALT");
        }

        if (uiModifierAltGr.isChecked()) {
            if (sb.length() > 0)
                sb.append("+");
            sb.append("ALT GR");
        }

        if (uiModifierMeta.isChecked()) {
            if (sb.length() > 0)
                sb.append("+");
            sb.append("META");
        }

        if (uiModifierShift.isChecked()) {
            if (sb.length() > 0)
                sb.append("+");
            sb.append("SHIFT");
        }

        String key = uiKey.getSelectedItem().toString();
        if (sb.length() > 0)
            sb.append("+");
        sb.append(key);

        return sb.toString();
    }

    private void setModifierListener(CheckBox checkBox) {
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                uiHotkeyPreview.setText(getCurrentHotkeyName());
            }
        });
    }
}