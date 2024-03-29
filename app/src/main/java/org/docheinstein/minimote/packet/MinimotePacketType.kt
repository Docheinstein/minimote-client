package org.docheinstein.minimote.packet

/**
 * Protocol's packet types.
 * There are three categories of packets
 * - mouse packets (movement, click)
 * - keyboard packets (keys, hotkeys)
 * - other (ping, discover)
 */
enum class MinimotePacketType(val value: Int) {
    None(0x00),

    // Mouse
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

    // Keyboard
    Write(0x0D),
    KeyDown(0x0E),
    KeyUp(0x0F),
    KeyClick(0x10),
    Hotkey(0x11),

    // Other
    Ping(0xFC),
    Pong(0xFD),
    DiscoverRequest(0xFE),
    DiscoverResponse(0xFF);

    companion object {
        private val map = values().associateBy(MinimotePacketType::value)
        operator fun get(value: Int): MinimotePacketType? {
            return map.getOrDefault(value, null)
        }
    }
}