package org.docheinstein.minimote.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import org.docheinstein.minimote.R;
import org.docheinstein.minimote.base.MinimoteFragment;
import org.docheinstein.minimote.connection.MinimoteConnection;
import org.docheinstein.minimote.controller.keyboard.AutoHideEditText;
import org.docheinstein.minimote.controller.touchpad.TouchpadView;
import org.docheinstein.minimote.database.DB;
import org.docheinstein.minimote.database.hotkey.HotkeyEntity;
import org.docheinstein.minimote.database.server.MinimoteServerEntity;
import org.docheinstein.minimote.edit.EditServerFragmentArgs;
import org.docheinstein.minimote.keys.MinimoteKeyType;
import org.docheinstein.minimote.packet.MinimotePacket;
import org.docheinstein.minimote.packet.MinimotePacketFactory;
import org.docheinstein.minimote.packet.MinimotePacketType;
import org.docheinstein.minimote.settings.SettingsManager;
import org.docheinstein.minimote.utils.StringUtils;
import org.docheinstein.minimote.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.docheinstein.minimote.commons.Conf.Controller.MAX_TOUCHPAD_CLICK_MS_THRESHOLD;
import static org.docheinstein.minimote.commons.Conf.Controller.MAX_TOUCHPAD_CLICK_POS_THRESHOLD;
import static org.docheinstein.minimote.commons.Conf.Controller.MIN_MS_BETWEEN_MOVEMENT_SAMPLES;
import static org.docheinstein.minimote.commons.Conf.Controller.MIN_MS_BETWEEN_SCROLL_SAMPLES;
import static org.docheinstein.minimote.commons.Conf.Controller.MIN_SCROLL_POS_DELTA_THRESHOLD;

public class MinimoteControllerFragment extends MinimoteFragment
        implements
        TouchpadView.TouchpadListener, TextWatcher, View.OnKeyListener {

    public static final String RESULT_KEY_CONNECTIVITY =
            "minimote_controller_fragment_result_connectivity";

    public static final String RESULT_KEY_SERVER_ADDRESS =
            "minimote_controller_fragment_result_server_address";

    public static final int RESULT_VALUE_CONNECTIVITY_OK = 1;
    public static final int RESULT_VALUE_CONNECTIVITY_ERROR = -1;

    private static final String TAG = "ControllerActivity";

    private static final int MAX_MOVEMENT_ID = 256;

    private String mServerAddress;
    private Integer mServerPort;

    private ExecutorService mConnectionHandler;
    private MinimoteConnection mConnection;

    private View uiConnectionOverlay;
    private View uiConnectionLoader;
    private TouchpadView uiTouchpad;
    private View uiTouchpadButtonsContainer;
    private ImageView uiTouchpadLeftButton;
    private ImageView uiTouchpadRightButton;
    private View uiHotkeysButton;
    private View uiKeyboardButton;
    private AutoHideEditText uiKeyboardPreview;
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

        Bundle args = getArguments();

        if (args == null) {
            Log.e(TAG, "Invalid args?");
            return null;
        }

        mServerAddress = EditServerFragmentArgs.fromBundle(args).getServerAddress();
        mServerPort = EditServerFragmentArgs.fromBundle(args).getServerPort();

        if (!StringUtils.isValid(mServerAddress)) {
            Log.e(TAG, "Invalid server address provided to MinimoteControllerFragment");
            finishWithResult(false);
            return null;
        }

        mConnectionHandler = Executors.newScheduledThreadPool(2);

        uiConnectionOverlay = view.findViewById(R.id.uiConnectionOverlay);
        uiConnectionLoader = view.findViewById(R.id.uiConnectionLoader);
        uiTouchpad = view.findViewById(R.id.uiTouchpad);
        uiTouchpadButtonsContainer = view.findViewById(R.id.uiTouchpadButtonsContainer);
        uiTouchpadLeftButton = view.findViewById(R.id.uiTouchpadLeftButton);
        uiTouchpadRightButton = view.findViewById(R.id.uiTouchpadRightButton);
        uiHotkeysOverlay = view.findViewById(R.id.uiHotkeysOverlay);
        uiHotkeysButton = view.findViewById(R.id.uiHotkeysButton);
        uiKeyboardButton = view.findViewById(R.id.uiKeyboardButton);
        uiKeyboardPreview = view.findViewById(R.id.uiKeyboardPreview);

        // Touchpad
        uiTouchpad.setTouchpadListener(this);

        // Touchpad buttons
        initTouchpadButton(uiTouchpadLeftButton, MinimotePacketType.LeftDown, MinimotePacketType.LeftUp);
        initTouchpadButton(uiTouchpadRightButton, MinimotePacketType.RightDown, MinimotePacketType.RightUp);

        // FABs
        uiHotkeysButton.setBackgroundTintList(
                ColorStateList.valueOf(SettingsManager.buttonHotkeyColor(getContext())));
        uiKeyboardButton.setBackgroundTintList(
                ColorStateList.valueOf(SettingsManager.buttonKeyboardColor(getContext())));

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

        // Keyboard preview

        // Text watcher: for soft keyboard chars and backspace
        uiKeyboardPreview.addTextChangedListener(this);

        // Key listener: for physical keyboards and special chars on soft keyboards
        uiKeyboardPreview.setOnKeyListener(this);

        fillHotkeysOverlay();
        updateToolbarTitleUsingServerInfo();

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
            mConnection = new MinimoteConnection(mServerAddress, mServerPort, mServerPort);
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


    // Keyboard listeners

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
                    mConnection.sendTcp(
                            MinimotePacketFactory.newType(c));
                }
            });
        } else if (before > count) {
            // Deletion
            Log.d(TAG, "Backspace detected");

            doNetworkOperation(new Runnable() {
                @Override
                public void run() {
                    mConnection.sendTcp(
                            MinimotePacketFactory.newKeyClick(
                                    MinimoteKeyType.Backspace));
                }
            });
        } else {
            Log.v(TAG, "no-op");
        }
    }

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

    private void fillHotkeysOverlay() {
        DB.getInstance().hotkeys().getAllObservable().observe(
            this,
            new Observer<List<HotkeyEntity>>() {
                @Override
                public void onChanged(List<HotkeyEntity> hotkeys) {
                    if (hotkeys == null) {
                        Log.w(TAG, "No hotkeys?");
                        return;
                    }

                    Context ctx = getContext();

                    if (ctx == null) {
                        Log.w(TAG, "Null context?");
                        return;
                    }

                    Log.d(TAG, "There are " + hotkeys.size() + " hotkeys");

                    final int hotkeysContainerWidth = uiHotkeysOverlay.getMeasuredWidth();

                    Log.d(TAG, "Container width: " + hotkeysContainerWidth);

                    for (final HotkeyEntity hotkey : hotkeys) {
                        Log.v(TAG, "Found hotkey: " + hotkey);
                        int xAbs = (int) (hotkeysContainerWidth * hotkey.xRel);

                        // Style the hotkey according to the preferences

                        final TextView hotkeyView = new TextView(ctx);
                        hotkeyView.setText(hotkey.name);
                        hotkeyView.setTextSize(hotkey.textSize);
                        hotkeyView.setTextColor(SettingsManager.hotkeyTextColor(ctx));

                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                        );
                        lp.leftMargin = xAbs;
                        lp.topMargin = (int) hotkey.yAbs;
                        hotkeyView.setLayoutParams(lp);

                        hotkeyView.setPadding(
                                hotkey.padding, hotkey.padding,
                                hotkey.padding, hotkey.padding);

                        hotkeyView.setBackground(ContextCompat.getDrawable(ctx, R.drawable.touchpad_button_selector));

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

                        // Hotkey pressed/unpressed selector

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
        boolean isConnected = mConnection != null && mConnection.isConnected();

        ViewUtils.setVisibility(uiConnectionLoader, !isConnected);
        ViewUtils.setVisibility(uiConnectionOverlay, !isConnected);
        uiTouchpad.setEnabled(isConnected);
        uiTouchpadLeftButton.setEnabled(isConnected);
        uiTouchpadRightButton.setEnabled(isConnected);
        uiKeyboardButton.setEnabled(isConnected);
        uiHotkeysOverlay.setEnabled(isConnected);
        uiHotkeysButton.setEnabled(isConnected);

        // Update UI accordingly to prefs too

        // Use View.INVISIBILE instead of View.GONE in order to be able to measeure the width
        // of the container, for compute the absolute x coordinate of the hotkeys
        ViewUtils.setVisibility(uiHotkeysOverlay, SettingsManager.openHotkeys(getContext()), View.INVISIBLE);

        if (SettingsManager.openKeyboard(getContext())) {
            showSoftKeyboard(uiKeyboardPreview);
        }

        ViewUtils.setVisibility(
                uiTouchpadButtonsContainer,
                SettingsManager.showTouchpadButtons(getContext())
        );

        // Touchpad
        uiTouchpad.setBackgroundColor(SettingsManager.touchpadColor(getContext()));

        GradientDrawable overlayBackground = new GradientDrawable();
        overlayBackground.setStroke(3, SettingsManager.hotkeysOverlayBorderColor(getContext()));
        overlayBackground.setCornerRadius(10);
        overlayBackground.setColor(SettingsManager.hotkeysOverlayColor(getContext()));
        uiHotkeysOverlay.setBackground(overlayBackground);

        // Touchpad buttons container
        uiTouchpadButtonsContainer.setBackgroundColor(
                SettingsManager.touchpadButtonsContainerColor(getContext()));

        // Touchpad buttons
        GradientDrawable touchpadButtonBackground = new GradientDrawable();
        touchpadButtonBackground.setStroke(3, SettingsManager.touchpadButtonsBorderColor(getContext()));
        touchpadButtonBackground.setCornerRadius(10);
        touchpadButtonBackground.setColor(SettingsManager.touchpadButtonsColor(getContext()));

        GradientDrawable touchpadButtonPressedBackground = new GradientDrawable();
        touchpadButtonPressedBackground.setStroke(3, SettingsManager.touchpadButtonsBorderColor(getContext()));
        touchpadButtonPressedBackground.setCornerRadius(10);
        touchpadButtonPressedBackground.setColor(SettingsManager.touchpadButtonsPressedColor(getContext()));


        StateListDrawable touchpadButtonSelectorL = new StateListDrawable();
        touchpadButtonSelectorL.addState(new int[] {android.R.attr.state_pressed}, touchpadButtonPressedBackground);
        touchpadButtonSelectorL.addState(new int[] {}, touchpadButtonBackground);

        StateListDrawable touchpadButtonSelectorR = new StateListDrawable();
        touchpadButtonSelectorR.addState(new int[] {android.R.attr.state_pressed}, touchpadButtonPressedBackground);
        touchpadButtonSelectorR.addState(new int[] {}, touchpadButtonBackground);

        uiTouchpadLeftButton.setBackground(touchpadButtonSelectorL);
        uiTouchpadRightButton.setBackground(touchpadButtonSelectorR);
    }

    private void toggleHotkeysOverlay() {
        ViewUtils.toggleVisibility(uiHotkeysOverlay);
    }

    private void toggleSoftKeyboard() {
        if (ViewUtils.isShown(uiKeyboardPreview))
            hideKeyboard();
        else
            showSoftKeyboard(uiKeyboardPreview);
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
        Bundle args = new Bundle();
        args.putInt(RESULT_KEY_CONNECTIVITY,
                success ?
                        RESULT_VALUE_CONNECTIVITY_OK :
                        RESULT_VALUE_CONNECTIVITY_ERROR);
        args.putString(RESULT_KEY_SERVER_ADDRESS, mServerAddress);
        goBack(args);
    }

    private void updateToolbarTitleUsingServerInfo() {
        // Retrieves the server details, order to use the display name or hostname
        // for the toolbar, if available
        DB.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                // Retrieve the entity
                final MinimoteServerEntity server =
                        DB.getInstance().servers().get(mServerAddress, mServerPort);

                if (server == null) {
                    Log.w(TAG, "Cannot retrieve server entity with address: " + mServerAddress + ":" + mServerPort);
                    finishWithResultOnUiThread(false);
                    return;
                }

                ui(new Runnable() {
                    @Override
                    public void run() {
                        setToolbarTitle(StringUtils.firstValid(
                                server.displayName,
                                server.hostname,
                                server.address
                        ));
                    }
                });
            }
        });

    }

    private void initTouchpadButton(View button,
                                    final MinimotePacketType downType,
                                    final MinimotePacketType upType) {
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Runnable task = null;
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        task = new Runnable() {
                            @Override
                            public void run() {
                                mConnection.sendTcp(new MinimotePacket(downType));
                            }
                        };
                        break;
                    case MotionEvent.ACTION_UP:
                        task = new Runnable() {
                            @Override
                            public void run() {
                                mConnection.sendTcp(new MinimotePacket(upType));
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
    }
}
