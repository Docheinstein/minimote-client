package org.docheinstein.minimotek.ui.servers

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.docheinstein.minimotek.data.server.Server
import org.docheinstein.minimotek.data.server.ServerRepository
import org.docheinstein.minimotek.util.debug
import javax.inject.Inject

@HiltViewModel
class ServersViewModel @Inject constructor(
    private val serverRepository: ServerRepository
) : ViewModel() {

    val servers = serverRepository.servers.asLiveData()

    init {
        debug("ServersViewModel.init(), current servers size is ${servers.value?.size}")
    }

   fun delete(server: Server) {
        viewModelScope.launch {
            serverRepository.delete(server)
        }
    }
}