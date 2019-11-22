package org.docheinstein.minimote.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.docheinstein.minimote.R;
import org.docheinstein.minimote.commons.conf.Conf;
import org.docheinstein.minimote.connection.MinimoteConnection;
import org.docheinstein.minimote.controller.touchpad.TouchpadView;
import org.docheinstein.minimote.packet.MinimotePacketFactory;
import org.docheinstein.minimote.utils.StringUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MinimoteControllerActivity extends AppCompatActivity implements TouchpadView.TouchpadListener {

    public static final String EXTRA_SERVER_ADDRESS = "server_address";

    private static final String TAG = "ControllerActivity";

    private static final int MAX_MOVEMENT_SAMPLING_RATE = 60;
    private static final int MIN_MS_BETWEEN_MOVEMENT_SAMPLES = 1000 / 60;

    private static final int MAX_MOVEMENT_ID = 256;

    private String mAddress;

    private ExecutorService mConnectionHandler;
    private MinimoteConnection mConnection;

    private TouchpadView uiTouchpad;

    private int mCurrentMovementId = 0;
    private long mLastMovementSample = 0;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller);

        Intent i = getIntent();

        if (i == null) {
            Log.e(TAG, "Invalid intent?");
            finish();
            return;
        }

        mAddress = i.getStringExtra(EXTRA_SERVER_ADDRESS);

        if (!StringUtils.isValid(mAddress)) {
            Log.e(TAG, "Invalid server address provided to MinimoteControllerActivity");
            finish();
            return;
        }

        uiTouchpad = findViewById(R.id.uiTouchpad);

        uiTouchpad.setTouchpadListener(this);

        mConnectionHandler = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "MinimoteControllerActivity.onResume()");

        if (mConnection == null) {
            Log.v(TAG, "Initializing MinimoteConnection");
            mConnection = new MinimoteConnection(mAddress, Conf.TCP_PORT, Conf.UDP_PORT);
        }

        if (!mConnection.isConnected()) {
            Log.d(TAG, "Minimote connection is not connected yet, doing so");

            doNetworkOperation(new Runnable() {
                @Override
                public void run() {
                    if (!mConnection.connect()) {
                        Log.e(TAG, "Connection with the server failed, quitting activity");
                        finishOnUiThread();
                    }
                }
            });
        }
    }

    @Override
    public void onTouchpadDown(MotionEvent ev) {

    }

    @Override
    public void onTouchpadUp(MotionEvent ev) {
        mCurrentMovementId = (mCurrentMovementId + 1) % MAX_MOVEMENT_ID;
    }

    @Override
    public void onTouchpadMovement(MotionEvent ev) {
        long eventTime = ev.getEventTime();
        if (ev.getPointerCount() > 1) {
            // Scroll
        } else {
            // Move
            if (eventTime - mLastMovementSample < MIN_MS_BETWEEN_MOVEMENT_SAMPLES) {
                Log.d(TAG, "Discarding movement sample, too fast");
                return;
            }

            mLastMovementSample = eventTime;

            final int x = (int) ev.getX();
            final int y = (int) ev.getY();

            Log.v(TAG, "Sampled movement " +
                    "(MID: " + mCurrentMovementId + ", X: " + x + ", Y: " + y);

            doNetworkOperation(new Runnable() {
                @Override
                public void run() {
                    mConnection.sendUdp(MinimotePacketFactory.newMove(mCurrentMovementId, x, y));
                }
            });
        }
    }

    @Override
    public void onTouchpadPointerUp(MotionEvent ev) {

    }

    @Override
    public void onTouchpadPointerDown(MotionEvent ev) {

    }

    private Future doNetworkOperation(Runnable runnable) {
        return mConnectionHandler.submit(runnable);
    }

    private void finishOnUiThread() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
    }
}
