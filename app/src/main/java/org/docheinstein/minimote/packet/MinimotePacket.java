package org.docheinstein.minimote.packet;

import org.docheinstein.minimote.utils.ByteUtils;

public class MinimotePacket {

    public static final int HEADER_SIZE = 8;

    public static MinimotePacket fromData(byte[] data) {
        if (data == null)
            return null;

        if (data.length < HEADER_SIZE)
            return null;

        Long header = ByteUtils.toLong(data);

        if (header == null)
            return null;

        int packetLength = (int) ((header >>> 56) & 0xFF);
        MinimotePacketType packetType = MinimotePacketType.fromValue((int) ((header >>> 48) & 0xFF));
        long eventTime = header & 0xFFFFFFFFFFFFL;

        byte[] payload = null;

        if (data.length > HEADER_SIZE) {
            payload = new byte[packetLength - HEADER_SIZE];
            System.arraycopy(data, HEADER_SIZE, payload, 0, packetLength - HEADER_SIZE);
        }

        return new MinimotePacket(
                packetLength, packetType,
                eventTime, payload
        );
    }

    private int mPacketLength;
    private MinimotePacketType mPacketType;
    private long mEventTime;
    private byte[] mPayload;

    public MinimotePacket(int packetLength, MinimotePacketType packetType,
                          long eventTime, byte[] payload) {
        mPacketLength = packetLength;
        mPacketType = packetType;
        mEventTime = eventTime;
        mPayload = payload;
    }

    public MinimotePacket(int packetLength, MinimotePacketType packetType,
                          byte[] payload) {
        this(packetLength, packetType, System.currentTimeMillis(), payload);
    }

    public MinimotePacket(MinimotePacketType packetType) {
        this(HEADER_SIZE, packetType, System.currentTimeMillis(), null);
    }

    public MinimotePacket(MinimotePacketType packetType, long eventTime) {
        this(HEADER_SIZE, packetType, eventTime, null);
    }

    public int getPacketLength() { return mPacketLength; }
    public MinimotePacketType getEventType() { return mPacketType; }
    public long getEventTime() { return mEventTime; }
    public byte[] getPayload() { return mPayload; }
    public int getPayloadLength() { return mPayload != null ? mPayload.length : 0; }

    public byte[] toData() {
        long header =
            ((mPacketLength & 0xFFL) << 56) |
            ((mPacketType.value & 0xFFL) << 48) |
            mEventTime & 0xFFFFFFFFFFFFL;

        byte[] data = new byte[HEADER_SIZE + getPayloadLength()];
        ByteUtils.put64(header, data, 0);

        if (getPayloadLength() > 0)
            System.arraycopy(mPayload, 0, data, HEADER_SIZE, mPayload.length);

        return data;
    }

    @Override
    public String toString() {
        return  "Packet length: " + mPacketLength + " bytes" + "\n" +
                "Packet type: " + mPacketType + "\n" +
                "Event time: " + mEventTime + "\n" +
                "Payload: " + ByteUtils.toBinaryString(mPayload);
    }
}
