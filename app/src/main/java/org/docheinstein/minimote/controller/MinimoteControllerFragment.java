package org.docheinstein.minimote.controller;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.navigation.fragment.NavHostFragment;

import org.docheinstein.minimote.R;
import org.docheinstein.minimote.base.MinimoteFragment;
import org.docheinstein.minimote.commons.conf.Conf;
import org.docheinstein.minimote.connection.MinimoteConnection;
import org.docheinstein.minimote.controller.keyboard.KeyboardEditText;
import org.docheinstein.minimote.controller.touchpad.TouchpadView;
import org.docheinstein.minimote.database.DB;
import org.docheinstein.minimote.database.hotkey.HotkeyEntity;
import org.docheinstein.minimote.database.server.MinimoteServerEntity;
import org.docheinstein.minimote.edit.EditServerFragment;
import org.docheinstein.minimote.edit.EditServerFragmentArgs;
import org.docheinstein.minimote.keys.MinimoteKeyType;
import org.docheinstein.minimote.packet.MinimotePacket;
import org.docheinstein.minimote.packet.MinimotePacketFactory;
import org.docheinstein.minimote.settings.MinimoteSettings;
import org.docheinstein.minimote.utils.StringUtils;
import org.docheinstein.minimote.utils.ThreadUtils;
import org.docheinstein.minimote.utils.ViewUtils;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class MinimoteControllerFragment extends MinimoteFragment
        implements TouchpadView.TouchpadListener, KeyboardEditText.KeyboardHidingListener {

    public static final int RESULT_CONNECTIVITY = 10;
    public static final int RESULT_CONNECTIVITY_OK = 0;
    public static final int RESULT_CONNECTIVITY_ERROR = -1;

    public static final String EXTRA_SERVER_ADDRESS = "server_address";

    private static final String TAG = "ControllerActivity";

    // Movement
    private static final int MAX_MOVEMENT_SAMPLING_RATE = 60; // Hz
    private static final int MIN_MS_BETWEEN_MOVEMENT_SAMPLES = 1000 / MAX_MOVEMENT_SAMPLING_RATE; // ms

    // Scroll
    private static final int MAX_SCROLL_SAMPLING_RATE = 30; // Hz
    private static final int MIN_MS_BETWEEN_SCROLL_SAMPLES = 1000 / MAX_SCROLL_SAMPLING_RATE; // ms
    private static final int MIN_SCROLL_POS_DELTA_THRESHOLD = 25; // points

    // Click
    private static final int MAX_TOUCHPAD_CLICK_MS_THRESHOLD = 250; // ms
    private static final int MAX_TOUCHPAD_CLICK_POS_THRESHOLD = 25; // points


    private static final int MAX_MOVEMENT_ID = 256;

    private String mServerAddress;

    private ExecutorService mConnectionHandler;
    private MinimoteConnection mConnection;

    private View uiConnectionOverlay;
    private View uiConnectionLoader;
    private TouchpadView uiTouchpad;
    private View uiButtonsContainer;
    private ImageView uiLeftButton;
    private ImageView uiRightButton;
    private View uiHotkeysButton;
    private View uiKeyboardButton;
    private KeyboardEditText uiKeyboardPreview;
    private FrameLayout uiHotkeysOverlay;

    // Movement handling
    private int mCurrentMovementId = 0;
    private long mLastMovementSample = 0;

    // Touchpad click handling
    private long mLastDownTime = 0;
    private int mLastDownX = 0;
    private int mLastDownY = 0;

    // Scroll handling
    private long mLastScrollTime = 0;
    private int mLastScrollY = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.controller, container, false);

        showBackButton();

        Bundle args = getArguments();

        if (args == null) {
            Log.e(TAG, "Invalid args?");
            return null;
        }

        mServerAddress = EditServerFragmentArgs.fromBundle(args).getServerAddress();

        if (!StringUtils.isValid(mServerAddress)) {
            Log.e(TAG, "Invalid server address provided to MinimoteControllerFragment");
            finishWithResult(false);
            return null;
        }

        uiConnectionOverlay = view.findViewById(R.id.uiConnectionOverlay);
        uiConnectionLoader = view.findViewById(R.id.uiConnectionLoader);
        uiTouchpad = view.findViewById(R.id.uiTouchpad);
        uiButtonsContainer = view.findViewById(R.id.uiButtonsContainer);
        uiLeftButton = view.findViewById(R.id.uiLeftButton);
        uiRightButton = view.findViewById(R.id.uiRightButton);
        uiHotkeysOverlay = view.findViewById(R.id.uiHotkeysOverlay);
        uiHotkeysButton = view.findViewById(R.id.uiHotkeysButton);
        uiKeyboardButton = view.findViewById(R.id.uiKeyboardButton);
        uiKeyboardPreview = view.findViewById(R.id.uiKeyboardPreview);

        uiTouchpad.setTouchpadListener(this);

        uiHotkeysButton.setBackgroundTintList(ColorStateList.valueOf(MinimoteSettings.buttonHotkeyColor(getContext())));
        uiKeyboardButton.setBackgroundTintList(ColorStateList.valueOf(MinimoteSettings.buttonKeyboardColor(getContext())));

        uiLeftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Runnable task = null;
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        task = new Runnable() {
                            @Override
                            public void run() {
                                mConnection.sendTcp(MinimotePacketFactory.newLeftDown());
                            }
                        };
                        break;
                    case MotionEvent.ACTION_UP:
                        task = new Runnable() {
                            @Override
                            public void run() {
                                mConnection.sendTcp(MinimotePacketFactory.newLeftUp());
                            }
                        };
                        break;
                }

                if (task != null) {
                    doNetworkOperation(task);
                }

                return false;
            }
        });

        uiRightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Runnable task = null;
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        task = new Runnable() {
                            @Override
                            public void run() {
                                mConnection.sendTcp(MinimotePacketFactory.newRightDown());
                            }
                        };
                        break;
                    case MotionEvent.ACTION_UP:
                        task = new Runnable() {
                            @Override
                            public void run() {
                                mConnection.sendTcp(MinimotePacketFactory.newRightUp());
                            }
                        };
                        break;
                }

                if (task != null) {
                    doNetworkOperation(task);
                }

                return false;
            }
        });

        uiHotkeysButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleHotkeysOverlay();
            }
        });

        uiKeyboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSoftKeyboard();
            }
        });

        uiKeyboardPreview.setListener(this);

        // Text watcher: for soft keyboard chars and backspace
        uiKeyboardPreview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.v(TAG, "onTextChanged: " +
                        "(str: " + s + " - start: " + start + " - " +
                        "before: " + before + " - count: " + count + ")");

                if (count > before) {
                    // Insertion
                    final char c = s.charAt(start + before);
                    Log.d(TAG, "+ " + c);

                    doNetworkOperation(new Runnable() {
                        @Override
                        public void run() {
                            mConnection.sendTcp(MinimotePacketFactory.newType(c));
                        }
                    });
                } else if (before > count) {
                    // Deletion
                    Log.d(TAG, "Backspace detected");

                    doNetworkOperation(new Runnable() {
                        @Override
                        public void run() {
                            mConnection.sendTcp(MinimotePacketFactory.newKeyClick(
                                    MinimoteKeyType.Backspace));
                        }
                    });
                } else {
                    Log.v(TAG, "no-op");
                }
            }
        });

        // Key listener: for physical keyboards and special chars on soft keyboards
        uiKeyboardPreview.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.v(TAG, "onKey(): " + keyCode);

                if (event == null) {
                    Log.w(TAG, "Null event?");
                    return false;
                }

                final MinimoteKeyType keyType = MinimoteKeyType.fromAndroidKeyCode(keyCode);

                if (keyType == null) {
                    Log.w(TAG, "Unsupported keycode (" + keyCode + ")");
                    return false;
                }

                Log.v(TAG, "AndroidKeyCode " + keyCode + " = MinimoteKeyType " + keyType.getValue());

                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    doNetworkOperation(new Runnable() {
                        @Override
                        public void run() {
                            mConnection.sendTcp(MinimotePacketFactory.newKeyDown(keyType));
                        }
                    });
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    doNetworkOperation(new Runnable() {
                        @Override
                        public void run() {
                            mConnection.sendTcp(MinimotePacketFactory.newKeyUp(keyType));
                        }
                    });
                }

                return true;
            }
        });

        fillHotkeysOverlay();

        mConnectionHandler = Executors.newScheduledThreadPool(2);

        DB.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                // Retrieve the entity
                final MinimoteServerEntity server = DB.getInstance().minimoteServerDao().getByAddress(mServerAddress);

                if (server == null) {
                    Log.w(TAG, "Cannot retrieve server entity with address: " + mServerAddress);
//                    finishWithResultOnUiThread(false);
                    return;
                }

                ui(new Runnable() {
                    @Override
                    public void run() {
//                        Activity a = getActivity();
//                        if (a != null)
//                            a.setTitle(StringUtils.firstValid(server.displayName, server.hostname, server.address));
                    }
                });
            }
        });

        setToolbarTitle(mServerAddress);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "Destroying controller for server with address: " + mServerAddress);

        doNetworkOperation(new Runnable() {
            @Override
            public void run() {
                if (mConnection != null) {
                    mConnection.disconnect();
                    mConnection = null;
                }
            }
        });

        finishWithResult(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "MinimoteControllerFragment.onResume()");

        updateUI();

        if (!StringUtils.isValid(mServerAddress)) {
            Log.e(TAG, "Invalid server address");
            finishWithResult(false);
            return;
        }

        if (mConnection == null) {
            Log.v(TAG, "Initializing MinimoteConnection");
            mConnection = new MinimoteConnection(mServerAddress, Conf.TCP_PORT, Conf.UDP_PORT);
        }

        if (!mConnection.isConnected()) {
            Log.d(TAG, "Minimote connection is not connected yet, doing so");

            doNetworkOperation(new Runnable() {
                @Override
                public void run() {
                    if (!mConnection.connect()) {
                        Log.e(TAG, "Connection with the server failed, quitting activity");
                        finishWithResultOnUiThread(false);
                        return;
                    }

//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

                    ui(new Runnable() {
                        @Override
                        public void run() {
                            // Connected
                            Log.i(TAG, "Connected to the server, enabling UI");
                            updateUI();
                        }
                    });
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.v(TAG, "Back button pressed");
            finishWithResult(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTouchpadDown(MotionEvent ev) {
        mLastDownTime = ev.getEventTime();
        mLastDownX = (int) ev.getX();
        mLastDownY = (int) ev.getY();
    }

    @Override
    public void onTouchpadPointerDown(MotionEvent ev) {
        mLastScrollTime = ev.getEventTime();
        mLastScrollY = (int) ev.getY();
    }

    @Override
    public void onTouchpadUp(MotionEvent ev) {
        mCurrentMovementId = (mCurrentMovementId + 1) % MAX_MOVEMENT_ID;
        Log.v(TAG, "New movement ID: " + mCurrentMovementId);

        if (ev.getEventTime() - mLastDownTime < MAX_TOUCHPAD_CLICK_MS_THRESHOLD &&
            Math.abs(ev.getX() - mLastDownX) < MAX_TOUCHPAD_CLICK_POS_THRESHOLD &&
            Math.abs(ev.getY() - mLastDownY) < MAX_TOUCHPAD_CLICK_POS_THRESHOLD) {
            Log.v(TAG, "Sampled click at position (X: " + ev.getX() + ", Y: " + ev.getY() + ")");
            doNetworkOperation(new Runnable() {
                @Override
                public void run() {
                    mConnection.sendTcp(MinimotePacketFactory.newLeftClick());
                }
            });
        }
    }

    @Override
    public void onTouchpadPointerUp(MotionEvent ev) {
        mCurrentMovementId = (mCurrentMovementId + 1) % MAX_MOVEMENT_ID;
        Log.v(TAG, "New movement ID: " + mCurrentMovementId);
    }


    @Override
    public void onTouchpadMovement(MotionEvent ev) {
        long eventTime = ev.getEventTime();
        if (ev.getPointerCount() > 1) {
            Log.v(TAG, "Maybe a scroll?");

            // Scroll
            if (eventTime - mLastScrollTime < MIN_MS_BETWEEN_SCROLL_SAMPLES) {
                Log.d(TAG, "Discarding scroll sample, too fast (" +
                        (eventTime - mLastScrollTime) + "ms)");
                return;
            }

            MinimotePacket scrollpacket = null;
            int deltaScroll = (int) (mLastScrollY - ev.getY());

            Log.v(TAG, "deltaScroll: " + deltaScroll);

            if (deltaScroll > MIN_SCROLL_POS_DELTA_THRESHOLD)
                scrollpacket = MinimotePacketFactory.newScrollUp();
            else if (deltaScroll < -MIN_SCROLL_POS_DELTA_THRESHOLD)
                scrollpacket = MinimotePacketFactory.newScrollDown();

            if (scrollpacket != null) {
                final MinimotePacket validScrollpacket = scrollpacket;

                Log.v(TAG, "Sampled scroll of type: " + (deltaScroll > 0 ? "upward" : "downward"));

                mLastScrollTime = eventTime;
                mLastScrollY = (int) ev.getY();

                doNetworkOperation(new Runnable() {
                    @Override
                    public void run() {
                        mConnection.sendUdp(validScrollpacket);
                    }
                });
            }
        } else {
            // Move
            if (eventTime - mLastMovementSample < MIN_MS_BETWEEN_MOVEMENT_SAMPLES) {
                Log.d(TAG, "Discarding movement sample, too fast" +
                        (eventTime - mLastMovementSample) + "ms)");
                return;
            }

            mLastMovementSample = eventTime;

            final int x = (int) ev.getX();
            final int y = (int) ev.getY();

            Log.v(TAG, "Sampled movement " +
                    "(MID: " + mCurrentMovementId + ", X: " + x + ", Y: " + y + ")");

            doNetworkOperation(new Runnable() {
                @Override
                public void run() {
                    mConnection.sendUdp(MinimotePacketFactory.newMove(mCurrentMovementId, x, y));
                }
            });
        }
    }

    private void fillHotkeysOverlay() {
        DB.getInstance().hotkeyEntityDao().getAllObservable().observe(
                this,
                new Observer<List<HotkeyEntity>>() {
                    @Override
                    public void onChanged(List<HotkeyEntity> hotkeys) {
                        if (hotkeys == null) {
                            Log.w(TAG, "No hotkeys?");
                            return;
                        }

                        Log.d(TAG, "There are " + hotkeys.size() + " hotkeys");

                        final int hotkeysContainerWidth = uiHotkeysOverlay.getWidth();

                        for (final HotkeyEntity hotkey : hotkeys) {
                            Log.v(TAG, "Hotkey: " + hotkey);
                            int xAbs = (int) (hotkeysContainerWidth * hotkey.xRel);

                            final TextView hotkeyView = new TextView(getContext());
                            hotkeyView.setText(hotkey.name);
                            hotkeyView.setTextSize(hotkey.textSize);
                            hotkeyView.setTextColor(MinimoteSettings.hotkeyTextColor(getContext()));
//                            hotkeyView.setBackgroundColor(hotkey.backgroundColor);
                            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.WRAP_CONTENT,
                                    FrameLayout.LayoutParams.WRAP_CONTENT
                            );
                            lp.leftMargin = xAbs;
                            lp.topMargin = (int) hotkey.yAbs;
                            hotkeyView.setLayoutParams(lp);
                            hotkeyView.setPadding(hotkey.padding, hotkey.padding, hotkey.padding, hotkey.padding);

                            hotkeyView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.touchpad_button_selector));

                            hotkeyView.setTag(hotkey.id);

                            hotkeyView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.v(TAG, "Clicked on hotkey " + hotkey);
                                    MinimoteKeyType baseKey = MinimoteKeyType.fromString(hotkey.key);
                                    if (baseKey == null)
                                        return;

                                    List<MinimoteKeyType> keys = new ArrayList<>();
                                    if (hotkey.shift)
                                        keys.add(MinimoteKeyType.ShiftLeft);
                                    if (hotkey.ctrl)
                                        keys.add(MinimoteKeyType.CtrlLeft);
                                    if (hotkey.alt)
                                        keys.add(MinimoteKeyType.AltLeft);
                                    if (hotkey.altgr)
                                        keys.add(MinimoteKeyType.AltGr);
                                    if (hotkey.meta)
                                        keys.add(MinimoteKeyType.MetaLeft);
                                    keys.add(baseKey);

                                    final MinimotePacket packet = MinimotePacketFactory.newHotkey(keys);
                                    doNetworkOperation(new Runnable() {
                                        @Override
                                        public void run() {
                                            mConnection.sendTcp(packet);
                                        }
                                    });
                                }
                            });

                            GradientDrawable hotkeyBackground = new GradientDrawable();
                            hotkeyBackground.setStroke(3, MinimoteSettings.hotkeyBorderColor(getContext()));
                            hotkeyBackground.setCornerRadius(10);
                            hotkeyBackground.setColor(MinimoteSettings.hotkeyBackgroundColor(getContext()));

                            GradientDrawable hotkeyPressedBackground = new GradientDrawable();
                            hotkeyPressedBackground.setStroke(3, MinimoteSettings.hotkeyBorderColor(getContext()));
                            hotkeyPressedBackground.setCornerRadius(10);
                            hotkeyPressedBackground.setColor(MinimoteSettings.hotkeyPressedBackgroundColor(getContext()));


                            StateListDrawable hotkeySelector = new StateListDrawable();
                            hotkeySelector.addState(new int[] {android.R.attr.state_pressed}, hotkeyPressedBackground);
                            hotkeySelector.addState(new int[] {}, hotkeyBackground);

                            hotkeyView.setBackground(hotkeySelector);

                            ui(new Runnable() {
                                @Override
                                public void run() {
                                    uiHotkeysOverlay.addView(hotkeyView);
                                }
                            });
                        }
                    }
                }
        );

    }

    private void updateUI() {
        if (mConnection != null && mConnection.isConnected()) {
            uiConnectionLoader.setVisibility(View.GONE);
            uiConnectionOverlay.setVisibility(View.GONE);
            uiTouchpad.setEnabled(true);
            uiLeftButton.setEnabled(true);
            uiRightButton.setEnabled(true);
            uiKeyboardButton.setEnabled(true);
            uiHotkeysOverlay.setEnabled(true);
            uiHotkeysButton.setEnabled(true);
        } else {
            uiConnectionLoader.setVisibility(View.VISIBLE);
            uiConnectionOverlay.setVisibility(View.VISIBLE);
            uiTouchpad.setEnabled(false);
            uiLeftButton.setEnabled(false);
            uiRightButton.setEnabled(false);
            uiKeyboardButton.setEnabled(false);
            uiHotkeysOverlay.setEnabled(true);
            uiHotkeysButton.setEnabled(true);
        }

        // Update UI accordingly to prefs too

        ViewUtils.show(uiHotkeysOverlay, MinimoteSettings.openHotkeys(getContext()));

        if (MinimoteSettings.openKeyboard(getContext())) {
            showSoftKeyboard();
        }

        ViewUtils.show(uiButtonsContainer, MinimoteSettings.showTouchpadButtons(getContext()));

        uiTouchpad.setBackgroundColor(MinimoteSettings.touchpadColor(getContext()));

        // Touchpad
        GradientDrawable overlayBackground = new GradientDrawable();
        overlayBackground.setStroke(3, MinimoteSettings.hotkeysOverlayBorderColor(getContext()));
        overlayBackground.setCornerRadius(10);
        overlayBackground.setColor(MinimoteSettings.hotkeysOverlayColor(getContext()));
        uiHotkeysOverlay.setBackground(overlayBackground);

        // Touchpad buttons container
        uiButtonsContainer.setBackgroundColor(MinimoteSettings.touchpadButtonsContainerColor(getContext()));

        // Touchpad buttons
        GradientDrawable touchpadButtonBackground = new GradientDrawable();
        touchpadButtonBackground.setStroke(3, MinimoteSettings.touchpadButtonsBorderColor(getContext()));
        touchpadButtonBackground.setCornerRadius(10);
        touchpadButtonBackground.setColor(MinimoteSettings.touchpadButtonsColor(getContext()));

        GradientDrawable touchpadButtonPressedBackground = new GradientDrawable();
        touchpadButtonPressedBackground.setStroke(3, MinimoteSettings.touchpadButtonsBorderColor(getContext()));
        touchpadButtonPressedBackground.setCornerRadius(10);
        touchpadButtonPressedBackground.setColor(MinimoteSettings.touchpadButtonsPressedColor(getContext()));


        StateListDrawable touchpadButtonSelectorL = new StateListDrawable();
        touchpadButtonSelectorL.addState(new int[] {android.R.attr.state_pressed}, touchpadButtonPressedBackground);
        touchpadButtonSelectorL.addState(new int[] {}, touchpadButtonBackground);

        StateListDrawable touchpadButtonSelectorR = new StateListDrawable();
        touchpadButtonSelectorR.addState(new int[] {android.R.attr.state_pressed}, touchpadButtonPressedBackground);
        touchpadButtonSelectorR.addState(new int[] {}, touchpadButtonBackground);

        uiLeftButton.setBackground(touchpadButtonSelectorL);
        uiRightButton.setBackground(touchpadButtonSelectorR);
    }

    private void toggleHotkeysOverlay() {
        if (uiHotkeysOverlay.getVisibility() != View.VISIBLE)
            uiHotkeysOverlay.setVisibility(View.VISIBLE);
        else
            uiHotkeysOverlay.setVisibility(View.GONE);
    }

    private void toggleSoftKeyboard() {
        if (ViewUtils.isShown(uiKeyboardPreview))
            hideKeyboard(getActivity());
        else
            showSoftKeyboard();

    }

    private void showSoftKeyboard() {
        uiKeyboardPreview.setVisibility(View.VISIBLE);

        if (!uiKeyboardPreview.requestFocus()) {
            Log.w(TAG, "Failed to acquire focus on keyboard preview");
            return;
        }

        Activity a = getActivity();
        if (a == null) {
            Log.w(TAG, "Null activity?");
            return;
        }

        InputMethodManager imm = (InputMethodManager) a.getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(uiKeyboardPreview, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private Future doNetworkOperation(Runnable runnable) {
        return mConnectionHandler.submit(runnable);
    }

    private void finishWithResultOnUiThread(final boolean success) {
        ui(new Runnable() {
            @Override
            public void run() {
                finishWithResult(success);
            }
        });
    }

    private void finishWithResult(boolean success) {
//        setResult(success ? RESULT_CONNECTIVITY_OK : RESULT_CONNECTIVITY_ERROR);
        goBack();
    }

    @Override
    public void onKeyboardHidden() {
        uiKeyboardPreview.setVisibility(View.GONE);
    }
}
