package org.docheinstein.minimote.discovery;

import androidx.annotation.NonNull;

public class DiscoverResponse {

    public static DiscoverResponse fromData(byte[] data) {
        return null;
    }

    private String mMac;
    private String mHostname;

    public String getMac() {
        return mMac;
    }

    public String getHostname() {
        return mHostname;
    }

    @NonNull
    @Override
    public String toString() {
        return mHostname + " - " + mMac;
    }
}
