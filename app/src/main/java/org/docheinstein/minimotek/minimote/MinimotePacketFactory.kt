package org.docheinstein.minimotek.minimote

object MinimotePacketFactory {

    fun newDiscoverRequest(): MinimotePacket {
        return MinimotePacket(MinimotePacketType.DiscoverRequest)
    }
}