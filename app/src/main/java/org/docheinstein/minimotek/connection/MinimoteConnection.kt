package org.docheinstein.minimotek.connection

import org.docheinstein.minimotek.sockets.TcpSocket
import org.docheinstein.minimotek.sockets.UdpSocket
import org.docheinstein.minimotek.packet.MinimotePacket
import org.docheinstein.minimotek.packet.MinimotePacketFactory
import org.docheinstein.minimotek.packet.MinimotePacketType
import org.docheinstein.minimotek.util.*

/**
 * Connection with the minimote server.
 * It consists of both a TCP socket (for packets not to lose, e.g. keyboard events)
 * and an UDP socket (for packets that could be lost, e.g. mouse movement).
 * Connection can be tested by sending a PING packet to the server on the TCP socket
 * and waiting for a PONG packet on the UDP socket (this allows to test both sockets).
 * By the way, the server accepts every events on both the sockets, therefore the client
 * may choose to use the UDP or the TCP autonomously for each packet.
 */
class MinimoteConnection(
    val address: String,
    val port: Int
) {
    private var udpSocket = UdpSocket()
    private var tcpSocket = TcpSocket(address, port)

    suspend fun connect(): Boolean {
        debug("MinimoteConnection.connect()")
        try {
            tcpSocket.connect {
                reuseAddress = true
            }
        } catch (e: Exception) {
            error("Failed to establish TCP connection: ${e.asMessage()}", e)
            return false
        }

        try {
            udpSocket.bind {
                reuseAddress = true
            }
        } catch (e: Exception) {
            error("Failed to create UDP socket: ${e.asMessage()}", e)
            return false
        }

        return true
    }

    suspend fun ensureConnection(): Boolean {
        // Check sockets health
        if (!tcpSocket.isOpen || !udpSocket.isOpen)
            return false

        // Actually check connection with ping/pong packets
        try {
            // Send PING on TCP socket
            debug("Sending PING")
            tcpSocket.send(MinimotePacketFactory.newPing(udpSocket.boundLocalPort).toBytes())

            // Wait for PONG on UDP socket
            debug("Waiting for PONG...")
            val response = udpSocket.recv()
            debug("Received a response for PING")

            // Check whether the packet received is actually a PONG
            val pongMinimotePacket: MinimotePacket
            try {
                pongMinimotePacket = MinimotePacket.fromBytes(response)
            } catch (e: MinimotePacket.InvalidPacketException) {
                warn("Failed to parse packet: ${e.asMessage()}")
                return false
            }

            if (pongMinimotePacket.packetType != MinimotePacketType.Pong) {
                warn("Received packet is not a PONG")
                return false
            }

            // Legal PONG received, connection is ok
            debug("Received packet is a valid PONG, connection is healthy")
            return true
        } catch (e: Exception) {
            error("Failed to ensure connection: ${e.asMessage()}", e)
            return false
        }
    }

    suspend fun sendTcp(packet: MinimotePacket): Boolean {
        return try {
            if (packet.payload.isEmpty())
                debug("TCP >> ${packet.packetType}")
            else
                debug("TCP >> ${packet.packetType} : ${packet.payload.toBinaryString(pretty = true)}")
            tcpSocket.send(packet.toBytes())
            true
        } catch (e: Exception) {
            error("Failed to send packet: ${e.asMessage()}", e)
            false
        }
    }

    suspend fun sendUdp(packet: MinimotePacket): Boolean {
        return try {
            if (packet.payload.isEmpty())
                debug("UDP >> ${packet.packetType}")
            else
                debug("UDP >> ${packet.packetType} : ${packet.payload.toBinaryString(pretty = true)}")
            udpSocket.send(packet.toBytes(), address, port)
            true
        } catch (e: Exception) {
            error("Failed to send packet: ${e.asMessage()}", e)
            false
        }
    }

    suspend fun disconnect(): Boolean {
        return try {
            debug("MinimoteConnection.disconnect()")
            tcpSocket.disconnect()
            udpSocket.disconnect()
            true
        } catch (e: Exception) {
            warn("Disconnection failed: ${e.asMessage()}")
            false
        }
    }
}