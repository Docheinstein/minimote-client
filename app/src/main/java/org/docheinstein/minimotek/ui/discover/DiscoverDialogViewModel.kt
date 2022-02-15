package org.docheinstein.minimotek.ui.discover

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.docheinstein.minimotek.data.discover.DiscoveredServer
import org.docheinstein.minimotek.data.discover.DiscoveredServerRepository
import org.docheinstein.minimotek.data.server.Server
import org.docheinstein.minimotek.data.server.ServerRepository
import org.docheinstein.minimotek.util.debug
import javax.inject.Inject
import kotlin.math.roundToLong
import kotlin.random.Random

@HiltViewModel
class DiscoverDialogViewModel @Inject constructor(
    private val discoveredServerRepository: DiscoveredServerRepository,
    private val serverRepository: ServerRepository,
) : ViewModel() {


//    val _discoveredServers = mutableListOf<DiscoveredServer>()
    val __discoveredServers = mutableListOf<DiscoveredServer>()
    val _discoveredServers = MutableLiveData<List<DiscoveredServer>>()
    val discoveredServers: LiveData<List<DiscoveredServer>>
        get() = _discoveredServers

    init {
        debug("DiscoveryDialogViewModel.init()")
        viewModelScope.launch {
            discoveredServerRepository.discoverServers().collect { discoveredServer ->
                debug("DiscoverDialogViewModel received discoveredServer: $discoveredServer")
                __discoveredServers.add(discoveredServer)
                _discoveredServers.value = __discoveredServers // trigger update
            }
        }
    }

    fun insert(discoveredServer: DiscoveredServer) {
        viewModelScope.launch {
            val s = Server(discoveredServer.address, discoveredServer.port, discoveredServer.name)
            serverRepository.add(s)
        }
    }
}