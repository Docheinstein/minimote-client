package org.docheinstein.minimotek.connection

import org.docheinstein.minimotek.net.TcpSocket
import org.docheinstein.minimotek.net.UdpSocket
import org.docheinstein.minimotek.packet.MinimotePacket
import org.docheinstein.minimotek.packet.MinimotePacketFactory
import org.docheinstein.minimotek.packet.MinimotePacketType
import org.docheinstein.minimotek.util.asMessage
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.error
import org.docheinstein.minimotek.util.warn
import java.io.IOException

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
        } catch (e: IOException) {
            error("Failed to establish TCP connection: ${e.asMessage()}", e)
            return false
        }

        try {
            udpSocket.bind {
                reuseAddress = true
            }
        } catch (e: IOException) {
            error("Failed to create UDP socket: ${e.asMessage()}", e)
            return false
        }

        return true
    }

    suspend fun ensureConnection(): Boolean {
        // Check socket health
        if (!tcpSocket.isOpen() || !udpSocket.isOpen())
            return false

        // Actually check connection with ping/pong packets
        try {
            debug("Sending PING")

            tcpSocket.send(MinimotePacketFactory.newPing(udpSocket.boundLocalPort).toBytes())

            debug("Waiting for PONG...")
            val response = udpSocket.recv()
            debug("Received a response for PING")

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

            debug("Received packet is a PONG, connection is healthy")
            return true
        } catch (e: IOException) {
            error("Failed to ensure connection: ${e.asMessage()}", e)
            return false
        }
    }

    suspend fun sendTcp(packet: MinimotePacket): Boolean {
        return try {
            tcpSocket.send(packet.toBytes())
            true
        } catch (e: IOException) {
            error("Failed to send packet: ${e.asMessage()}", e)
            false
        }
    }

    suspend fun sendUdp(packet: MinimotePacket): Boolean {
        return try {
            udpSocket.send(packet.toBytes(), address, port)
            true
        } catch (e: IOException) {
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
        } catch (e: IOException) {
            false
        }
    }
}