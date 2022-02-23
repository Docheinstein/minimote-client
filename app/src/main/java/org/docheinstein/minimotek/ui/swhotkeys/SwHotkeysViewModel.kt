package org.docheinstein.minimotek.ui.swhotkeys

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.launch
import org.docheinstein.minimotek.AUTO_ID
import org.docheinstein.minimotek.database.hotkey.sw.SwHotkey
import org.docheinstein.minimotek.database.hotkey.sw.SwHotkeyRepository
import org.docheinstein.minimotek.di.IODispatcher
import org.docheinstein.minimotek.di.IOGlobalScope
import org.docheinstein.minimotek.keys.MinimoteKeyType
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn
import javax.inject.Inject

private const val DEFAULT_HOTKEY_X = 48
private const val DEFAULT_HOTKEY_Y = 48

@HiltViewModel
class SwHotkeysViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val swHotkeyRepository: SwHotkeyRepository
) : ViewModel() {


    companion object {
        private const val HOTKEY_ID_STATE_KEY = "hotkeyId"
        const val HOTKEY_ID_NONE = -1L
    }


    private val __swHotkeys = mutableListOf<SwHotkey>()
    private val _swHotkeys = MutableLiveData<List<SwHotkey>>()
    val swHotkeys: LiveData<List<SwHotkey>>
        get() = _swHotkeys

    private val _hasPendingChanges = MutableLiveData(false)
    val hasPendingChanges: LiveData<Boolean>
        get() = _hasPendingChanges

    // in-memory id
    // must be transled to AUTO_ID when the hotkeys are inserted into the db
    private var nextId = -1L

//    val swHotkeys = swHotkeyRepository.hotkeys.asLiveData()

    init {
        debug("SwHotkeysSharedViewModel.init()")
        viewModelScope.launch(ioDispatcher) {
            debug("Retrieving hotkeys")
            swHotkeyRepository.hotkeys.collect {
//            swHotkeyRepository.hotkeys.singleOrNull()?.let {
                debug("Hotkeys retrieved")
                for (h in it)
                    __swHotkeys.add(h)
                triggerUpdate(pendingChanges = false)
            }
        }
    }

    override fun onCleared() {
        debug("SwHotkeysSharedViewModel.onCleared()")
        super.onCleared()
    }

    fun commit() {
        // DB update

        debug("Would save ${__swHotkeys.size} hotkeys")

        ioScope.launch {
            swHotkeyRepository.clear()
            for (h in __swHotkeys) {
                // translate brand new hotkeys' ids to AUTO_ID before insert into the DB
                if (h.id < 0)
                    h.id = AUTO_ID
                swHotkeyRepository.save(h)
            }
        }
    }


    fun add(
        key: MinimoteKeyType,
        alt: Boolean,
        altgr: Boolean,
        ctrl: Boolean,
        meta: Boolean,
        shift: Boolean,
        label: String?
    ): SwHotkey {
        debug("Adding hotkey in-memory")

        val hotkey = SwHotkey(
            id = getNextId(),
            alt = alt,
            altgr = altgr,
            ctrl = ctrl,
            meta = meta,
            shift = shift,
            key = key,
            label = label,
            x = DEFAULT_HOTKEY_X,
            y = DEFAULT_HOTKEY_Y
        )

        __swHotkeys.add(hotkey)

        triggerUpdate()

        return hotkey
    }

    fun remove(id: Long) {
        debug("Removing hotkey in-memory")

        if (__swHotkeys.removeIf { swHotkey -> swHotkey.id == id })
            triggerUpdate()
        else
            warn("No hotkey with id $id to remove")
    }

    fun clear() {
        debug("Removing all in-memory hotkeys")
        __swHotkeys.clear()
        triggerUpdate()
    }


    fun edit(
        id: Long,
        key: MinimoteKeyType,
        alt: Boolean,
        altgr: Boolean,
        ctrl: Boolean,
        meta: Boolean,
        shift: Boolean,
        label: String?
    ): SwHotkey? {
        debug("Editing hotkey in-memory")

        var hotkey: SwHotkey? = null

        for (h in __swHotkeys) {
            if (h.id == id) {
                debug("Found hotkey with id $id, updating")
                hotkey = h
                h.alt = alt
                h.altgr = altgr
                h.ctrl = ctrl
                h.meta = meta
                h.shift = shift
                h.key = key
            }
        }

        triggerUpdate()

        return hotkey
    }

    fun updatePosition(id: Long, x: Int, y: Int) {
        // Memory update

        debug("Would update hotkey $id")

        var found = false
        for (h in __swHotkeys) {
            if (h.id == id) {
                debug("Actually updating pos for hotkey ${h.id}")
                h.x = x
                h.y = y
                found = true
            }
        }

        if (found) {
            debug("Hotkey $id position has been updated, triggering update")
            triggerUpdate()
        } else {
            warn("Failed to find hotkey with id $id, not triggering update")
        }
    }

    private fun triggerUpdate(pendingChanges: Boolean = true) {
        debug("Triggering update, size is = ${__swHotkeys.size}")
        _swHotkeys.postValue(__swHotkeys)
        _hasPendingChanges.postValue(pendingChanges)
    }

    private fun getNextId(): Long {
        return nextId--
    }
}