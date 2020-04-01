package org.docheinstein.minimote.connection;

import android.util.Log;

import org.docheinstein.minimote.commons.Conf;
import org.docheinstein.minimote.packet.MinimotePacket;
import org.docheinstein.minimote.packet.MinimotePacketFactory;
import org.docheinstein.minimote.packet.MinimotePacketType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class MinimoteConnection {

    private static final int SOCKET_TIMEOUT = 5000;

    private static final String TAG = "MinimoteConnection";

    private final Object mSocketConnectLock = new Object();
    private final Object mSocketDisconnectLock = new Object();

    private String mAddress;
    private int mTcpPort;
    private int mUdpPort;

    private Socket mTcpSocket;
    private DatagramSocket mUdpSocket;

    public MinimoteConnection(String address, int tcpPort, int udpPort) {
        mAddress = address;
        mTcpPort = tcpPort;
        mUdpPort = udpPort;
    }

    public boolean shouldBeConnected() {
        return
                mTcpSocket != null && !mTcpSocket.isClosed() &&
                mUdpSocket != null && !mUdpSocket.isClosed();
    }

    public boolean connect() {
        synchronized (mSocketConnectLock) {
            Log.v(TAG, "MinimoteConnection.connect()");
            return connectTcp() && connectUdp();
        }
    }

    public synchronized void disconnect() {
        synchronized (mSocketDisconnectLock) {
            Log.v(TAG, "MinimoteConnection.disconnect()");

            if (mTcpSocket != null) {
                try {
                    mTcpSocket.close();
                } catch (IOException e) {
                    Log.w(TAG, "TCP Socket close failed", e);
                }
            }

            if (mUdpSocket != null) {
                mUdpSocket.close();
            }

            mTcpSocket = null;
            mUdpSocket = null;
        }
    }

    public boolean sendTcp(MinimotePacket packet) {
        if (packet == null) {
            Log.w(TAG, "Cannot send null packet");
            return false;
        }

        if (mTcpSocket == null) {
            Log.w(TAG, "Cannot send TCP packet, null socket");
            return false;
        }

        try {
            Log.v(TAG, ">> Sending TCP packet >> \n" + packet + "\n");
            mTcpSocket.getOutputStream().write(packet.toData());
            return true;
        } catch (IOException e) {
            Log.w(TAG, "Cannot write to TCP socket", e);
            return false;
        }
    }

    public boolean sendUdp(MinimotePacket packet) {
        if (packet == null) {
            Log.w(TAG, "Cannot send null packet");
            return false;
        }

        if (mUdpSocket == null) {
            Log.w(TAG, "Cannot send TCP packet, null socket");
            return false;
        }

        byte[] bs = packet.toData();
        DatagramPacket datagram;
        try {
            datagram = new DatagramPacket(bs, bs.length, InetAddress.getByName(mAddress), mUdpPort);
        } catch (UnknownHostException e) {
            Log.w(TAG, "Address resolution failed, cannot send UDP packet");
            return false;
        }

        try {
        Log.v(TAG, ">> Sending UDP packet >> \n" + packet + "\n");
            mUdpSocket.send(datagram);
            return true;
        } catch (IOException e) {
            Log.w(TAG, "Cannot write to TCP socket", e);
            return false;
        }
    }

    public boolean ensureConnected(int timeout) {
        // At least it "should be connected"
        if (!shouldBeConnected())
            return false;


        // Create an incoming UDP socket for listen to PONG
        DatagramSocket pongSocket;

        try {
            // Create UDP socket
            InetAddress inet = InetAddress.getByName("0.0.0.0");
            pongSocket = new DatagramSocket(null /* unbound */);
            pongSocket.setReuseAddress(true);
            pongSocket.bind(new InetSocketAddress(inet, 0));
            pongSocket.setSoTimeout(timeout);
        } catch (IOException e) {
            Log.w(TAG, "Socket creation failed", e);
            return false;
        }

        int pongPort = pongSocket.getLocalPort();

        // Send PING on the current TCP socket
        if (sendTcp(MinimotePacketFactory.newPing(pongPort))) {
            Log.d(TAG, "Ping sent");
        }   else {
            Log.w(TAG, "Ping send failed");
            pongSocket.close();
            return false;
        }

        Log.d(TAG, "Listening for PONG on port " + pongPort);

        // Wait for PONG on a new UDP socket
        try {
            // Listen
            final int PONG_PACKET_SIZE = 8; // Only header
            byte[] buf = new byte[PONG_PACKET_SIZE];

            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            pongSocket.receive(packet);

            Log.d(TAG, "Received packet from " +
                    packet.getAddress().getHostAddress() + ":" + packet.getPort());

            MinimotePacket pingResponse = MinimotePacket.fromData(buf);

            if (pingResponse != null) {
                if (pingResponse.getEventType() == MinimotePacketType.Pong) {
                    Log.d(TAG, "Received a valid PONG, connection is up");
                    return true;
                } else {
                    Log.w(TAG, "Received packet is not a valid PONG\n" + pingResponse);
                }
            } else {
                Log.w(TAG, "Received packet is not a valid minimote packet");
            }
        } catch (IOException e) {
            Log.w(TAG, "Socket timeout elapsed, connection is probably down", e);
        }


        pongSocket.close();
        return false;
    }

    private boolean connectTcp() {
        Log.d(TAG, "Going to establish a TCP connection with " + mAddress + ":" + mTcpPort);
        try {
            mTcpSocket = new Socket(mAddress, mTcpPort);
            mTcpSocket.setSoTimeout(SOCKET_TIMEOUT);
            mTcpSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error occurred while trying to establish a TCP connection", e);
            return false;
        }
    }

    private boolean connectUdp() {
        Log.d(TAG, "Going to establish a UDP connection with " + mAddress + ":" + mUdpPort);
        try {
            mUdpSocket = new DatagramSocket();
            mUdpSocket.setSoTimeout(SOCKET_TIMEOUT);
            mUdpSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error occurred while trying to establish an UDP connection", e);
            return false;
        }
    }
}
