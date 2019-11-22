package org.docheinstein.minimote.connection;

import android.util.Log;

import org.docheinstein.minimote.packet.MinimotePacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MinimoteConnection {

    private static final String TAG = "MinimoteConnection";

    private final Object mSocketLock = new Object();

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

    public boolean isConnected() {
        return
                mTcpSocket != null && !mTcpSocket.isClosed() &&
                mUdpSocket != null && !mUdpSocket.isClosed();
    }

    public boolean connect() {
        return connectTcp() && connectUdp();
    }

    public void disconnect() {
        synchronized (mSocketLock) {
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
            Log.v(TAG, ">> Sending UDP packet >> \n" + packet + "\n");
            datagram = new DatagramPacket(bs, bs.length, InetAddress.getByName(mAddress), mUdpPort);
        } catch (UnknownHostException e) {
            Log.w(TAG, "Address resolution failed, cannot send UDP packet");
            return false;
        }

        try {
            mUdpSocket.send(datagram);
            return true;
        } catch (IOException e) {
            Log.w(TAG, "Cannot write to TCP socket", e);
            return false;
        }
    }

    private boolean connectTcp() {
        synchronized (mSocketLock) {
            Log.d(TAG, "Going to establish a TCP connection with " + mAddress + ":" + mTcpPort);
            try {
                mTcpSocket = new Socket(mAddress, mTcpPort);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error occurred while trying to establish a TCP connection", e);
                return false;
            }
        }
    }

    private boolean connectUdp() {
        synchronized (mSocketLock) {
            Log.d(TAG, "Going to establish a UDP connection with " + mAddress + ":" + mUdpPort);
            try {
                mUdpSocket = new DatagramSocket();
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error occurred while trying to establish an UDP connection", e);
                return false;
            }
        }
    }
}
