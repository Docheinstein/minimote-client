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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MinimoteServerDiscoverer {
    private static final String TAG = "MinimoteServerDiscover";

    private static final int MAX_BUFFER_SIZE = 64;

    private Future mDiscoveryTask;
    private ExecutorService mDiscoveryExecutor;
    private DatagramSocket mSocket;
    private int mPort;

    private MinimoteServerDiscovererListener mListener;

    public interface MinimoteServerDiscovererListener {
        void onServerDiscovered(MinimoteDiscoveredServer server);
        void onDiscoveryStarted();
        void onDiscoveryFinished();
    }

    public MinimoteServerDiscoverer(MinimoteServerDiscovererListener listener, int port) {
        if (mListener == null)
            Log.w(TAG, "Listener should not be null");

        mListener = listener;
        mPort = port;

        InetAddress inet;

        try {
            inet = InetAddress.getByName("0.0.0.0");
        } catch (UnknownHostException e) {
            Log.e(TAG, "Unable to perform InetAddress.getByName()", e);
            return;
        }

        try {
            mSocket = new DatagramSocket(Conf.UDP_PORT, inet);
            mSocket.setReuseAddress(true);
            mSocket.setBroadcast(true);
        } catch (SocketException e) {
            Log.e(TAG, "Unable to allocate UDP socket", e);
            return;
        }

        mDiscoveryExecutor = Executors.newSingleThreadExecutor();

        Log.d(TAG , "MinimoteServerDiscoverer initialized");
    }

    public void discoverAndWait(int timeout) {
        if (mSocket == null)
            return;

        Runnable discovery = new Runnable() {
            @Override
            public void run() {
                startListeningDiscoverAnswers();
            }
        };

        mDiscoveryTask = mDiscoveryExecutor.submit(discovery);

        if (!broadcastDiscover()) {
            Log.w(TAG, "Broadcast failed, discovery cannot proceed");
            cleanup();
            return;
        }

        if (mListener != null)
            mListener.onDiscoveryStarted();

        try {
            mDiscoveryTask.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception ignored) {
        } finally {
            Log.v(TAG, "Timeout of " + timeout + "ms elapsed");
            if (mDiscoveryTask != null)
                mDiscoveryTask.cancel(true);
            cleanup();
        }
    }

    public void abortDiscovery() {
        Log.v(TAG, "Discovery abort required");

        if (mSocket == null)
            return;

        if (mDiscoveryTask == null) {
            Log.w(TAG, "No discovery is going on, doing nothing");
            return;
        }

        mDiscoveryTask.cancel(true);
        cleanup();
    }

    private void startListeningDiscoverAnswers() {
        Log.d(TAG, "Discovery started on port " + mPort + ", waiting...");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                byte[] buf = new byte[MAX_BUFFER_SIZE];

                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                mSocket.receive(packet);
                Log.d(TAG, "Received packet from " +
                        packet.getAddress().getHostAddress() + ":" + packet.getPort());
                handleDiscoverResponse(packet);
            } catch (IOException e) {
                if (!(e instanceof SocketException)) // SocketException is thrown when
                                                     // the socket is fairly close (end of discovery)
                    Log.e(TAG, "Error occurred while receiving UDP datagram", e);
            }
        }

        Log.d(TAG, "Exiting DISCOVER listening, thread has been interrupted");
        if (mListener != null)
            mListener.onDiscoveryFinished();
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
                    hostname
            );
            mListener.onServerDiscovered(s);
        }
    }

    private boolean broadcastDiscover() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        MinimotePacket minimoteDiscoverPacket = MinimotePacketFactory.newDiscoverRequest();
        byte[] discoverPacketData = minimoteDiscoverPacket.toData();

        InetAddress broadcastAddress;

        try {
            broadcastAddress = InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException e) {
            Log.e(TAG, "Unable to perform InetAddress.getByName()", e);
            return false;
        }

        DatagramPacket discoverPacket = new DatagramPacket(
                discoverPacketData, discoverPacketData.length, broadcastAddress, mPort);

        try {
            Log.d(TAG, "Broadcasting DISCOVER message" + minimoteDiscoverPacket);
            mSocket.send(discoverPacket);
        } catch (IOException e) {
            Log.e(TAG, "Unable to send broadcast packet", e);
            return false;
        }

        Log.i(TAG, "Broadcasting discovery packet to " + broadcastAddress);
        return true;
    }

    private void cleanup() {
        if (mSocket != null)
            mSocket.close();
        mSocket = null;
        mDiscoveryTask = null;
    }
}
