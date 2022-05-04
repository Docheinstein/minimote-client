package org.docheinstein.minimote.ui.servers

import android.net.Uri
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.docheinstein.minimote.AUTO_ID
import org.docheinstein.minimote.database.server.Server
import org.docheinstein.minimote.database.server.ServerRepository
import org.docheinstein.minimote.di.IOGlobalScope
import org.docheinstein.minimote.util.error
import org.docheinstein.minimote.util.verbose
import javax.inject.Inject


@HiltViewModel
class AddEditServerViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    private val serverRepository: ServerRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val SERVER_ID_STATE_KEY = "serverId"
        const val SERVER_ID_NONE = Long.MIN_VALUE
    }

    enum class Mode {
        ADD,
        EDIT
    }

    private val serverId: Long = savedStateHandle[SERVER_ID_STATE_KEY] ?: SERVER_ID_NONE
    val mode = if (serverId == SERVER_ID_NONE) Mode.ADD else Mode.EDIT

    var fetched = false
    val server = if (mode == Mode.EDIT) serverRepository.observe(serverId).asLiveData() else null

    init {
        verbose("AddEditServerViewModel.init() for serverId = $serverId")
    }

    override fun onCleared() {
        verbose("AddEditServerViewModel.onCleared()")
    }

    fun save(
        address: String,
        port: Int,
        name: String?,
        icon: Uri?
    ): Server {
        val s = Server(
            id = if (mode == Mode.ADD) AUTO_ID else serverId,
            address = address,
            port = port,
            name = name,
            icon = icon
        )

        ioScope.launch {
            serverRepository.save(s)
        }
        return s
    }

    fun delete() {
        if (serverId == SERVER_ID_NONE) {
            error("Cannot delete, invalid serverId")
            return
        }

        ioScope.launch {
            serverRepository.delete(serverId)
        }
    }
}