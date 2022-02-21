package org.docheinstein.minimotek.discover

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.date.*
import io.ktor.util.network.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import org.docheinstein.minimotek.DISCOVER_PORT
import org.docheinstein.minimotek.di.IODispatcher
import org.docheinstein.minimotek.extensions.toBinaryString
import org.docheinstein.minimotek.packet.MinimotePacket
import org.docheinstein.minimotek.packet.MinimotePacketFactory
import org.docheinstein.minimotek.packet.MinimotePacketType
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.error
import org.docheinstein.minimotek.util.warn
import java.io.IOException
import java.net.InetSocketAddress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Discoverer @Inject constructor(
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun discoverServers(): Flow<DiscoveredServer> {
        /*
         * The discover mechanism is the following:
         * 1. Broadcast UDP packet on port 50500 (send to 255.255.255.255)
         * 2. Listen to UDP responses on port 50500 (bound to local IPs: 0.0.0.0)
         *
         * The same UDP socket is used for sending and receiving.
         */
        debug("DiscoveredServerRepository.discoverServers")
        try {
            debug("Creating UDP socket")
            val udpSocket = aSocket(ActorSelectorManager(ioDispatcher))
                .udp()
                // listen to responses on local IP(s)
                .bind(InetSocketAddress("0.0.0.0", DISCOVER_PORT)) {
                reuseAddress = true
                broadcast = true
            }

            // Build discover request packet
            val discoverRequestMinimotePacket = MinimotePacketFactory.newDiscoverRequest()
            val discoverRequestPacket =
                BytePacketBuilder(discoverRequestMinimotePacket.packetLength).also { builder ->
                    builder.writeFully(discoverRequestMinimotePacket.toBytes())
            }.build()

            val discoverRequestPacketDatagram = Datagram(
                discoverRequestPacket, InetSocketAddress("255.255.255.255", DISCOVER_PORT))

//            delay(2000)
//            throw Exception("Unknown error")

            // Broadcast request
            debug("Sending discover request...$discoverRequestMinimotePacket")
            udpSocket.send(discoverRequestPacketDatagram)

            // Listen for responses
            debug("Waiting for discover responses...")
            return udpSocket.incoming.receiveAsFlow().transform { response ->
                    debug("Received response from ${response.address} at t=${getTimeMillis()}")
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

                    debug("Received packet is legal")

                    if (discoverResponseMinimotePacket.packetType != MinimotePacketType.DiscoverResponse) {
                        warn("Received packet is not a DiscoverResponse, ignoring it")
                        return@transform
                    }

                    debug("Received packet is a valid discover response")

                    val hostname = String(discoverResponseMinimotePacket.payload)
                    val discoveredServer = DiscoveredServer(
                        response.address.hostname,
                        response.address.port,
                        hostname
                    )
                    debug("Discovered host: $discoveredServer")

                    emit(discoveredServer)
            }
        } catch (e: IOException) {
            error("Exception: ${e.message}")
            throw e
        }
    }
}