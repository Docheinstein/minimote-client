package org.docheinstein.minimotek.ui.swhotkeys

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.docheinstein.minimotek.AUTO_ID
import org.docheinstein.minimotek.BuildConfig
import org.docheinstein.minimotek.buttons.ButtonEventBus
import org.docheinstein.minimotek.database.hotkey.Hotkey
import org.docheinstein.minimotek.orientation.Orientation
import org.docheinstein.minimotek.database.hotkey.sw.SwHotkey
import org.docheinstein.minimotek.database.hotkey.sw.SwHotkeyRepository
import org.docheinstein.minimotek.di.IODispatcher
import org.docheinstein.minimotek.di.IOGlobalScope
import org.docheinstein.minimotek.keys.MinimoteKeyType
import org.docheinstein.minimotek.orientation.OrientationEventBus
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn
import javax.inject.Inject

private const val DEFAULT_HOTKEY_X = 48
private const val DEFAULT_HOTKEY_Y = 48

@HiltViewModel
class SwHotkeysViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val swHotkeyRepository: SwHotkeyRepository,
    private val orientationEventBus: OrientationEventBus,
) : ViewModel() {

    companion object {
        private const val HOTKEY_ID_STATE_KEY = "hotkeyId"
        const val HOTKEY_ID_NONE = Long.MIN_VALUE
    }

    // Hotkeys

    private val __portraitHotkeys = mutableListOf<SwHotkey>()
    private val _portraitHotkeys = MutableLiveData<List<SwHotkey>>()
    private val portraitHotkeys: LiveData<List<SwHotkey>>
        get() = _portraitHotkeys

    private val __landscapeHotkeys = mutableListOf<SwHotkey>()
    private val _landscapeHotkeys = MutableLiveData<List<SwHotkey>>()
    private val landscapeHotkeys: LiveData<List<SwHotkey>>
        get() = _landscapeHotkeys


    private fun __hotkeys(o: Orientation = orientationSnapshot): MutableList<SwHotkey> {
        return if (o == Orientation.Portrait) __portraitHotkeys else __landscapeHotkeys
    }

    private fun _hotkeys(o: Orientation = orientationSnapshot): MutableLiveData<List<SwHotkey>> {
        return if (o == Orientation.Portrait) _portraitHotkeys else _landscapeHotkeys
    }

    fun hotkeys(o: Orientation = orientationSnapshot):  LiveData<List<SwHotkey>> {
        return if (o == Orientation.Portrait) portraitHotkeys else landscapeHotkeys
    }

    fun hotkey(id: Long):  SwHotkey? {
        for (h in __portraitHotkeys)
            if (h.id == id)
                return h
        for (h in __landscapeHotkeys)
            if (h.id == id)
                return h
        warn("No hotkey for id $id")
        return null
    }

    // Pending changes

    private val _portraitHasPendingChanges = MutableLiveData(false)
    private val portraitHasPendingChanges: LiveData<Boolean>
        get() = _portraitHasPendingChanges

    private val _landscapeHasPendingChanges = MutableLiveData(false)
    private val landscapeHasPendingChanges: LiveData<Boolean>
        get() = _landscapeHasPendingChanges

    private fun _hasPendingChanges(o: Orientation = orientationSnapshot): MutableLiveData<Boolean> {
        return if (o == Orientation.Portrait) _portraitHasPendingChanges else _landscapeHasPendingChanges
    }

    fun hasPendingChanges(o: Orientation = orientationSnapshot): LiveData<Boolean> {
        return if (o == Orientation.Portrait) portraitHasPendingChanges else landscapeHasPendingChanges
    }

    // in-memory id
    // must be transled to AUTO_ID when the hotkeys are inserted into the db
    private var nextId = -1L

    var orientation = orientationEventBus.orientation.asLiveData()
    
    val orientationSnapshot: Orientation
        get() = orientationEventBus.orientation.value

    init {
        debug("SwHotkeysSharedViewModel.init()")
        viewModelScope.launch(ioDispatcher) {
            for (h in swHotkeyRepository.getAll(Orientation.Portrait))
                __portraitHotkeys.add(h.copy()) // deep copy
            for (h in swHotkeyRepository.getAll(Orientation.Landscape))
                __landscapeHotkeys.add(h.copy()) // deep copy
            _portraitHotkeys.postValue(__portraitHotkeys)
            _landscapeHotkeys.postValue(__landscapeHotkeys)

            debug("Hotkeys retrieved")
            debug("PORTRAIT: ${__portraitHotkeys.size}")
            for (h in __portraitHotkeys)
                debug("$h")
            debug("LANDSCAPE: ${__landscapeHotkeys.size}")
            for (h in __landscapeHotkeys)
                debug("$h")
        }
    }

    override fun onCleared() {
        debug("SwHotkeysSharedViewModel.onCleared()")
        super.onCleared()
    }

    fun commit() {
        // DB update

        val o = orientationSnapshot // take a snapshot of the orientation

        debug("Would save ${__hotkeys(o).size} hotkeys for orientation $o")

        ioScope.launch {
            val hotkeys = __hotkeys(o)

        if (BuildConfig.DEBUG)
            assert(hotkeys.associateBy { h -> h.id }.size == hotkeys.size)

            for (h in __hotkeys(o)) {
                debug("Will save $h")
                assert(h.orientation == o)
                // translate brand new hotkeys' ids to AUTO_ID before insert into the DB
                if (h.id < 0)
                    h.id = AUTO_ID
            }
            swHotkeyRepository.replaceForOrientation(o, hotkeys) // update db in a transaction

            // retrieve the new ids
            debug("Retrieving the new hotkeys from db")
            hotkeys.clear()
            for (h in swHotkeyRepository.getAll(o)) {
                debug("$h")
                hotkeys.add(h.copy()) // deep copy
            }

            triggerUpdate(o, false) // must update hotkeys too since auto inserted ids are different from before
//            _hasPendingChanges(o).postValue(false) // no more pending changes
        }
    }


    fun add(
        key: MinimoteKeyType,
        alt: Boolean,
        altgr: Boolean,
        ctrl: Boolean,
        meta: Boolean,
        shift: Boolean,
        label: String?,
        size: Int
    ): SwHotkey {
        debug("Adding hotkey in-memory")
        val o = orientationSnapshot // take a snapshot

        val hotkey = SwHotkey(
            id = getNextId(),
            alt = alt,
            altgr = altgr,
            ctrl = ctrl,
            meta = meta,
            shift = shift,
            key = key,
            label = label,
            orientation = o,
            x = DEFAULT_HOTKEY_X,
            y = DEFAULT_HOTKEY_Y,
            size = size
        )

        __hotkeys(o).add(hotkey)

        triggerUpdate(o)

        return hotkey
    }

    fun remove(id: Long) {
        debug("Removing hotkey in-memory")
        if (__portraitHotkeys.removeIf { swHotkey -> swHotkey.id == id })
            triggerUpdate(Orientation.Portrait)

        if (__landscapeHotkeys.removeIf { swHotkey -> swHotkey.id == id })
            triggerUpdate(Orientation.Landscape)
    }

    fun clear() {
        debug("Removing all in-memory hotkeys")
        val o = orientationSnapshot // take a snapshot
        __hotkeys(o).clear()
        triggerUpdate(o)
    }

    fun edit(
        id: Long,
        key: MinimoteKeyType,
        alt: Boolean,
        altgr: Boolean,
        ctrl: Boolean,
        meta: Boolean,
        shift: Boolean,
        label: String?,
        size: Int
    ): SwHotkey? {
        debug("Editing hotkey with id $id in-memory")
        var hotkey: SwHotkey? = null

        val editHotkey: ((SwHotkey) -> SwHotkey) = { h ->
            h.alt = alt
            h.altgr = altgr
            h.ctrl = ctrl
            h.meta = meta
            h.shift = shift
            h.key = key
            h.label = label
            h.size = size
            h
        }

        for (h in __portraitHotkeys) { // look in both
            if (h.id == id) {
                debug("Found hotkey with id $id in portrait hotkeys, updating")
                hotkey = editHotkey(h)
                triggerUpdate(Orientation.Portrait)
                return hotkey
            }
        }

        for (h in __landscapeHotkeys) { // look in both
            if (h.id == id) {
                debug("Found hotkey with id $id in landscape hotkeys, updating")
                hotkey = editHotkey(h)
                triggerUpdate(Orientation.Landscape)
                return hotkey
            }
        }

        return hotkey
    }

    fun updatePosition(id: Long, x: Int, y: Int) {
        // Memory update
        val o = orientationSnapshot // take a snapshot

        debug("Would update hotkey $id")

        var found = false
        val hotkeys = __hotkeys(o)

        if (BuildConfig.DEBUG)
            assert(hotkeys.associateBy { h -> h.id }.size == hotkeys.size)

        for (h in __hotkeys(o)) {
            if (h.id == id) {
                debug("Actually updating pos for hotkey ${h.id}")
                h.x = x
                h.y = y
                found = true
//                break
            }
        }

        if (found) {
            debug("Hotkey $id position has been updated, triggering update")
            triggerUpdate(o)
        } else {
            warn("Failed to find hotkey with id $id, not triggering update")
        }
    }

    fun import(maxX: Int?, maxY: Int?) {
        val o = orientationSnapshot
        debug("Importing hotkeys from ${if (o == Orientation.Portrait) Orientation.Landscape else Orientation.Portrait} to $o ")

        __hotkeys(o).clear()
        for (h in __hotkeys(if (o == Orientation.Portrait) Orientation.Landscape else Orientation.Portrait)) {
            debug("Importing $h")
            var offscreen = false
            val MIN_AREA_FOR_GRAB_HOTKEY = 50
            if (maxX != null && h.x + MIN_AREA_FOR_GRAB_HOTKEY > maxX) {
                warn("Hotkey ${h.key} will probably be off-screen (x=${h.x}, maxX=$maxX), MIN_AREA_FOR_GRAB_HOTKEY=$MIN_AREA_FOR_GRAB_HOTKEY")
                offscreen = true
            }
            if (maxY != null && h.y + MIN_AREA_FOR_GRAB_HOTKEY > maxY) {
                warn("Hotkey ${h.key} will probably be off-screen (y=${h.y}, maxY=$maxY), MIN_AREA_FOR_GRAB_HOTKEY=$MIN_AREA_FOR_GRAB_HOTKEY")
                offscreen = true
            }

//            if (o == Orientation.Portrait) {
//                if (h.x > h.y) {
//                    warn("Hotkey will probably be off-screen in Portrait: $h ")
//                }
//            } else if (o == Orientation.Landscape) {
//                if (h.y > h.x) {
//                    warn("Hotkey will probably be off-screen in Landscape: $h ")
//                }
//            }
            val x = if (!offscreen) h.x else DEFAULT_HOTKEY_X
            val y = if (!offscreen) h.y else DEFAULT_HOTKEY_Y

            __hotkeys(o).add(
                h.copy(
                    id = getNextId(),
                    orientation = o,
                    x = x,
                    y = y
                )
            ) // deep copy, but change orientation
        }
        triggerUpdate(o)
    }

    private fun triggerUpdate(o: Orientation = orientationSnapshot, pendingChanges: Boolean = true) {
        debug("Triggering hotkeys update for orientation $o, size is = ${__hotkeys(o).size}, pending changes = $pendingChanges")
        _hotkeys(o).postValue(__hotkeys(o))
        _hasPendingChanges(o).postValue(pendingChanges)
    }

    private fun getNextId(): Long {
        return nextId--
    }
}