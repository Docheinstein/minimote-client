package org.docheinstein.minimotek.connection

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import org.docheinstein.minimotek.DISCOVER_PORT
import org.docheinstein.minimotek.extensions.toBinaryString
import org.docheinstein.minimotek.packet.MinimotePacket
import org.docheinstein.minimotek.packet.MinimotePacketFactory
import org.docheinstein.minimotek.packet.MinimotePacketType
import org.docheinstein.minimotek.util.asMessage
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.error
import org.docheinstein.minimotek.util.warn
import java.io.IOException
import java.net.InetSocketAddress

class MinimoteConnection(
    val address: String,
    val port: Int
) {
    private var udpSocket: BoundDatagramSocket? = null
    private var tcpSocket: Socket? = null

    private var tcpOutput: ByteWriteChannel? = null
    private var udpOutput: SendChannel<Datagram>? = null

    suspend fun connect(): Boolean {
        debug("MinimoteConnection.connect()")
        try {
            debug("Creating TCP socket...")
            tcpSocket = aSocket(ActorSelectorManager(Dispatchers.IO))
            .tcp()
            .connect(address, port) {
                reuseAddress = true
            }

            tcpOutput = tcpSocket!!.openWriteChannel(autoFlush = true)
        } catch (e: IOException) {
            error("Failed to establish TCP connection: ${e.asMessage()}", e)
            return false
        }

        try {
            debug("Creating UDP socket...")
            udpSocket = aSocket(ActorSelectorManager(Dispatchers.IO))
                .udp()
                .bind() {
                    reuseAddress = true
                }
            udpOutput = udpSocket!!.outgoing
        } catch (e: IOException) {
            error("Failed to create UDP socket: ${e.asMessage()}", e)
            return false
        }

        return true
    }

    suspend fun ensureConnection(): Boolean {
        // Check socket health
        if (tcpSocket?.isClosed != false || udpSocket?.isClosed != false ||
            tcpOutput == null || udpOutput == null)
            // null or closed sockets, not connected for sure
            return false

        // Actually check connection with ping/pong packets
        try {
            debug("Creating UDP socket for listening to PONG")
            aSocket(ActorSelectorManager(Dispatchers.IO))
                .udp()
                .bind(InetSocketAddress("0.0.0.0", 0 /* auto */)) {
                    reuseAddress = true
                }.use /* auto close */ { pongSocket ->
                debug("PONG socket bound to ${pongSocket.localAddress.hostname}:${pongSocket.localAddress.port}")

                debug("Sending PING")
                if (!sendTcp(MinimotePacketFactory.newPing(pongSocket.localAddress.port)))
                    return false

                debug("Waiting for PONG...")
                val responseBytes = pongSocket.incoming.receive().packet.readBytes()
                debug("Response is (length=${responseBytes.size}): " +
                        responseBytes.toBinaryString(pretty = true)
                )

                val pongMinimotePacket: MinimotePacket
                try {
                    pongMinimotePacket = MinimotePacket.fromBytes(responseBytes)
                } catch (e: MinimotePacket.InvalidPacketException) {
                    warn("Failed to parse packet: ${e.asMessage()}")
                    return false
                }

                debug("Received packet is legal")

                if (pongMinimotePacket.packetType != MinimotePacketType.Pong) {
                    warn("Received packet is not a PONG")
                    return false
                }

                debug("Received packet is a PONG, connection is healthy")
                return true
            }
        } catch (e: IOException) {
            return false
        }
    }

    suspend fun sendTcp(packet: MinimotePacket): Boolean {
        // Check socket health
        if (tcpSocket?.isClosed != false || tcpOutput == null)
            return false
        try {
            val data = packet.toBytes()
            debug(">> Sending TCP: ${data.toBinaryString(pretty = true)}" )
            tcpOutput!!.writeFully(packet.toBytes())
            return true
        }  catch (e: IOException) {
            return false
        }
    }

    suspend fun sendUdp(packet: MinimotePacket): Boolean {
        // Check socket health
        if (tcpSocket?.isClosed != false || tcpOutput == null)
            return false
        try {
            val data = packet.toBytes()
            debug(">> Sending UDP: ${data.toBinaryString(pretty = true)}" )
            udpSocket!!.send(Datagram(
                BytePacketBuilder(data.size). also { it.writeFully(data) }.build(),
                InetSocketAddress(address, port))
            )
            return true
        }  catch (e: IOException) {
            return false
        }
    }

    fun disconnect() {
        debug("MinimoteConnection.disconnect()")

        debug("Closing TCP socket")
        tcpSocket?.close()
        tcpSocket = null

        debug("Closing UDP socket")
        udpSocket?.close()
        udpSocket = null
    }
}