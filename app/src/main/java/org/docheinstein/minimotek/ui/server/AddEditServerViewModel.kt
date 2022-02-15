package org.docheinstein.minimotek.ui.server

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.docheinstein.minimotek.data.server.Server
import org.docheinstein.minimotek.data.server.ServerRepository
import org.docheinstein.minimotek.util.error
import org.docheinstein.minimotek.util.debug
import javax.inject.Inject

@HiltViewModel
class AddEditServerViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val savedStateHandle: SavedStateHandle,
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
    val server = serverRepository.get(serverId).asLiveData()
    var mode: Mode

    init {
        debug("AddEditServerViewModel.init() for serverId = $serverId")
        mode = if (serverId != SERVER_ID_NONE) Mode.EDIT else Mode.ADD
    }

    fun insert(s: Server) {
        viewModelScope.launch {
            serverRepository.add(s)
        }
    }

    fun update(s: Server) {
        viewModelScope.launch {
            serverRepository.update(s)
        }
    }

    fun delete() {
        viewModelScope.launch {
            serverRepository.delete(server.value!!)
        }
    }
}