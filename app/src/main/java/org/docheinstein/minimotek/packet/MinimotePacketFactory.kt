package org.docheinstein.minimotek.packet

import org.docheinstein.minimotek.extensions.set16
import org.docheinstein.minimotek.extensions.set32

object MinimotePacketFactory {

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
}