package org.docheinstein.minimotek.ui.server

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.util.date.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.docheinstein.minimotek.database.server.Server
import org.docheinstein.minimotek.database.server.ServerRepository
import org.docheinstein.minimotek.di.IOGlobalScope
import org.docheinstein.minimotek.ui.hwhotkey.AddEditHwHotkeyViewModel
import org.docheinstein.minimotek.util.debug
import javax.inject.Inject

@HiltViewModel
class AddEditServerViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    private val serverRepository: ServerRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        private const val SERVER_ID_STATE_KEY = "serverId"
        const val SERVER_ID_NONE = -1L
    }

    enum class Mode {
        ADD,
        EDIT
    }

    private val serverId: Long = savedStateHandle[SERVER_ID_STATE_KEY] ?: SERVER_ID_NONE
    val mode = if (serverId != SERVER_ID_NONE) Mode.EDIT else AddEditHwHotkeyViewModel.Mode.ADD
    val server = if (mode == Mode.EDIT) serverRepository.load(serverId).asLiveData() else null

    init {
        debug("AddEditServerViewModel.init() for serverId = $serverId")
    }

    fun insert(s: Server) {
        ioScope.launch {
            debug("insert.launch executed on thread = ${Thread.currentThread()}")
            serverRepository.add(s)
        }
    }

    fun update(s: Server) {
        ioScope.launch {
            serverRepository.update(s)
        }
    }

    fun delete() {
        ioScope.launch {
            serverRepository.delete(server?.value!!)
        }
    }
}