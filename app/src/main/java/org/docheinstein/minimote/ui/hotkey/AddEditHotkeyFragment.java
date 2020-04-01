package org.docheinstein.minimote.ui.hotkey;

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
import org.docheinstein.minimote.ui.base.MinimoteFragment;
import org.docheinstein.minimote.database.DB;
import org.docheinstein.minimote.database.hotkey.HotkeyEntity;
import org.docheinstein.minimote.keys.MinimoteKeyType;
import org.docheinstein.minimote.settings.SettingsManager;
import org.docheinstein.minimote.utils.ResUtils;
import org.docheinstein.minimote.utils.StringUtils;

public class AddEditHotkeyFragment extends MinimoteFragment {

    private enum FragmentMode {
        Add,
        Edit
    }

    public static final int ADD_HOTKEY_MAGIC_ACTION_ID = -1;

    private static final String TAG = "HotkeysFragment";
    private static final int TEXT_SIZE_SLIDER_FACTOR = 2;
    private static final int PADDING_SLIDER_FACTOR = 2;

    private static final double DEFAULT_X_REL = 0.1;
    private static final int DEFAULT_Y_ABS = 10;


    private TextView uiHotkeyPreview;
    private EditText uiHotkeyName;

    private CheckBox uiModifierAlt;
    private CheckBox uiModifierAltGr;
    private CheckBox uiModifierCtrl;
    private CheckBox uiModifierMeta;
    private CheckBox uiModifierShift;

    private Spinner uiKey;

    private SeekBar uiTextSize;
    private SeekBar uiPadding;

    private TextView uiTextSizeCurrentValue;
    private TextView uiPaddingCurrentValue;

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
        uiHotkeyName = view.findViewById(R.id.uiDisplayName);
        uiModifierAlt = view.findViewById(R.id.uiModifierAlt);
        uiModifierAltGr = view.findViewById(R.id.uiModifierAltGr);
        uiModifierCtrl = view.findViewById(R.id.uiModifierCtrl);
        uiModifierMeta = view.findViewById(R.id.uiModifierMeta);
        uiModifierShift = view.findViewById(R.id.uiModifierShift);
        uiKey = view.findViewById(R.id.uiKey);
        uiTextSize = view.findViewById(R.id.uiTextSize);
        uiPadding = view.findViewById(R.id.uiPadding);
        uiTextSizeCurrentValue = view.findViewById(R.id.uiTextSizeCurrentValue);
        uiPaddingCurrentValue = view.findViewById(R.id.uiPaddingCurrentValue);

        // Style hotkey preview

        updateHotkeyTextSize(uiTextSize.getProgress() * TEXT_SIZE_SLIDER_FACTOR);
        updateHotkeyPadding(uiPadding.getProgress() * PADDING_SLIDER_FACTOR);

        uiHotkeyPreview.setText(getCurrentHotkeyName());
        uiHotkeyPreview.setTextColor(SettingsManager.hotkeyTextColor(getContext()));

        GradientDrawable hotkeyBackground = new GradientDrawable();
        hotkeyBackground.setStroke(3, SettingsManager.hotkeyBorderColor(getContext()));
        hotkeyBackground.setCornerRadius(10);
        hotkeyBackground.setColor(SettingsManager.hotkeyUnpressedColor(getContext()));

        GradientDrawable hotkeyPressedBackground = new GradientDrawable();
        hotkeyPressedBackground.setStroke(3, SettingsManager.hotkeyBorderColor(getContext()));
        hotkeyPressedBackground.setCornerRadius(10);
        hotkeyPressedBackground.setColor(SettingsManager.hotkeyPressedColor(getContext()));

        StateListDrawable hotkeySelector = new StateListDrawable();
        hotkeySelector.addState(new int[] {android.R.attr.state_pressed}, hotkeyPressedBackground);
        hotkeySelector.addState(new int[] {}, hotkeyBackground);

        uiHotkeyPreview.setBackground(hotkeySelector);

        setModifierListener(uiModifierAlt);
        setModifierListener(uiModifierAltGr);
        setModifierListener(uiModifierCtrl);
        setModifierListener(uiModifierMeta);
        setModifierListener(uiModifierShift);

        uiTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.v(TAG, "Text size slider value is changed, updating preview");
                updateHotkeyTextSize(progress * TEXT_SIZE_SLIDER_FACTOR);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        uiPadding.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.v(TAG, "Padding slider value is changed, updating preview");
                updateHotkeyPadding(progress * PADDING_SLIDER_FACTOR);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        uiHotkeyName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                uiHotkeyPreview.setText(getCurrentHotkeyName());
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        uiKey.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                uiHotkeyPreview.setText(getCurrentHotkeyName());
            }

            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });


        // Check if in add or edit mode

        Bundle args = getArguments();

        if (args != null) {
            mEditHotkeyId = AddEditHotkeyFragmentArgs.fromBundle(args).getHotkeyId();
            if (getCurrentFragmentMode() == FragmentMode.Edit) {
                DB.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        HotkeyEntity h = DB.getInstance().hotkeys().getById(mEditHotkeyId);
                        if (h == null)
                            return;
                        Log.d(TAG, "Editing valid hotkey, updating UI\n" + h);
                        uiHotkeyName.setText(h.name);
                        uiModifierAlt.setChecked(h.alt);
                        uiModifierAltGr.setChecked(h.altgr);
                        uiModifierCtrl.setChecked(h.ctrl);
                        uiModifierMeta.setChecked(h.meta);
                        uiModifierShift.setChecked(h.shift);

                        MinimoteKeyType keyType = MinimoteKeyType.fromString(h.key);
                        int pos = ((ArrayAdapter<String>) uiKey.getAdapter()).getPosition(keyType.toString());
                        Log.d(TAG, "Key pos:" + pos);
                        uiKey.setSelection(pos);

                        uiTextSize.setProgress(h.textSize / TEXT_SIZE_SLIDER_FACTOR);
                        uiPadding.setProgress(h.padding / PADDING_SLIDER_FACTOR);
                        if (mDeleteHotkeyMenuButton != null)
                            mDeleteHotkeyMenuButton.setVisible(true);
                    }
                });
            }
        }

        setToolbarTitle(
                getCurrentFragmentMode() == FragmentMode.Edit ?
                        "Edit graphical hotkey" :
                        "Add graphical hotkey");

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.add_edit_hotkey, menu);
        mDeleteHotkeyMenuButton = menu.findItem(R.id.uiDeleteHotkeyMenuItem);

        if (getCurrentFragmentMode() == FragmentMode.Edit && mDeleteHotkeyMenuButton != null) {
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
                if (mEditHotkeyId == null || getCurrentFragmentMode() == FragmentMode.Add) {
                    Log.w(TAG, "Hotkey id should not be null if delete button is enabled");
                    return;
                }

                DB.getInstance().hotkeys().deleteById(mEditHotkeyId);
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
                        getCurrentFragmentMode() == FragmentMode.Add ? 0 : mEditHotkeyId,
                        name,
                        shift, ctrl, alt, altgr, meta,
                        key.toString(),
                        uiTextSize.getProgress() * TEXT_SIZE_SLIDER_FACTOR,
//                        0xff32a852,
//                        0xffcfd0e3,
//                        3, 0xffd18b28,
                        uiPadding.getProgress() * PADDING_SLIDER_FACTOR,
                        DEFAULT_X_REL,
                        DEFAULT_Y_ABS
                );
                Log.v(TAG, "Pushing hotkey to DB\n" + hotkey);

                DB.getInstance().hotkeys().addOrReplace(hotkey);
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
            sb.append(ResUtils.getString(R.string.edit_hotkey_ctrl, getContext()));

        if (uiModifierAlt.isChecked()) {
            if (sb.length() > 0)
                sb.append("+");
            sb.append(ResUtils.getString(R.string.edit_hotkey_alt, getContext()));
        }

        if (uiModifierAltGr.isChecked()) {
            if (sb.length() > 0)
                sb.append("+");
            sb.append(ResUtils.getString(R.string.edit_hotkey_altgr, getContext()));
        }

        if (uiModifierMeta.isChecked()) {
            if (sb.length() > 0)
                sb.append("+");
            sb.append(ResUtils.getString(R.string.edit_hotkey_meta, getContext()));
        }

        if (uiModifierShift.isChecked()) {
            if (sb.length() > 0)
                sb.append("+");
            sb.append(ResUtils.getString(R.string.edit_hotkey_shift, getContext()));
        }

        String key = uiKey.getSelectedItem().toString();
        if (sb.length() > 0)
            sb.append("+");
        sb.append(key);

        return sb.toString();
    }

    private void updateHotkeyTextSize(int textSize) {
        uiHotkeyPreview.setTextSize(textSize);
        uiTextSizeCurrentValue.setText(String.valueOf(textSize));
    }

    private void updateHotkeyPadding(int padding) {
        uiHotkeyPreview.setPadding(padding, padding, padding, padding);
        uiPaddingCurrentValue.setText(String.valueOf(padding));
    }

    private FragmentMode getCurrentFragmentMode() {
        return mEditHotkeyId == ADD_HOTKEY_MAGIC_ACTION_ID ? FragmentMode.Add : FragmentMode.Edit;
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