package org.docheinstein.minimotek.ui.discover

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.docheinstein.minimotek.ESTIMATED_DISCOVER_TIME
import org.docheinstein.minimotek.data.discover.DiscoveredServer
import org.docheinstein.minimotek.data.discover.DiscoveredServerRepository
import org.docheinstein.minimotek.data.server.Server
import org.docheinstein.minimotek.data.server.ServerRepository
import org.docheinstein.minimotek.di.IODispatcher
import org.docheinstein.minimotek.di.IOGlobalScope
import org.docheinstein.minimotek.util.asMessage
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.error
import javax.inject.Inject

@HiltViewModel
class DiscoverDialogViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val discoveredServerRepository: DiscoveredServerRepository,
    private val serverRepository: ServerRepository,
) : ViewModel() {


    private val _isDiscovering = MutableLiveData(true)
    val isDiscovering: LiveData<Boolean> = _isDiscovering

    private val _discoverError = MutableLiveData<String?>()
    val discoverError: LiveData<String?> = _discoverError

    private val __discoveredServers = mutableListOf<DiscoveredServer>()
    private val _discoveredServers = MutableLiveData<List<DiscoveredServer>>()
    val discoveredServers: LiveData<List<DiscoveredServer>>
        get() = _discoveredServers

    init {
        debug("DiscoveryDialogViewModel.init()")
        // Launch on view model scope since the discover job makes sense only until this view is
        // valid, but perform the job on IO dispatcher since it is a network task that cannot be
        // performed on main thread
        viewModelScope.launch(ioDispatcher) {
            debug("Discovery coroutine launched")
            try {
                discoveredServerRepository.discoverServers().collect { discoveredServer ->
                    debug("DiscoverDialogViewModel received discoveredServer: $discoveredServer")
                    __discoveredServers.add(discoveredServer)
                    _discoveredServers.postValue(__discoveredServers) // trigger update
                }
            } catch (e: Exception) {
                error("Error occurred while discovering: ${e.asMessage()}")
                _discoverError.postValue(e.asMessage())
            }
        }

        viewModelScope.launch {
            delay(ESTIMATED_DISCOVER_TIME.toLong())
            debug("$ESTIMATED_DISCOVER_TIME ms: discovery is probably over")
            _isDiscovering.value = false
        }
    }

    fun insert(discoveredServer: DiscoveredServer) {
        ioScope.launch {
            val s = Server(discoveredServer.address, discoveredServer.port, discoveredServer.hostname)
            serverRepository.add(s)
        }
    }
}