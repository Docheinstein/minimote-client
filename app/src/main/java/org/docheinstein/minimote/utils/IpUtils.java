package org.docheinstein.minimote.utils;

public class IpUtils {
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
}