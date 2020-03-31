package org.docheinstein.minimote.utils;

import androidx.annotation.NonNull;

public class NetUtils {
    public static boolean isValidIPv4(String ip) {

        // Check
        if (ip == null)
            return false;

        if (ip.isEmpty() || ip.length() >  15)
            return false;

        String[] quartets = ip.split("\\.");

        if (quartets.length != 4)
            return false;

        for (String quartet : quartets) {
            try {
                int q = Integer.parseInt(quartet);
                if (q < 0 || q > 255)
                    return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    public static boolean isValidPort(int port) {
        return port >= 0 && port < 65535;
    }

    public static String fullAddress(String address, Integer port) {
        return address + ((port != null) ? (":" + port) : "");
    }

    public static class AddressPort {
        public static class AddressPortParseException extends Exception { }

        public String address;
        public int port;

        public static AddressPort parse(String fullAddress) throws AddressPortParseException {
            if (fullAddress == null)
                throw new AddressPortParseException();
            AddressPort ap = new AddressPort();
            String[] parts = fullAddress.split(":");
            if (parts.length == 0)
                throw new AddressPortParseException();
            ap.address = parts[0];
            if (parts.length > 1) {
                try {
                    ap.port = Integer.valueOf(parts[1]);
                } catch (NumberFormatException nfe) {
                    throw new AddressPortParseException();
                }
            }
            if (!StringUtils.isValid(ap.address))
                throw new AddressPortParseException();
            return ap;
        }

        public AddressPort() {}

        public AddressPort(String address, int port) {
            this.address = address;
            this.port = port;
        }

        public boolean isValid() {
            return isValidIPv4(address) && isValidPort(port);
        }

        @NonNull
        @Override
        public String toString() {
            return fullAddress(address, port);
        }

    }
}