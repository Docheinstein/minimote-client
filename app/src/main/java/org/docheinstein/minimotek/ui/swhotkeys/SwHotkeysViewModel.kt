package org.docheinstein.minimotek.ui.swhotkeys

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.docheinstein.minimotek.AUTO_ID
import org.docheinstein.minimotek.BuildConfig
import org.docheinstein.minimotek.orientation.Orientation
import org.docheinstein.minimotek.database.hotkey.sw.SwHotkey
import org.docheinstein.minimotek.database.hotkey.sw.SwHotkeyRepository
import org.docheinstein.minimotek.di.IODispatcher
import org.docheinstein.minimotek.di.IOGlobalScope
import org.docheinstein.minimotek.keys.MinimoteKeyType
import org.docheinstein.minimotek.orientation.OrientationEventBus
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.verbose
import org.docheinstein.minimotek.util.warn
import javax.inject.Inject

private const val DEFAULT_HOTKEY_X = 48
private const val DEFAULT_HOTKEY_Y = 48
private const val MIN_ESTIMATED_AREA_FOR_GRAB_HOTKEY = 50

/**
 * This view model keeps and handles the current hotkeys for both orientation, separately.
 * All the changes from the UI to the hotkeys are in-memory,
 * apart from [commit] which actually pushes the changes to the DB.
 */
@HiltViewModel
class SwHotkeysViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val swHotkeyRepository: SwHotkeyRepository,
    private val orientationEventBus: OrientationEventBus,
) : ViewModel() {

    // Hotkeys (for both orientations)

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

    // Pending changes (for both orientations)

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

    // Next in-memory ID.
    // This id goes backward so that it won't conflict with the DB generated IDs.
    // All the negative IDs must be translated into AUTO_ID (0) when the
    // hotkeys are inserted into the DB, so that they could acquire a positive ID automatically.
    private var nextId = -1L
    private fun getNextId() = nextId--

    var orientation = orientationEventBus.orientation.asLiveData()
    
    val orientationSnapshot: Orientation
        get() = orientationEventBus.orientation.value

    init {
        verbose("SwHotkeysViewModel.init()")

        viewModelScope.launch(ioDispatcher) {
            // Retrieve the hotkeys for both orientation, just the first time,
            // and makes a copy so that DB updates won't be reflected to in-memory hotkeys
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
        verbose("SwHotkeysViewModel.onCleared()")
    }

    fun commit() {
        // Push in-memory hotkeys to DB for the current orientation

        val o = orientationSnapshot // take a snapshot of the orientation
        val H = __hotkeys(o)

        debug("Going to save ${H.size} hotkeys for orientation $o in DB")

        ioScope.launch {
            if (BuildConfig.DEBUG)
                assert(H.associateBy { h -> h.id }.size == H.size)

            for (h in H) {
                debug("Will save hotkey $h")
                assert(h.orientation == o)
                // Translate brand new hotkeys' ids to AUTO_ID before insert them into the DB
                if (h.id < 0)
                    h.id = AUTO_ID
            }

            // Replace all the hotkeys for the current orientation in a transaction
            swHotkeyRepository.replaceAllForOrientation(o, H)

            // Invalidate the current hotkeys and retrieve the new
            // hotkeys with the auto-generated IDs
            debug("Retrieving the new hotkeys from db")
            H.clear()
            for (h in swHotkeyRepository.getAll(o)) {
                debug("$h")
                H.add(h.copy()) // deep copy
            }

            // Must trigger an update since hotkeys might have different ids then before
            triggerUpdate(o, false)
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
        textSize: Int,
        horizontalPadding: Int,
        verticalPadding: Int
    ): SwHotkey {
        // Add in-memory hotkey for the current orientation

        val o = orientationSnapshot // take a snapshot of the orientation
        val H = __hotkeys(o)

        debug("Adding hotkey in-memory to orientation $o")

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
            textSize = textSize,
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding
        )

        H.add(hotkey)

        triggerUpdate(o)

        return hotkey
    }

    fun remove(id: Long) {
        // Remove in-memory hotkey

        // Look in both orientation

        debug("Removing hotkey $id in-memory")
        if (__portraitHotkeys.removeIf { swHotkey -> swHotkey.id == id }) {
            debug("Found hotkey with id $id in portrait hotkeys, updating")
            triggerUpdate(Orientation.Portrait)
        }

        if (__landscapeHotkeys.removeIf { swHotkey -> swHotkey.id == id }) {
            debug("Found hotkey with id $id in landscape hotkeys, updating")
            triggerUpdate(Orientation.Landscape)
        }
    }

    fun clear() {
        // Remove all the in-memory hotkeys for the current orientation

        val o = orientationSnapshot // take a snapshot of the orientation
        val H = __hotkeys(o)

        debug("Removing all in-memory hotkeys for orientation $o")

        H.clear()
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
        textSize: Int,
        horizontalPadding: Int,
        verticalPadding: Int
    ): SwHotkey? {
        // Update an in-memory hotkey

        debug("Editing hotkey $id in-memory")

        var hotkey: SwHotkey? = null

        val doEditHotkey: ((SwHotkey) -> SwHotkey) = { h ->
            h.alt = alt
            h.altgr = altgr
            h.ctrl = ctrl
            h.meta = meta
            h.shift = shift
            h.key = key
            h.label = label
            h.textSize = textSize
            h.horizontalPadding = horizontalPadding
            h.verticalPadding = verticalPadding
            h
        }

        // Look in both orientation

        for (h in __portraitHotkeys) {
            if (h.id == id) {
                debug("Found hotkey with id $id in portrait hotkeys, updating")
                hotkey = doEditHotkey(h)
                triggerUpdate(Orientation.Portrait)
                return hotkey
            }
        }

        for (h in __landscapeHotkeys) {
            if (h.id == id) {
                debug("Found hotkey with id $id in landscape hotkeys, updating")
                hotkey = doEditHotkey(h)
                triggerUpdate(Orientation.Landscape)
                return hotkey
            }
        }

        return hotkey
    }

    fun updatePosition(id: Long, x: Int, y: Int) {
        // Update hotkey position in-memory

        val o = orientationSnapshot // take a snapshot of the orientation
        val H = __hotkeys(o)

        debug("Updating hotkey $id position to (X=$x,Y=$y) $id for orientation $o")

        var found = false

        if (BuildConfig.DEBUG)
            assert(H.associateBy { h -> h.id }.size == H.size)

        for (h in H) {
            if (h.id == id) {
                debug("Found hotkey with id $id, updating position")
                h.x = x
                h.y = y
                found = true
                break
            }
        }

        if (found) {
            debug("Hotkey $id position has been updated, triggering update")
            triggerUpdate(o)
        }
    }

    fun import(maxX: Int?, maxY: Int?) {
        // Import hotkeys from the opposite orientation to the current orientation

        val o = orientationSnapshot // take a snapshot of the orientation
        val notO = !o
        val H = __hotkeys(o)
        val notH = __hotkeys(notO)

        debug("Importing hotkeys from $notO to $o ")

        H.clear()
        for (h in notH) {
            debug("Importing hotkey $h")

            // Before importing brainless, check whether the the coordinates of the hotkey
            // in the other orientation are still legal in the current orientation.
            // If not, set a default x,y for the hotkeys offscreen (e.g. top left corner)

            var isOffscreen = false
            if (maxX != null && h.x + MIN_ESTIMATED_AREA_FOR_GRAB_HOTKEY > maxX) {
                warn("Hotkey ${h.key} will probably be off-screen " +
                        "(x=${h.x}, maxX=$maxX, MIN_ESTIMATED_AREA_FOR_GRAB_HOTKEY=$MIN_ESTIMATED_AREA_FOR_GRAB_HOTKEY)")
                isOffscreen = true
            }
            if (maxY != null && h.y + MIN_ESTIMATED_AREA_FOR_GRAB_HOTKEY > maxY) {
                warn("Hotkey ${h.key} will probably be off-screen " +
                        "(y=${h.y}, maxY=$maxY, MIN_ESTIMATED_AREA_FOR_GRAB_HOTKEY=$MIN_ESTIMATED_AREA_FOR_GRAB_HOTKEY)")
                isOffscreen = true
            }

            val x = if (!isOffscreen) h.x else DEFAULT_HOTKEY_X
            val y = if (!isOffscreen) h.y else DEFAULT_HOTKEY_Y

            H.add(
                h.copy(
                    id = getNextId(),
                    orientation = o,
                    x = x,
                    y = y
                )
            ) // deep copy, but change orientation and coordinates
        }
        triggerUpdate(o)
    }

    private fun triggerUpdate(o: Orientation = orientationSnapshot, pendingChanges: Boolean = true) {
        debug("Triggering hotkeys update for orientation $o, " +
                "size is = ${__hotkeys(o).size}, pending changes = $pendingChanges")
        _hotkeys(o).postValue(__hotkeys(o))
        _hasPendingChanges(o).postValue(pendingChanges)
    }
}