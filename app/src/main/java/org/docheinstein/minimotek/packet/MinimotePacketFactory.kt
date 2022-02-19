package org.docheinstein.minimotek.packet

import org.docheinstein.minimotek.extensions.set16
import org.docheinstein.minimotek.extensions.set32
import org.docheinstein.minimotek.extensions.set8
import org.docheinstein.minimotek.keys.MinimoteKeyType

object MinimotePacketFactory {

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

    fun newLeftDown(): MinimotePacket {
        return MinimotePacket(MinimotePacketType.LeftDown)
    }

    fun newLeftUp(): MinimotePacket {
        return MinimotePacket(MinimotePacketType.LeftUp)
    }

    fun newLeftClick(): MinimotePacket {
        return MinimotePacket(MinimotePacketType.LeftClick)
    }

    fun newMiddleDown(): MinimotePacket {
        return MinimotePacket(MinimotePacketType.MiddleDown)
    }

    fun newMiddleUp(): MinimotePacket {
        return MinimotePacket(MinimotePacketType.MiddleUp)
    }

    fun newMiddleClick(): MinimotePacket {
        return MinimotePacket(MinimotePacketType.MiddleClick)
    }

    fun newRightDown(): MinimotePacket {
        return MinimotePacket(MinimotePacketType.RightDown)
    }

    fun newRightUp(): MinimotePacket {
        return MinimotePacket(MinimotePacketType.RightUp)
    }

    fun newRightClick(): MinimotePacket {
        return MinimotePacket(MinimotePacketType.RightClick)
    }

    fun newScrollDown(): MinimotePacket {
        return MinimotePacket(MinimotePacketType.ScrollDown)
    }

    fun newScrollUp(): MinimotePacket {
        return MinimotePacket(MinimotePacketType.ScrollUp)
    }

    fun newDiscoverRequest(): MinimotePacket {
        return MinimotePacket(MinimotePacketType.DiscoverRequest)
    }

    fun newPing(recvPort: Int): MinimotePacket {
        // | RECV_PORT (16 bit) |
        val payload = ByteArray(2)
        payload.set16(recvPort)
        return MinimotePacket(MinimotePacketType.Ping, payload)
    }

    fun newMove(mid: Int, x: Int, y: Int): MinimotePacket {
        // | MOVE_ID (8 bit) | X (12 bit) | Y (12 bit) |
        val payload = ByteArray(4)
        payload.set32(
            ((mid and 0xFF) shl 24) or
            ((x and 0xFFF) shl 12) or
            ((y and 0xFFF))
        )
        return MinimotePacket(MinimotePacketType.Move, payload)
    }

    fun newWrite(c: Char): MinimotePacket {
        // | CHAR (8 bite) |
        val payload = ByteArray(1)
        payload.set8(c.code)
        return MinimotePacket(MinimotePacketType.Write, payload)
    }

    fun newKeyClick(keyType: MinimoteKeyType): MinimotePacket {
        // | KEY TYPE (8 bit) |
        val payload = ByteArray(1)
        payload.set8(keyType.value)
        return MinimotePacket(MinimotePacketType.KeyClick, payload)
    }

    fun newKeyDown(keyType: MinimoteKeyType): MinimotePacket {
        // | KEY TYPE (8 bit) |
        val payload = ByteArray(1)
        payload.set8(keyType.value)
        return MinimotePacket(MinimotePacketType.KeyDown, payload)
    }

    fun newKeyUp(keyType: MinimoteKeyType): MinimotePacket {
        // | KEY TYPE (8 bit) |
        val payload = ByteArray(1)
        payload.set8(keyType.value)
        return MinimotePacket(MinimotePacketType.KeyUp, payload)
    }
}