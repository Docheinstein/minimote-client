package org.docheinstein.minimote.sockets

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.docheinstein.minimote.util.toBinaryString
import org.docheinstein.minimote.util.debug

/**
 * TCP socket.
 * Actually this is a wrapper of ktor's TCP socket ([Socket]).
 */
// https://ktor.io/docs/servers-raw-sockets.html
class TcpSocket(
    val remoteAddress: String,
    val remotePort: Int
) {
    private var socket: Socket? = null
    private var output: ByteWriteChannel? = null
    private val builder = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
    private val mutex = Mutex()

    val isOpen: Boolean
        get() = socket?.isClosed == false

    suspend fun connect(configure: SocketOptions.TCPClientSocketOptions.() -> Unit = {}) {
        debug("Going to create TCP socket...")
        mutex.withLock {
            debug("Creating TCP socket...")
            socket = builder.connect(remoteAddress, remotePort, configure)
        }
    }

    suspend fun send(data: ByteArray) {
        debug("Going to send on TCP socket...")
        mutex.withLock {
            if (output == null)
                output = socket!!.openWriteChannel(autoFlush = true)
            debug(">> Sending TCP: ${data.toBinaryString(pretty = true)}" )
            output!!.writeFully(data)
        }
    }

    suspend fun disconnect() {
        debug("Going to close TCP socket...")
        mutex.withLock {
            debug("Closing TCP socket...")
            socket?.close()
            socket = null
        }
    }
}