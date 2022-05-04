package org.docheinstein.minimote.discover

import io.ktor.util.network.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.*
import org.docheinstein.minimote.ANY_ADDR
import org.docheinstein.minimote.BROADCAST_ADDR
import org.docheinstein.minimote.DISCOVER_PORT
import org.docheinstein.minimote.sockets.UdpSocket
import org.docheinstein.minimote.packet.MinimotePacket
import org.docheinstein.minimote.packet.MinimotePacketFactory
import org.docheinstein.minimote.packet.MinimotePacketType
import org.docheinstein.minimote.util.*
import java.io.IOException
import java.lang.System.currentTimeMillis
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.String

/**
 * Servers discoverer.
 * The discover mechanism is the following:
 * 1. Broadcast UDP packet on port 50500 (send to 255.255.255.255)
 * 2. Listen to UDP responses on port 50500 (bound to local IPs: 0.0.0.0)
 */
@Singleton
class Discoverer @Inject constructor() {
    /*
     * Returns a Flow of DiscoveredServer: a DiscoveredServer is emitted
     * each time we receive a response from a Minimote server.
     */
    suspend fun discoverServers(): Flow<DiscoveredServer> {
        verbose("Discoverer.discoverServers()")
        try {
            // Create UDP socket
            debug("Creating UDP socket")
            val udpSocket = UdpSocket(ANY_ADDR, DISCOVER_PORT)
            udpSocket.bind {
                reuseAddress = true
                broadcast = true
            }

            // Broadcast discover request packet
            debug("Broadcasting DISCOVER_REQUEST packet")
            udpSocket.send(MinimotePacketFactory.newDiscoverRequest().toBytes(), BROADCAST_ADDR, DISCOVER_PORT)

            // Listen for discover responses
            debug("Waiting for discover responses...")

            return udpSocket.recvDatagramsAsFlow().transform { response ->
                debug("Received response from ${response.address} at t=${currentTimeMillis()}")

                // Check whether the packet received is actually a DISCOVER_RESPONSE
                val responseBytes = response.packet.readBytes()
                debug("Response is (length=${responseBytes.size}): " +
                    responseBytes.toBinaryString(pretty = true)
                )

                val discoverResponseMinimotePacket: MinimotePacket
                try {
                    discoverResponseMinimotePacket = MinimotePacket.fromBytes(responseBytes)
                } catch (e: MinimotePacket.InvalidPacketException) {
                    warn("Failed to parse packet: ${e.message}")
                    return@transform
                }

                if (discoverResponseMinimotePacket.packetType != MinimotePacketType.DiscoverResponse) {
                    warn("Received packet is not a DISCOVER_RESPONSE, ignoring it")
                    return@transform
                }

                // Legal DISCOVER_RESPONSE received

                debug("Received packet is a valid DISCOVER_RESPONSE")

                val hostname = String(discoverResponseMinimotePacket.payload)
                val discoveredServer = DiscoveredServer(
                    response.address.addr,
                    response.address.port,
                    hostname
                )
                debug("Discovered host: $discoveredServer")

                // Emit the discovered server
                emit(discoveredServer)
            }
        } catch (e: IOException) {
            error("Exception occurred while discovering: ${e.asMessage()}")
            throw e
        }
    }
}