package org.docheinstein.minimote.packet;

public enum MinimotePacketType {
    None(0x00),
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
    Hotkey(0x11),

    Ping(0xFC),
    Pong(0xFD),
    DiscoverRequest(0xFE),
    DiscoverResponse(0xFF);

    public int value;

    MinimotePacketType(int value) {
        this.value = value;
    }

    public static MinimotePacketType fromValue(int value) {
        switch (value) {
            case 0x01:
                return LeftDown;
            case 0x02:
                return LeftUp;
            case 0x03:
                return LeftClick;
            case 0x04:
                return MiddleDown;
            case 0x05:
                return MiddleUp;
            case 0x06:
                return MiddleClick;
            case 0x07:
                return RightDown;
            case 0x08:
                return RightUp;
            case 0x09:
                return RightClick;
            case 0x0A:
                return ScrollDown;
            case 0x0B:
                return ScrollUp;
            case 0x0C:
                return Move;
            case 0x0D:
                return Write;
            case 0x0E:
                return KeyDown;
            case 0x0F:
                return KeyUp;
            case 0x10:
                return KeyClick;
            case 0x11:
                return Hotkey;
            case 0xFE:
                return DiscoverRequest;
            case 0xFF:
                return DiscoverResponse;
            default:
                return None;
        }
    }

}
