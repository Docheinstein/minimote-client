package org.docheinstein.minimotek.net

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.docheinstein.minimotek.extensions.toBinaryString
import org.docheinstein.minimotek.util.asMessage
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.error
import java.io.IOException
import java.net.InetSocketAddress

class UdpSocket(
    val localAddress: String = "0.0.0.0" /* auto */,
    val localPort: Int = 0 /* auto */,
) {
    private var socket: BoundDatagramSocket? = null
    private val builder = aSocket(ActorSelectorManager(Dispatchers.IO)).udp()
    private val mutex = Mutex()

    private val uid: Int
        get() = hashCode() % 100

    val boundLocalAddress: String
        get() = socket!!.localAddress.hostname

    val boundLocalPort: Int
        get() = socket!!.localAddress.port

    suspend fun bind(configure: SocketOptions.UDPSocketOptions.() -> Unit = {}) {
        debug("Going to create UDP socket [$uid]...")
        mutex.withLock {
            debug("Creating UDP socket [$uid]...")
            socket = builder.bind(InetSocketAddress(localAddress, localPort), configure)
        }
    }

    suspend fun send(data: ByteArray, remoteAddress: String, remotePort: Int) {
        debug("Going to send on UDP socket [$uid]...")
        mutex.withLock {
            debug(">> Sending UDP [$uid]: ${data.toBinaryString(pretty = true)}" )
            socket!!.send(Datagram(
                BytePacketBuilder(data.size). also { it.writeFully(data) }.build(),
                InetSocketAddress(remoteAddress, remotePort))
            )
        }
    }

    suspend fun recv(): ByteArray {
        debug("Going to recv on UDP socket [$uid]...")
        mutex.withLock {
            debug(">> Waiting UDP [$uid]..." )
            val data = socket!!.incoming.receive().packet.readBytes()
            debug(">> Received UDP [$uid]: ${data.toBinaryString(pretty = true)}" )
            return data
        }
    }

    suspend fun disconnect() {
        debug("Going to close UDP socket [$uid]...")
        mutex.withLock {
            debug("Closing UDP socket [$uid]...")
            socket?.close()
            socket = null
        }
    }

    fun isOpen(): Boolean {
        return socket?.isClosed == false
    }
}