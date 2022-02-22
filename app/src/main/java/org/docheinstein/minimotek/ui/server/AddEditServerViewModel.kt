package org.docheinstein.minimotek.ui.server

import androidx.lifecycle.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.util.date.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.docheinstein.minimotek.AUTO_ID
import org.docheinstein.minimotek.R
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
    val mode = if (serverId == SERVER_ID_NONE) Mode.ADD else AddEditHwHotkeyViewModel.Mode.EDIT
    val server = if (mode == Mode.EDIT) serverRepository.load(serverId).asLiveData() else null

    init {
        debug("AddEditServerViewModel.init() for serverId = $serverId")
    }

    fun save(address: String, port: Int, name: String?): Server {
        val s = Server(if (mode == Mode.ADD) AUTO_ID else server?.value!!.id, address, port, name)
        ioScope.launch {
            serverRepository.save(s)
        }
        return s
    }

    fun delete() {
        ioScope.launch {
            serverRepository.delete(server?.value!!)
        }
    }
}