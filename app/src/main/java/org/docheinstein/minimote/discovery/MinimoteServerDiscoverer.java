package org.docheinstein.minimote.discovery;

import android.os.StrictMode;
import android.util.Log;

import org.docheinstein.minimote.commons.conf.Conf;
import org.docheinstein.minimote.packet.MinimotePacket;
import org.docheinstein.minimote.packet.MinimotePacketFactory;
import org.docheinstein.minimote.packet.MinimotePacketType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MinimoteServerDiscoverer {
    private static final String TAG = "MinimoteServerDiscover";

    private static final int MAX_BUFFER_SIZE = 64;

    private final Object mDiscoveryLock = new Object();

    private Future mDiscoveryTask;
    private ExecutorService mDiscoveryExecutor;
    private DatagramSocket mSocket;
    private int mPort;

    private MinimoteServerDiscovererListener mListener;

    public interface MinimoteServerDiscovererListener {
        void onDiscoveryStarted();
        void onDiscoveryFinished(boolean success);
        void onServerDiscovered(MinimoteDiscoveredServer server);
    }

    public MinimoteServerDiscoverer(MinimoteServerDiscovererListener listener, int port) {
        if (mListener == null)
            Log.w(TAG, "Listener should not be null");

        mListener = listener;
        mPort = port;
        mDiscoveryExecutor = Executors.newSingleThreadExecutor();

        Log.d(TAG , "MinimoteServerDiscoverer initialized");
    }

    public void startDiscovery(int timeout) {
        synchronized (mDiscoveryLock) {
            Log.v(TAG, "Locked discovery lock on startDiscovery()");
            if (!initSocket(timeout)) {
                Log.e(TAG, "Failed to initialize socket, not starting discovery");
                return;
            }

            // First of all, listen to answer
            mDiscoveryTask = mDiscoveryExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    startListeningDiscoverResponses();
                }
            });

            // Ensure that we are listening
            // TODO: are we actually listening right now? Shall we introduce a sleep()?

            // Now that we are listening, we can broadcast the request
            if (!broadcastDiscoverRequest()) {
                Log.w(TAG, "Broadcast failed, discovery cannot proceed");
                cleanup();
                return;
            }

            // Notify that the discovery is started
            if (mListener != null)
                mListener.onDiscoveryStarted();

            Log.v(TAG, "Unlocked discovery lock on startDiscovery()");
        }
    }

    public void stopDiscovery() {
        synchronized (mDiscoveryLock) {
            Log.v(TAG, "Discovery abort required");
            Log.v(TAG, "Locked discovery lock on stopDiscovery()");
            cleanup();
            Log.v(TAG, "Discovery abort completed");
            Log.v(TAG, "Unlocked discovery lock on stopDiscovery()");
        }
    }

    private boolean initSocket(int timeout) {
        boolean success = false;

        cleanup();

        try {
            InetAddress inet = InetAddress.getByName("0.0.0.0");
            // Create unbound socket
            mSocket = new DatagramSocket(null /* unbound */);
            mSocket.setReuseAddress(true);
            // Bind it after setReuseAddr()
            mSocket.bind(new InetSocketAddress(inet, mPort));
            mSocket.setBroadcast(true);
            mSocket.setSoTimeout(timeout);
            success = true;
        } catch (SocketException e) {
            Log.e(TAG, "Unable to allocate UDP socket", e);
        } catch (UnknownHostException e) {
            Log.e(TAG, "Unable to perform InetAddress.getByName()", e);
        }

        return success;
    }


    private void cleanup() {
        if (mSocket != null)
            mSocket.close();
        mSocket = null;

        if (mDiscoveryTask != null)
            mDiscoveryTask.cancel(true);
        mDiscoveryTask = null;

    }

    private void startListeningDiscoverResponses() {
        Log.d(TAG, "Discovery started on port " + mPort + ", waiting for responses...");

        boolean success = false;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                byte[] buf = new byte[MAX_BUFFER_SIZE];

                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                mSocket.receive(packet);
                Log.d(TAG, "Received packet from " +
                        packet.getAddress().getHostAddress() + ":" + packet.getPort());
                handleDiscoverResponse(packet);
            } catch (SocketTimeoutException | SocketException ste) {
                // Expected exception, when the timeout elapses
                Log.d(TAG, "Socket timeout elapsed, stopping discovery");
                success = true;
                break;
            } catch (IOException e) {
                break;
            }
        }

        Log.d(TAG, "Exiting discovery");

        // Notify that the discovery is finished, with or without errors
        if (mListener != null)
            mListener.onDiscoveryFinished(success);
    }

    private void handleDiscoverResponse(DatagramPacket rawDiscoverResponse) {
        MinimotePacket discoverResponse = MinimotePacket.fromData(rawDiscoverResponse.getData());

        if (discoverResponse == null) {
            Log.w(TAG, "Received packet is not a valid minimote packet");
            return;
        }

        if (discoverResponse.getEventType() != MinimotePacketType.DiscoverResponse) {
            Log.w(TAG, "Received packet is not a DISCOVER_RESPONSE");
            return;
        }

        Log.v(TAG, "-- parsed discoverAndWait response: \n" + discoverResponse);
        String hostname = new String(discoverResponse.getPayload());

        // Notify listener
        if (mListener != null) {
            MinimoteDiscoveredServer s = new MinimoteDiscoveredServer(
                    rawDiscoverResponse.getAddress().getHostAddress(),
                    rawDiscoverResponse.getPort(),
                    hostname
            );
            mListener.onServerDiscovered(s);
        }
    }

    private boolean broadcastDiscoverRequest() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        MinimotePacket minimoteDiscoverPacket = MinimotePacketFactory.newDiscoverRequest();
        byte[] discoverPacketData = minimoteDiscoverPacket.toData();

        try {
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");

            DatagramPacket discoverPacket = new DatagramPacket(
                    discoverPacketData, discoverPacketData.length, broadcastAddress, mPort);

            Log.d(TAG, "Broadcasting DISCOVER message" + minimoteDiscoverPacket);
            mSocket.send(discoverPacket);
        } catch (UnknownHostException e) {
            Log.e(TAG, "Unable to perform InetAddress.getByName()", e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Unable to send broadcast packet", e);
            return false;
        }

        return true;
    }
}
