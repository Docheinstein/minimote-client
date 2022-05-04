package org.docheinstein.minimotek.packet


import org.docheinstein.minimotek.keys.MinimoteKeyType
import org.docheinstein.minimotek.util.set16
import org.docheinstein.minimotek.util.set32
import org.docheinstein.minimotek.util.set8

/**
 * Factory of [MinimotePacket]s for each possible [MinimotePacketType].
 */
object MinimotePacketFactory {
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
        // | UNICODE (4 byte) |
        val payload = ByteArray(4)
        payload.set32(c.code)
        return MinimotePacket(MinimotePacketType.Write, payload)
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

    fun newKeyClick(keyType: MinimoteKeyType): MinimotePacket {
        // | KEY TYPE (8 bit) |
        val payload = ByteArray(1)
        payload.set8(keyType.value)
        return MinimotePacket(MinimotePacketType.KeyClick, payload)
    }

    fun newHotkey(keys: List<MinimoteKeyType>): MinimotePacket {
        // | KEY TYPE (8 bit) * keys.size |
        val payload = ByteArray(keys.size)
        var i = 0
        for (key in keys) {
            payload.set8(key.value, offset = i)
            i++
        }
        return MinimotePacket(MinimotePacketType.Hotkey, payload)
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
}