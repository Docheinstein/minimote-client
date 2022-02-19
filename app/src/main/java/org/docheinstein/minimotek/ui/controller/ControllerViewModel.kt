package org.docheinstein.minimotek.ui.controller

import android.view.MotionEvent
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import org.docheinstein.minimotek.*
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
import java.lang.Exception
import java.net.InetSocketAddress
import javax.inject.Inject
import kotlin.math.abs
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
    
    val isConnected
        get() = _connectionState.value == ConnectionState.Connected

    companion object {
        private const val SERVER_ADDRESS_STATE_KEY = "address"
        private const val SERVER_PORT_STATE_KEY = "port"
    }

    val serverAddress: String = savedStateHandle[SERVER_ADDRESS_STATE_KEY]!!
    val serverPort: Int = savedStateHandle[SERVER_PORT_STATE_KEY]!!

    private val connection = MinimoteConnection(serverAddress, serverPort)
    
    private var currentTouchEventId = 0

    // Movement handling
    private var lastMovementSampleTime: Long = 0

    // Click handling
    private var lastClickTouchEventId = 0
    private var lastDownX = 0
    private var lastDownY = 0

    // Scroll handling
    private var lastScrollTime: Long = 0
    private var lastScrollY = 0

    init {
        debug("ControllerViewModel.init()")

        viewModelScope.launch( ioDispatcher) {
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
        ioScope.launch() {
            connection.disconnect()
        }
    }

    fun leftClick() {
        if (!isConnected)
            return
        
        viewModelScope.launch {
            connection.sendUdp(MinimotePacketFactory.newLeftClick())
        }
    }

    fun leftDown() {
        if (!isConnected)
            return

        viewModelScope.launch {
            connection.sendUdp(MinimotePacketFactory.newLeftDown())
        }
    }

    fun leftUp() {
        if (!isConnected)
            return

        viewModelScope.launch {
            connection.sendUdp(MinimotePacketFactory.newLeftUp())
        }
    }
    
    fun middleClick() {
        if (!isConnected)
            return
        
        viewModelScope.launch {
            connection.sendUdp(MinimotePacketFactory.newMiddleClick())
        }
    }
    
    fun rightClick() {
        if (!isConnected)
            return
        
        viewModelScope.launch {
            connection.sendUdp(MinimotePacketFactory.newRightClick())
        }
    }
    
    fun touchpadDown(ev: MotionEvent) {
        if (!isConnected)
            return
        increaseMovementId()
        lastDownX = ev.x.roundToInt()
        lastDownY = ev.y.roundToInt()
    }

    fun touchpadUp(ev: MotionEvent) {
        if (!isConnected)
            return

        if (lastClickTouchEventId == currentTouchEventId)
            // prevent duplicate click
            return

        // check click area
        if (abs(ev.x.roundToInt() - lastDownX) > CLICK_AREA ||
            abs(ev.y.roundToInt() - lastDownY) > CLICK_AREA)
                return

        lastClickTouchEventId = currentTouchEventId

        viewModelScope.launch {
            connection.sendUdp(MinimotePacketFactory.newLeftClick())
        }

    }

    fun touchpadPointerDown(ev: MotionEvent) {
        if (!isConnected)
            return

        increaseMovementId()
        lastScrollTime = ev.eventTime
        lastScrollY = ev.y.roundToInt()
    }

    fun touchpadPointerUp(ev: MotionEvent) {
        if (!isConnected)
            return

        if (lastClickTouchEventId == currentTouchEventId)
            // prevent duplicate click
            return

        if (ev.pointerCount <= 1)
            return

        var clickPacket: MinimotePacket? = null
        if (ev.pointerCount == 2)
            clickPacket = MinimotePacketFactory.newRightClick()
        else if (ev.pointerCount == 3)
            clickPacket = MinimotePacketFactory.newMiddleClick()

        if (clickPacket == null)
            return

        lastClickTouchEventId = currentTouchEventId

        viewModelScope.launch {
            connection.sendUdp(clickPacket)
        }
    }

    fun touchpadMovement(ev: MotionEvent) {
        if (!isConnected)
            return
        debug("Movement pointer count = ${ev.pointerCount}")

        if (ev.pointerCount > 1) {
            if (ev.eventTime - lastScrollTime < SCROLL_MIN_TIME_BETWEEN_SAMPLES)
                // do not exceed the sample rate
                return

            val deltaScroll = lastScrollY - ev.y.roundToInt()

            debug("Scroll delta = $deltaScroll")

            var scrollPacket: MinimotePacket? = null
            if (deltaScroll > SCROLL_DELTA_FOR_TICK)
                scrollPacket = MinimotePacketFactory.newScrollUp()
            else if (deltaScroll < -SCROLL_DELTA_FOR_TICK)
                scrollPacket = MinimotePacketFactory.newScrollDown()

            if (scrollPacket == null)
                return

            lastScrollTime = ev.eventTime
            lastScrollY = ev.y.roundToInt()

            viewModelScope.launch {
                connection.sendUdp(scrollPacket)
            }
        } else {
            if (ev.eventTime - lastMovementSampleTime < MOVEMENT_MIN_TIME_BETWEEN_SAMPLES)
                // do not exceed the sample rate
                return

            lastMovementSampleTime = ev.eventTime

            viewModelScope.launch {
                connection.sendUdp(MinimotePacketFactory.newMove(currentTouchEventId, ev.x.roundToInt(), ev.y.roundToInt()))
            }
        }
    }

    private fun increaseMovementId() {
        currentTouchEventId = (currentTouchEventId + 1) % MAX_MOVEMENT_ID
        debug("New MID = $currentTouchEventId")
    }
}