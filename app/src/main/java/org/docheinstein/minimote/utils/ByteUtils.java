package org.docheinstein.minimote.utils;

public class ByteUtils {

    public static String toBinaryString(byte[] bytes) {
        return toBinaryString(bytes, false);
    }

    public static String toBinaryString(byte[] bytes, boolean pretty) {
        if (bytes == null)
            return null;

        StringBuilder sb = new StringBuilder();

        for (byte aByte : bytes) {
            StringBuilder bin = new StringBuilder(Integer.toBinaryString(aByte & 0xFF));
            while (bin.length() < 8)
                bin.insert(0, "0");
            sb.append(bin);
            if (pretty)
                sb.append(" | ");
        }

        return sb.toString();
    }

    public static Long toLong(byte[] bytes) {
        if (bytes == null || bytes.length < 8)
            return null;

        return
            ((bytes[0] & 0xFFL) << 56) |
            ((bytes[1] & 0xFFL) << 48) |
            ((bytes[2] & 0xFFL) << 40) |
            ((bytes[3] & 0xFFL) << 32)|
            ((bytes[4] & 0xFFL) << 24) |
            ((bytes[5] & 0xFFL) << 16) |
            ((bytes[6] & 0xFFL) << 8)|
            ((bytes[7] & 0xFFL));
    }

    public static byte[] fromLong(long value) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) ((value >>> 56) & 0xFFL);
        bytes[1] = (byte) ((value >>> 48) & 0xFFL);
        bytes[2] = (byte) ((value >>> 40) & 0xFFL);
        bytes[3] = (byte) ((value >>> 32) & 0xFFL);
        bytes[4] = (byte) ((value >>> 24) & 0xFFL);
        bytes[5] = (byte) ((value >>> 16) & 0xFFL);
        bytes[6] = (byte) ((value >>> 8) & 0xFFL);
        bytes[7] = (byte) ((value) & 0xFFL);
        return bytes;
    }

    public static void put64(long value, byte[] bytes) {
        put64(value, bytes, 0);
    }

    public static void put64(long value, byte[] bytes, int offset) {
        bytes[offset] =     (byte) ((value >>> 56) & 0xFFL);
        bytes[offset + 1] = (byte) ((value >>> 48) & 0xFFL);
        bytes[offset + 2] = (byte) ((value >>> 40) & 0xFFL);
        bytes[offset + 3] = (byte) ((value >>> 32) & 0xFFL);
        bytes[offset + 4] = (byte) ((value >>> 24) & 0xFFL);
        bytes[offset + 5] = (byte) ((value >>> 16) & 0xFFL);
        bytes[offset + 6] = (byte) ((value >>> 8) & 0xFFL);
        bytes[offset + 7] = (byte) ((value) & 0xFFL);
    }

    public static void put32(int value, byte[] bytes) {
        put32(value, bytes, 0);
    }

    public static void put32(int value, byte[] bytes, int offset) {
        bytes[offset] =     (byte) ((value >>> 24) & 0xFF);
        bytes[offset + 1] = (byte) ((value >>> 16) & 0xFF);
        bytes[offset + 2] = (byte) ((value >>> 8) & 0xFF);
        bytes[offset + 3] = (byte) ((value) & 0xFF);
    }

    public static void put8(int value, byte[] bytes) {
        put8(value, bytes, 0);
    }

    public static void put8(int value, byte[] bytes, int offset) {
        bytes[offset] =     (byte) ((value) & 0xFF);
    }
}
