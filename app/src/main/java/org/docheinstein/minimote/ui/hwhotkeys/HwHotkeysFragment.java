package org.docheinstein.minimote.ui.hwhotkeys;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.docheinstein.minimote.R;
import org.docheinstein.minimote.buttons.ButtonType;
import org.docheinstein.minimote.database.DB;
import org.docheinstein.minimote.database.hwhotkey.HwHotkeyEntity;
import org.docheinstein.minimote.ui.base.MinimoteFragment;
import org.docheinstein.minimote.ui.hwhotkey.AddEditHwHotkeyFragment;
import org.docheinstein.minimote.utils.ResUtils;

import java.util.List;

public class HwHotkeysFragment extends MinimoteFragment {

    private static final String TAG = "ServersFragment";

    private HwHotkeyListAdapter uiHwHotkeysListAdapter;
    private RecyclerView.LayoutManager uiHwHotkeyListLayoutManager;
    private RecyclerView uiHwHotkeyList;

    private class HwHotkeyListAdapter extends RecyclerView.Adapter<HwHotkeyListAdapter.HwHotkeyViewHolder> {

        private List<HwHotkeyEntity> mHotkeys;

        public void setHwHotkeys(List<HwHotkeyEntity> hwhotkeys) {
            Log.d(TAG, "Updating hw hotkeys list UI");
            mHotkeys = hwhotkeys;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public HwHotkeyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.hw_hotkey_item, parent, false);
            return new HwHotkeyViewHolder(v);

        }

        @Override
        public void onBindViewHolder(@NonNull HwHotkeyViewHolder holder, int position) {
            if (mHotkeys == null) {
                Log.w(TAG, "Null hotkeys list");
                return;
            }

            if (position < 0 || position >= mHotkeys.size()) {
                // Fallback
                Log.w(TAG, "Invalid list position (" + position + ")");
                holder.uiHwHotkeyLabel.setText("");
                holder.uiHwHotkeyIcon.setImageIcon(null);
                holder.mRowClickListener = null;
                return;
            }

            final HwHotkeyEntity hwhotkey = mHotkeys.get(position);

            holder.mRowClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "Click on hw hotkey [" + hwhotkey.id + "], opening edit screen");

                    NavHostFragment.findNavController(HwHotkeysFragment.this)
                            .navigate(HwHotkeysFragmentDirections.actionAddEditHwHotkey(hwhotkey.id));
                }
            };
            holder.uiHwHotkeyLabel.setText(getHwHotkeyName(hwhotkey));

            int icon = 0;
            switch (ButtonType.fromString(hwhotkey.button)) {
                case VolumeUp:
                    icon = R.drawable.volume_up;
                    break;
                case VolumeDown:
                    icon = R.drawable.volume_down;
                    break;
                default:
                    Log.w(TAG, "Invalid hw hotkey button (and icon)");
            }

            holder.uiHwHotkeyIcon.setImageResource(icon);
        }


        @Override
        public int getItemCount() {
            return mHotkeys != null ? mHotkeys.size() : 0;
        }


        private String getHwHotkeyName(HwHotkeyEntity hwhotkey) {
            if (hwhotkey == null)
                return "<unknown>";

            StringBuilder sb = new StringBuilder();
            if (hwhotkey.ctrl)
                sb.append(ResUtils.getString(R.string.edit_hotkey_ctrl, getContext()));

            if (hwhotkey.alt) {
                if (sb.length() > 0)
                    sb.append("+");
                sb.append(ResUtils.getString(R.string.edit_hotkey_alt, getContext()));
            }

            if (hwhotkey.altgr) {
                if (sb.length() > 0)
                    sb.append("+");
                sb.append(ResUtils.getString(R.string.edit_hotkey_altgr, getContext()));
            }

            if (hwhotkey.meta) {
                if (sb.length() > 0)
                    sb.append("+");
                sb.append(ResUtils.getString(R.string.edit_hotkey_meta, getContext()));
            }

            if (hwhotkey.shift) {
                if (sb.length() > 0)
                    sb.append("+");
                sb.append(ResUtils.getString(R.string.edit_hotkey_shift, getContext()));
            }

            String key = hwhotkey.key;
            if (sb.length() > 0)
                sb.append("+");
            sb.append(key);

            return sb.toString();
        }

        private class HwHotkeyViewHolder extends RecyclerView.ViewHolder{
            ImageView uiHwHotkeyIcon;
            TextView uiHwHotkeyLabel;
            View.OnClickListener mRowClickListener;

            HwHotkeyViewHolder(@NonNull View itemView) {
                super(itemView);
                uiHwHotkeyIcon = itemView.findViewById(R.id.uiHwHotkeyIcon);
                uiHwHotkeyLabel = itemView.findViewById(R.id.uiHwHotkeyLabel);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mRowClickListener != null)
                            mRowClickListener.onClick(v);
                    }
                });
            }
        }
    }

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
        final View view = inflater.inflate(R.layout.hw_hotkeys, container, false);

        Log.v(TAG, "HwHotkeysFragment.onCreateView() [" + hashCode() + "]");

        uiHwHotkeyList = view.findViewById(R.id.uiHwHotkeyList);
        uiHwHotkeyList.setHasFixedSize(true);

        uiHwHotkeyListLayoutManager = new LinearLayoutManager(getContext());
        uiHwHotkeyList.setLayoutManager(uiHwHotkeyListLayoutManager);

        uiHwHotkeysListAdapter = new HwHotkeyListAdapter();
        uiHwHotkeyList.setAdapter(uiHwHotkeysListAdapter);

        DB.getInstance().hwhotkeys().getAllObservable().observe(
                getViewLifecycleOwner(), new Observer<List<HwHotkeyEntity>>() {
                    @Override
                    public void onChanged(List<HwHotkeyEntity> hotkeys) {
                        Log.v(TAG, "onChanged() for hw hotkeys");

                        if (hotkeys == null || hotkeys.isEmpty()) {
                            Log.d(TAG, "No hotkeys yet");
                        } else {
                            StringBuilder sb = new StringBuilder("\n");
                            for (HwHotkeyEntity hotkey : hotkeys) {
                                sb.append(">>: ").append(hotkey).append("\n");
                            }
                            Log.i(TAG, sb.toString());
                        }

                        uiHwHotkeysListAdapter.setHwHotkeys(hotkeys);
                    }
                });

        setToolbarTitle("Physical hotkeys");

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.hw_hotkeys, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.uiAddHwHotkeyMenuItem:
                handleAddHwHotkeyButtonClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleAddHwHotkeyButtonClick() {
        FragmentActivity activity = getActivity();

        if (activity == null) {
            Log.w(TAG, "Null activity!?");
            return;
        }

        Log.v(TAG, "Clicked add hw hotkey button");

        NavHostFragment.findNavController(this).navigate(HwHotkeysFragmentDirections
                .actionAddEditHwHotkey(AddEditHwHotkeyFragment.ADD_HW_HOTKEY_MAGIC_ACTION_ID));
    }
}
