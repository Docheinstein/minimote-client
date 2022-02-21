package org.docheinstein.minimotek.ui.controller

import android.view.MotionEvent
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.util.date.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.docheinstein.minimotek.*
import org.docheinstein.minimotek.buttons.ButtonEventBus
import org.docheinstein.minimotek.buttons.ButtonType
import org.docheinstein.minimotek.connection.MinimoteConnection
import org.docheinstein.minimotek.database.hwhotkey.HwHotkeyRepository
import org.docheinstein.minimotek.di.IODispatcher
import org.docheinstein.minimotek.di.IOGlobalScope
import org.docheinstein.minimotek.keys.MinimoteKeyType
import org.docheinstein.minimotek.packet.MinimotePacket
import org.docheinstein.minimotek.packet.MinimotePacketFactory
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

@HiltViewModel
class ControllerViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val buttonEventBus: ButtonEventBus,
    private val hwHotkeyRepository: HwHotkeyRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {


    enum class ConnectionState {
        Connecting,
        Connected,
        Disconnected
    }

    companion object {
        private const val SERVER_ADDRESS_STATE_KEY = "address"
        private const val SERVER_PORT_STATE_KEY = "port"
    }
    // Connection

    val serverAddress: String = savedStateHandle[SERVER_ADDRESS_STATE_KEY]!!
    val serverPort: Int = savedStateHandle[SERVER_PORT_STATE_KEY]!!

    private val _connectionState = MutableLiveData(ConnectionState.Connecting)
    val connectionState: LiveData<ConnectionState>
        get() = _connectionState
    
    val isConnected
        get() = _connectionState.value == ConnectionState.Connected

    private val connection = MinimoteConnection(serverAddress, serverPort)
    private var lastConnectionCheckTime: Long = 0
    
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

    // Widgets
    private val _touchpadButtons = MutableLiveData(true)
    val touchpadButtons: LiveData<Boolean>
        get() = _touchpadButtons

    private val _keyboard = MutableLiveData(false)
    val keyboard: LiveData<Boolean>
        get() = _keyboard

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
            lastConnectionCheckTime = getTimeMillis()
            _connectionState.postValue(ConnectionState.Connected)
        }

//        buttonEventBus.events
//            .onEach { btn ->
//                debug("ControllerViewModel notified about button press")
//                delay(5000)
//            }
        viewModelScope.launch {
            debug("Going to collect buttonEventBus events")
            buttonEventBus.events.collect { btn ->
                debug("Collected button press")
                withContext(ioDispatcher) {
                    handleButtonPressed(btn)
                }
                debug("Button handled in ControllerViewModel")
            }
            debug("Finished to collect buttonEventBus events")
        }
    }

    override fun onCleared() {
        debug("ControllerViewModel.onCleared")
        ioScope.launch {
            connection.disconnect()
        }
    }

    fun leftDown() {
        if (!isConnected)
            return

        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendUdp(MinimotePacketFactory.newLeftDown())
        }
    }

    fun leftUp() {
        if (!isConnected)
            return

        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendUdp(MinimotePacketFactory.newLeftUp())
        }
    }

    fun rightDown() {
        if (!isConnected)
            return

        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendUdp(MinimotePacketFactory.newRightDown())
        }
    }

    fun rightUp() {
        if (!isConnected)
            return

        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendUdp(MinimotePacketFactory.newRightUp())
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

        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendUdp(MinimotePacketFactory.newLeftClick())
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

        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendUdp(clickPacket)
        }
    }

    fun touchpadMovement(ev: MotionEvent) {
        if (!isConnected)
            return
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

            viewModelScope.launch(ioDispatcher) {
                checkConnectionAndSendUdp(scrollPacket)
            }
        } else {
            if (ev.eventTime - lastMovementSampleTime < MOVEMENT_MIN_TIME_BETWEEN_SAMPLES)
                // do not exceed the sample rate
                return

            lastMovementSampleTime = ev.eventTime

            // IMPORTANT
            // compute outside of coroutine, otherwise these
            // may change, for some obscure reason
            val x = ev.x.roundToInt()
            val y = ev.y.roundToInt()

            viewModelScope.launch(ioDispatcher) {
                checkConnectionAndSendUdp(MinimotePacketFactory.newMove(currentTouchEventId, x, y))
            }
        }
    }

    fun write(c: Char) {
        if (!isConnected)
            return
        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendTcp(MinimotePacketFactory.newWrite(c))
        }
    }

    fun keyClick(keyCode: Int) {
        if (!isConnected)
            return
        val key = MinimoteKeyType.byKeyCode(keyCode)
        if (key == null) {
            warn("No key for keyCode $keyCode")
            return
        }
        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendTcp(MinimotePacketFactory.newKeyClick(key))
        }
    }

    fun keyDown(keyCode: Int) {
        if (!isConnected)
            return
        val key = MinimoteKeyType.byKeyCode(keyCode)
        if (key == null) {
            warn("No key for keyCode $keyCode")
            return
        }
        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendTcp(MinimotePacketFactory.newKeyDown(key))
        }
    }

    fun keyUp(keyCode: Int) {
        if (!isConnected)
            return
        val key = MinimoteKeyType.byKeyCode(keyCode)
        if (key == null) {
            warn("No key for keyCode $keyCode")
            return
        }
        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendTcp(MinimotePacketFactory.newKeyUp(key))
        }
    }

    fun openKeyboard() {
        _keyboard.value = true
    }

    fun closeKeyboard() {
        _keyboard.value = false
    }

    fun toggleKeyboard() {
        _keyboard.value = !(_keyboard.value ?: false)
    }

    fun showTouchpadButtons() {
        _touchpadButtons.value = true
    }

    fun hideTouchpadButtons() {
        _touchpadButtons.value = false
    }

    fun toggleTouchpadButtons() {
        _touchpadButtons.value = !(_touchpadButtons.value ?: false)
    }

    private suspend fun handleButtonPressed(button: ButtonType) {
        debug("ControllerViewModel notified about press of button $button")

        val hwHotkey = hwHotkeyRepository.get(button)

        if (hwHotkey == null) {
            debug("No mapping for button $hwHotkey")
            return
        }

        debug("Retrieved hotkey for button: $hwHotkey")

        val keys = mutableListOf<MinimoteKeyType>()

        // Modifiers
        if (hwHotkey.shift)
            keys.add(MinimoteKeyType.ShiftLeft)
        if (hwHotkey.ctrl)
            keys.add(MinimoteKeyType.CtrlLeft)
        if (hwHotkey.alt)
            keys.add(MinimoteKeyType.AltLeft)
        if (hwHotkey.altgr)
            keys.add(MinimoteKeyType.AltGr)
        if (hwHotkey.meta)
            keys.add(MinimoteKeyType.MetaLeft)

        // Base
        keys.add(hwHotkey.key)

        checkConnectionAndSendTcp(MinimotePacketFactory.newHotkey(keys))

    }

    private suspend fun checkConnectionAndSendUdp(packet: MinimotePacket): Boolean {
        return isConnectionStillAlive() && connection.sendUdp(packet)
    }

    private suspend fun checkConnectionAndSendTcp(packet: MinimotePacket): Boolean {
        return isConnectionStillAlive() && connection.sendTcp(packet)
    }

    private suspend fun isConnectionStillAlive(): Boolean {
        if (getTimeMillis() - lastConnectionCheckTime > CONNECTION_KEEP_ALIVE_TIME) {
            debug("Checking whether connection is still alive...")
            // too much time passed from last ping, check connection is still alive
            if (!connection.ensureConnection()) {
                _connectionState.postValue(ConnectionState.Disconnected)
                return false
            }
        }
        lastConnectionCheckTime = getTimeMillis()
        return true
    }

    private fun increaseMovementId() {
        currentTouchEventId = (currentTouchEventId + 1) % MAX_MOVEMENT_ID
        debug("New MID = $currentTouchEventId")
    }
}