package org.docheinstein.minimote.ui.hotkeys;

import android.content.ClipData;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.navigation.fragment.NavHostFragment;

import org.docheinstein.minimote.R;
import org.docheinstein.minimote.ui.base.MinimoteFragment;
import org.docheinstein.minimote.database.DB;
import org.docheinstein.minimote.database.hotkey.HotkeyEntity;
import org.docheinstein.minimote.settings.SettingsManager;
import org.docheinstein.minimote.ui.hotkey.AddEditHotkeyFragment;

import java.util.List;

public class HotkeysFragment extends MinimoteFragment {

    private static final String TAG = "HotkeysFragment";

    private Bundle mSavedState;
    private FrameLayout uiHotkeysContainer;

    private MenuItem mSaveButton;

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
        Log.d(TAG, "onCreateView");

        if(savedInstanceState != null && mSavedState == null) {
            mSavedState = savedInstanceState.getBundle(TAG);
        }

        View view = inflater.inflate(R.layout.hotkeys, container, false);

        uiHotkeysContainer = view.findViewById(R.id.uiHotkeysContainer);
        uiHotkeysContainer.setBackgroundColor(SettingsManager.hotkeysOverlayColor(getContext()));

        DB.getInstance().hotkeys().getAllObservable().observe(
                this, new Observer<List<HotkeyEntity>>() {
                    @Override
                    public void onChanged(List<HotkeyEntity> hotkeys) {
                        Log.v(TAG, "onChanged() for hotkeys");

                        if (hotkeys == null || hotkeys.isEmpty()) {
                            Log.d(TAG, "No hotkeys");
                            return;
                        }

                        uiHotkeysContainer.removeAllViews();

                        final int hotkeysContainerWidth = uiHotkeysContainer.getWidth();
                        for (HotkeyEntity hotkey : hotkeys) {
                            Log.i(TAG, "Hotkey " + hotkey.toString());
                            int xAbs = (int) (hotkeysContainerWidth * hotkey.xRel);

                            TextView h = makeHotkeyView(
                                    String.valueOf(hotkey.id),
                                    hotkey.name,
                                    hotkey.textSize,
                                    hotkey.padding,
                                    xAbs,
                                    (int) hotkey.yAbs
                            );
                            uiHotkeysContainer.addView(h);
                        }

                        if (mSavedState != null) {
                            Log.d(TAG, "Restoring from a saved instance state, updating positions");

                            for (int i = 0; i < uiHotkeysContainer.getChildCount(); i++) {
                                View hotkeyView = uiHotkeysContainer.getChildAt(i);

                                if (!(hotkeyView instanceof TextView)) {
                                    Log.w(TAG, "No text view?");
                                    continue;
                                }

                                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) hotkeyView.getLayoutParams();

                                final int id = Integer.valueOf((String) hotkeyView.getTag());

                                int[] viewPos = mSavedState.getIntArray(String.valueOf(id));
                                if (viewPos != null) {
                                    Log.d(TAG, "Restoring pos for [" + id + "]");
                                    lp.leftMargin = viewPos[0];
                                    lp.topMargin = viewPos[1];
                                    hotkeyView.setLayoutParams(lp);
                                }
                            }
                        }

//                        mSavedState = null;
                    }
                });
        setToolbarTitle("Hotkeys");
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSavedState = saveFragmentState();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(TAG, (mSavedState != null) ? mSavedState : saveFragmentState());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.hotkeys, menu);
        mSaveButton = menu.findItem(R.id.uiSaveHotkeysMenuItem);
        mSaveButton.getIcon().mutate();
        mSaveButton.setEnabled(false);
        mSaveButton.getIcon().mutate().setAlpha(130);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.uiAddHotkeyMenuItem:
                handleAddButtonClick();
                return true;
            case R.id.uiSaveHotkeysMenuItem:
                handleSaveButtonClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Bundle saveFragmentState() {
        Bundle bundle = new Bundle();

        Log.d(TAG, "Fragment will go away, saving state");

        for (int i = 0; i < uiHotkeysContainer.getChildCount(); i++) {
            View hotkeyView = uiHotkeysContainer.getChildAt(i);

            if (!(hotkeyView instanceof TextView)) {
                Log.w(TAG, "No text view?");
                continue;
            }

            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) hotkeyView.getLayoutParams();

            final int id = Integer.valueOf((String) hotkeyView.getTag());
            final int x = lp.leftMargin;
            final int y = lp.topMargin;

            Log.d(TAG, "Saving state of [" + id + "]");
            bundle.putIntArray(String.valueOf(id), new int[] {x, y});
        }

        return bundle;
    }

    private TextView makeHotkeyView(
            final String id,
            String name,
            int textSize,
            int padding,
            int x, int y
    ) {
        Context ctx = getContext();

        if (ctx == null) {
            Log.w(TAG, "Null context!?");
            return null;
        }

        final String idString = String.valueOf(id);

        final TextView hotkeyView = new TextView(ctx);
        hotkeyView.setText(name);
        hotkeyView.setTextSize(textSize);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        lp.leftMargin = x;
        lp.topMargin = y;
        hotkeyView.setLayoutParams(lp);

        hotkeyView.setPadding(padding, padding, padding, padding);

        hotkeyView.setBackground(ContextCompat.getDrawable(ctx, R.drawable.touchpad_button_selector));

        GradientDrawable hotkeyBackground = new GradientDrawable();
        hotkeyBackground.setStroke(3, SettingsManager.hotkeyBorderColor(ctx));
        hotkeyBackground.setCornerRadius(10);
        hotkeyBackground.setColor(SettingsManager.hotkeyUnpressedColor(ctx));

        GradientDrawable hotkeyPressedBackground = new GradientDrawable();
        hotkeyPressedBackground.setStroke(3, SettingsManager.hotkeyBorderColor(ctx));
        hotkeyPressedBackground.setCornerRadius(10);
        hotkeyPressedBackground.setColor(SettingsManager.hotkeyPressedColor(ctx));

        StateListDrawable hotkeySelector = new StateListDrawable();
        hotkeySelector.addState(new int[] {android.R.attr.state_pressed}, hotkeyPressedBackground);
        hotkeySelector.addState(new int[] {}, hotkeyBackground);

        hotkeyView.setBackground(hotkeySelector);
        hotkeyView.setTextColor(SettingsManager.hotkeyTextColor(ctx));

        hotkeyView.setTag(id);

        hotkeyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Click on hotkey [" + v.getTag() + "], opening edit screen");
                NavHostFragment.findNavController(HotkeysFragment.this)
                        .navigate(HotkeysFragmentDirections.actionAddEditHotkey(Integer.valueOf(id)));
            }
        });

        hotkeyView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.v(TAG, "Long click on hotkey [" + v.getTag() + "]");

                v.startDragAndDrop(
                        ClipData.newPlainText(idString, idString),
                        new View.DragShadowBuilder(hotkeyView),
                        null, 0
                );

                return true;
            }
        });

        uiHotkeysContainer.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch(event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        Log.v(TAG, "Action is DragEvent.ACTION_DRAG_STARTED");
                        break;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        Log.v(TAG, "Action is DragEvent.ACTION_DRAG_ENTERED");
                        break;

                    case DragEvent.ACTION_DRAG_EXITED :
                        Log.v(TAG, "Action is DragEvent.ACTION_DRAG_EXITED");
                        break;

                    case DragEvent.ACTION_DRAG_LOCATION  :
                        Log.v(TAG, "Action is DragEvent.ACTION_DRAG_LOCATION");
                        break;

                    case DragEvent.ACTION_DRAG_ENDED   :
                        Log.v(TAG, "Action is DragEvent.ACTION_DRAG_ENDED");
                        break;

                    case DragEvent.ACTION_DROP:
                        Log.d(TAG, "ACTION_DROP event for v: " + v);
                        ClipData.Item item = event.getClipData().getItemAt(0);
                        String dragData = item.getText().toString();
                        Log.d(TAG, "ACTION_DROP event for data: " + dragData);

                        TextView draggedTextView = uiHotkeysContainer.findViewWithTag(dragData);

                        int whalf = draggedTextView.getWidth() / 2;
                        int hhalf = draggedTextView.getHeight() / 2;
                        int x = (int) event.getX();
                        int y = (int) event.getY();
                        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) draggedTextView.getLayoutParams();
                        lp.leftMargin = x - whalf;
                        lp.topMargin = y - hhalf;
                        draggedTextView.setLayoutParams(lp);
                        mSaveButton.setEnabled(true);
                        mSaveButton.getIcon().setAlpha(255);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        return hotkeyView;
    }

    private void handleAddButtonClick() {
        Log.v(TAG, "Clicked on add button");
        NavHostFragment.findNavController(this).navigate(HotkeysFragmentDirections
                .actionAddEditHotkey(AddEditHotkeyFragment.ADD_HOTKEY_MAGIC_ACTION_ID));
    }

    private void handleSaveButtonClick() {
        Log.v(TAG, "Clicked on save button, saving " + uiHotkeysContainer.getChildCount() + " views");

        for (int i = 0; i < uiHotkeysContainer.getChildCount(); i++) {
            View hotkeyView = uiHotkeysContainer.getChildAt(i);

            if (!(hotkeyView instanceof TextView)) {
                Log.w(TAG, "Hotkey not instance of text view?");
                continue;
            }

            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) hotkeyView.getLayoutParams();

            final int id = Integer.valueOf((String) hotkeyView.getTag());
            // Save the X coordinate as relative accordingly to the width of the parent.
            // In this way the disposition of the views is robust to the real container
            // width of the overlay on the controller, despite of orientation, width or similar stuff
            final int xAbs = lp.leftMargin;
            final int yAbs = lp.topMargin;

            final double xRel = (double) xAbs / uiHotkeysContainer.getWidth();

            DB.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    Log.v(TAG, "Updating view [" + id + "] with pos X: " + xRel + ", Y: " + yAbs);
                    DB.getInstance().hotkeys().updatePosition(id, xRel, yAbs);
                }
            });
        }

        goBack();
    }
}