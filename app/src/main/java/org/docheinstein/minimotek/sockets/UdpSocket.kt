package org.docheinstein.minimotek.sockets

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.docheinstein.minimotek.ANY_ADDR
import org.docheinstein.minimotek.ANY_PORT
import org.docheinstein.minimotek.util.toBinaryString
import org.docheinstein.minimotek.util.debug
import java.net.InetSocketAddress

/**
 * UDP socket.
 * Actually this is a wrapper of ktor's UDP socket ([BoundDatagramSocket].
 */
// https://ktor.io/docs/servers-raw-sockets.html
class UdpSocket(
    val localAddress: String = ANY_ADDR,
    val localPort: Int = ANY_PORT,
) {
    private var socket: BoundDatagramSocket? = null
    private val builder = aSocket(ActorSelectorManager(Dispatchers.IO)).udp()
    private val mutex = Mutex()

    val boundLocalAddress: String
        get() = socket!!.localAddress.hostname

    val boundLocalPort: Int
        get() = socket!!.localAddress.port

    val isOpen: Boolean
        get() = socket?.isClosed == false

    suspend fun bind(configure: SocketOptions.UDPSocketOptions.() -> Unit = {}) {
        debug("Going to create UDP socket...")
        mutex.withLock {
            debug("Creating UDP socket...")
            socket = builder.bind(InetSocketAddress(localAddress, localPort), configure)
        }
    }

    suspend fun send(data: ByteArray, remoteAddress: String, remotePort: Int) {
        debug("Going to send on UDP socket...")
        mutex.withLock {
            debug(">> Sending UDP: ${data.toBinaryString(pretty = true)}" )
            socket!!.send(Datagram(
                BytePacketBuilder(data.size). also { it.writeFully(data) }.build(),
                InetSocketAddress(remoteAddress, remotePort))
            )
        }
    }

    suspend fun recv(): ByteArray {
        debug("Going to recv on UDP socket...")
        mutex.withLock {
            debug(">> Waiting UDP..." )
            val data = socket!!.incoming.receive().packet.readBytes()
            debug(">> Received UDP: ${data.toBinaryString(pretty = true)}" )
            return data
        }
    }
    fun recvDatagramsAsFlow(): Flow<Datagram> {
        return socket!!.incoming.receiveAsFlow()
    }

    suspend fun disconnect() {
        debug("Going to close UDP socket...")
        mutex.withLock {
            debug("Closing UDP socket...")
            socket?.close()
            socket = null
        }
    }
}