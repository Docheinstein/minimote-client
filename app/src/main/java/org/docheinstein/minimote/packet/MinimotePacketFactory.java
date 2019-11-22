package org.docheinstein.minimote.packet;

import org.docheinstein.minimote.utils.ByteUtils;

public class MinimotePacketFactory {
    public static MinimotePacket newDiscoverRequest() {
        return new MinimotePacket(MinimotePacketType.DiscoverRequest);
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
}
