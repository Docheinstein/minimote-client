package org.docheinstein.minimotek.ui.servers

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.docheinstein.minimotek.database.server.Server
import org.docheinstein.minimotek.database.server.ServerRepository
import org.docheinstein.minimotek.di.IOGlobalScope
import org.docheinstein.minimotek.util.debug
import javax.inject.Inject

@HiltViewModel
class ServersViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    private val serverRepository: ServerRepository
) : ViewModel() {

    val servers = serverRepository.servers.asLiveData()

    init {
        debug("ServersViewModel.init(), current servers size is ${servers.value?.size}")
    }

    fun delete(server: Server) {
        ioScope.launch {
            serverRepository.delete(server)
        }
    }
}