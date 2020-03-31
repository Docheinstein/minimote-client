package org.docheinstein.minimote.discovery;

import androidx.annotation.NonNull;

import org.docheinstein.minimote.utils.NetUtils;

public class MinimoteDiscoveredServer {

    private String mAddress;
    private int mPort;
    private String mHostname;

    MinimoteDiscoveredServer(String address, int port, String hostname) {
        mAddress = address;
        mPort = port;
        mHostname = hostname;
    }

    public String getAddress() {
        return mAddress;
    }

    public int getPort() {
        return mPort;
    }

    public String getHostname() {
        return mHostname;
    }

    @NonNull
    @Override
    public String toString() {
        return NetUtils.fullAddress(mAddress, mPort) + " (" + mHostname + ")";
    }
}
