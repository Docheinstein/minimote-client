package org.docheinstein.minimotek.ui.controller

import android.view.MotionEvent
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import org.docheinstein.minimotek.MAX_MOVEMENT_ID
import org.docheinstein.minimotek.connection.MinimoteConnection
import org.docheinstein.minimotek.di.IODispatcher
import org.docheinstein.minimotek.di.IOGlobalScope
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
import javax.inject.Inject
import kotlin.math.roundToInt


@HiltViewModel
class ControllerViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
//    private val serverRepository: ServerRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {


    enum class ConnectionState {
        Connecting,
        Connected,
        Disconnected
    }

    private val _connectionState = MutableLiveData(ConnectionState.Connecting)
    val connectionState: LiveData<ConnectionState>
        get() = _connectionState

    companion object {
        private const val SERVER_ADDRESS_STATE_KEY = "address"
        private const val SERVER_PORT_STATE_KEY = "port"
    }

    val serverAddress: String = savedStateHandle[SERVER_ADDRESS_STATE_KEY]!!
    val serverPort: Int = savedStateHandle[SERVER_PORT_STATE_KEY]!!

    private val connection = MinimoteConnection(serverAddress, serverPort)

    private var currentMovementId = 0

    init {
        debug("ControllerViewModel.init()")

        viewModelScope.launch(ioDispatcher) {
            if (!connection.connect()) {
                debug("MinimoteConnection.connect() failed")
                _connectionState.postValue(ConnectionState.Disconnected)
                return@launch
            }

            if (!connection.ensureConnection()) {
                debug("MinimoteConnection.ensureConnection() failed")
                _connectionState.postValue(ConnectionState.Disconnected)
                return@launch
            }

            // Connection is actually on
            _connectionState.postValue(ConnectionState.Connected)
        }
    }

    override fun onCleared() {
        debug("ControllerViewModel.onCleared")
        connection.disconnect()
    }

    fun touchpadUp(ev: MotionEvent) {
        increaseMovementId()
    }

    fun touchpadPointerUp(ev: MotionEvent) {
        increaseMovementId()
    }

    fun touchpadMovement(ev: MotionEvent) {
        viewModelScope.launch {
            connection.sendUdp(MinimotePacketFactory.newMove(currentMovementId, ev.x.roundToInt(), ev.y.roundToInt()))
        }
    }

    private fun increaseMovementId() {
        currentMovementId = (currentMovementId + 1) % MAX_MOVEMENT_ID
        debug("New MID = $currentMovementId")
    }
}