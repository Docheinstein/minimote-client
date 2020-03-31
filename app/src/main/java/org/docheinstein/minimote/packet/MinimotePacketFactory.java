package org.docheinstein.minimote.packet;

import org.docheinstein.minimote.keys.MinimoteKeyType;
import org.docheinstein.minimote.utils.ByteUtils;

import java.util.List;

public class MinimotePacketFactory {
    /*
        LeftDown(0x01),
        LeftUp(0x02),
        LeftClick(0x03),
        MiddleDown(0x04),
        MiddleUp(0x05),
        MiddleClick(0x06),
        RightDown(0x07),
        RightUp(0x08),
        RightClick(0x09),
        ScrollDown(0x0A),
        ScrollUp(0x0B),
        Move(0x0C),
        Write(0x0D),
        KeyDown(0x0E),
        KeyUp(0x0F),
        KeyClick(0x10),

        DiscoverRequest(0xFE),
     */

    public static MinimotePacket newLeftDown() {
        return new MinimotePacket(MinimotePacketType.LeftDown);
    }

    public static MinimotePacket newLeftUp() {
        return new MinimotePacket(MinimotePacketType.LeftUp);
    }

    public static MinimotePacket newLeftClick() {
        return new MinimotePacket(MinimotePacketType.LeftClick);
    }

    public static MinimotePacket newMiddleDown() {
        return new MinimotePacket(MinimotePacketType.MiddleDown);
    }

    public static MinimotePacket newMiddleUp() {
        return new MinimotePacket(MinimotePacketType.MiddleUp);
    }

    public static MinimotePacket newMiddleClick() {
        return new MinimotePacket(MinimotePacketType.MiddleClick);
    }

    public static MinimotePacket newRightDown() {
        return new MinimotePacket(MinimotePacketType.RightDown);
    }

    public static MinimotePacket newRightUp() {
        return new MinimotePacket(MinimotePacketType.RightUp);
    }

    public static MinimotePacket newRightClick() {
        return new MinimotePacket(MinimotePacketType.RightClick);
    }

    public static MinimotePacket newScrollDown() {
        return new MinimotePacket(MinimotePacketType.ScrollDown);
    }

    public static MinimotePacket newScrollUp() {
        return new MinimotePacket(MinimotePacketType.ScrollUp);
    }

    public static MinimotePacket newMove(int mid, int x, int y) {
        final int PAYLOAD_SIZE = 4;

        // | MID (8 bit) | X (12 bit) | Y (12 bit) |
        int payloadValue =
                ((mid & 0xFF) << 24) |
                ((x & 0xFFF) << 12) |
                ((y & 0xFFF));

        byte[] payload = new byte[PAYLOAD_SIZE];
        ByteUtils.put32(payloadValue, payload);

        return new MinimotePacket(
                MinimotePacket.HEADER_SIZE + PAYLOAD_SIZE,
                MinimotePacketType.Move,
                payload
        );
    }

    public static MinimotePacket newWrite(char ch) {
        final int PAYLOAD_SIZE = 1;

        // | CHAR (8 bit) |
        int payloadValue = (int) ch;

        byte[] payload = new byte[PAYLOAD_SIZE];
        ByteUtils.put8(payloadValue, payload);

        return new MinimotePacket(
                MinimotePacket.HEADER_SIZE + PAYLOAD_SIZE,
                MinimotePacketType.Write,
                payload
        );
    }

    public static MinimotePacket newKeyDown(MinimoteKeyType keyType) {
        final int PAYLOAD_SIZE = 1;

        // | KEY TYPE (8 bit) |
        int payloadValue = keyType.getValue();

        byte[] payload = new byte[PAYLOAD_SIZE];
        ByteUtils.put8(payloadValue, payload);

        return new MinimotePacket(
                MinimotePacket.HEADER_SIZE + PAYLOAD_SIZE,
                MinimotePacketType.KeyDown,
                payload
        );
    }

    public static MinimotePacket newKeyUp(MinimoteKeyType keyType) {
        final int PAYLOAD_SIZE = 1;

        // | KEY TYPE (8 bit) |
        int payloadValue = keyType.getValue();

        byte[] payload = new byte[PAYLOAD_SIZE];
        ByteUtils.put8(payloadValue, payload);

        return new MinimotePacket(
                MinimotePacket.HEADER_SIZE + PAYLOAD_SIZE,
                MinimotePacketType.KeyUp,
                payload
        );
    }

    public static MinimotePacket newKeyClick(MinimoteKeyType keyType) {
        final int PAYLOAD_SIZE = 1;

        // | KEY TYPE (8 bit) |
        int payloadValue = keyType.getValue();

        byte[] payload = new byte[PAYLOAD_SIZE];
        ByteUtils.put8(payloadValue, payload);

        return new MinimotePacket(
                MinimotePacket.HEADER_SIZE + PAYLOAD_SIZE,
                MinimotePacketType.KeyClick,
                payload
        );
    }

    public static MinimotePacket newHotkey(List<MinimoteKeyType> keys) {
        final int PAYLOAD_SIZE = keys.size();

        // | KEY TYPE (8 bit) * keys.length |
        byte[] payload = new byte[PAYLOAD_SIZE];

        int i = 0;
        for (MinimoteKeyType key : keys) {
            ByteUtils.put8(key.getValue(), payload, i);
            i++;
        }

        return new MinimotePacket(
                MinimotePacket.HEADER_SIZE + PAYLOAD_SIZE,
                MinimotePacketType.Hotkey,
                payload
        );
    }

    public static MinimotePacket newDiscoverRequest() {
        return new MinimotePacket(MinimotePacketType.DiscoverRequest);
    }

}
