package org.docheinstein.minimote.discovery;

import androidx.annotation.NonNull;

public class MinimoteDiscoveredServer {
    private String mAddress;
    private String mHostname;

    MinimoteDiscoveredServer(String address, String hostname) {
        mAddress = address;
        mHostname = hostname;
    }

    public String getAddress() {
        return mAddress;
    }

    public String getHostname() {
        return mHostname;
    }

    @NonNull
    @Override
    public String toString() {
        return mAddress + " (" + mHostname + ")";
    }
}
