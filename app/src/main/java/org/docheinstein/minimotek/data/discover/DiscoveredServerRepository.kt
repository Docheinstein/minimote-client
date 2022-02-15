package org.docheinstein.minimotek.data.discover

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.docheinstein.minimotek.util.debug
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToLong
import kotlin.random.Random

@Singleton
class DiscoveredServerRepository @Inject constructor() {
    fun discoverServers(): Flow<DiscoveredServer> {
//        val socket = AsynchronousSocketChannel.open()
//        val dest = InetSocketAddress("192.168.1.106", 8080)
        return flow {
//            socket.connect(dest).runCatching {  }

            debug("DiscoveredServerRepository.discoverServers")
            for (i in 0..10) {
                val sx = DiscoveredServer("192.168.1.110", 50500, "stefano-$i")
                emit(sx)
//                delay((2000 * Random.nextFloat()).roundToLong())
                delay(1000)
            }
//            val exec = Executors.newCachedThreadPool()
//            val selector = ActorSelectorManager(exec.asCoroutineDispatcher())
//            val udpSocketBuilder = aSocket(selector).udp()
//            val udpSocket = udpSocketBuilder.bind(InetSocketAddress("0.0.0.0", 50500))
//
//            debug("Listening...")
//            while (true) {
//                val datagram = udpSocket.receive()
//                debug("Received datagram")
//            }
        }
    }
}