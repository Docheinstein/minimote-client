package org.docheinstein.minimotek.ui.servers

import android.net.Uri
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.docheinstein.minimotek.AUTO_ID
import org.docheinstein.minimotek.database.server.Server
import org.docheinstein.minimotek.database.server.ServerRepository
import org.docheinstein.minimotek.di.IOGlobalScope
import org.docheinstein.minimotek.ui.hwhotkeys.AddEditHwHotkeyViewModel
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
        const val SERVER_ID_NONE = Long.MIN_VALUE
    }

    enum class Mode {
        ADD,
        EDIT
    }

    private val serverId: Long = savedStateHandle[SERVER_ID_STATE_KEY] ?: SERVER_ID_NONE
    val mode = if (serverId == SERVER_ID_NONE) Mode.ADD else Mode.EDIT
    val server = if (mode == Mode.EDIT) serverRepository.load(serverId).asLiveData() else null

    private val _icon = MutableLiveData<Uri>()
    val icon: LiveData<Uri>
        get() = _icon

    init {
        debug("AddEditServerViewModel.init() for serverId = $serverId")
    }

    fun save(address: String, port: Int, name: String?): Server {
        val s = Server(if (mode == Mode.ADD) AUTO_ID else server?.value!!.id, address, port, name, _icon.value)
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

    fun setIcon(uri: Uri) {
        _icon.postValue(uri)
    }
}