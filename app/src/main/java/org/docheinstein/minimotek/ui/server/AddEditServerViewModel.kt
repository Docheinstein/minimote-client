package org.docheinstein.minimotek.ui.server

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.docheinstein.minimotek.database.server.Server
import org.docheinstein.minimotek.database.server.ServerRepository
import org.docheinstein.minimotek.di.IOGlobalScope
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

    // server for EDIT mode
    private val serverId: Long = savedStateHandle[SERVER_ID_STATE_KEY]!!
    val server = serverRepository.load(serverId).asLiveData()
    var mode: Mode

    init {
        debug("AddEditServerViewModel.init() for serverId = $serverId")
        mode = if (serverId != SERVER_ID_NONE) Mode.EDIT else Mode.ADD
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
            serverRepository.delete(server.value!!)
        }
    }
}