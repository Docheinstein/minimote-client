package org.docheinstein.minimotek.ui.discover

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.docheinstein.minimotek.AUTO_ID
import org.docheinstein.minimotek.ESTIMATED_DISCOVER_TIME
import org.docheinstein.minimotek.discover.DiscoveredServer
import org.docheinstein.minimotek.discover.Discoverer
import org.docheinstein.minimotek.database.server.Server
import org.docheinstein.minimotek.database.server.ServerRepository
import org.docheinstein.minimotek.di.IODispatcher
import org.docheinstein.minimotek.di.IOGlobalScope
import org.docheinstein.minimotek.util.asMessage
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.error
import org.docheinstein.minimotek.util.verbose
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class DiscoverDialogViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val serverDiscoverer: Discoverer,
    private val serverRepository: ServerRepository,
) : ViewModel() {

    // Discover state
    private val _isDiscovering = MutableLiveData(true)
    val isDiscovering: LiveData<Boolean> = _isDiscovering

    private val _discoverError = MutableLiveData<String?>()
    val discoverError: LiveData<String?> = _discoverError

    // Discovered servers
    private val __discoveredServers = mutableListOf<DiscoveredServer>()
    private val _discoveredServers = MutableLiveData<List<DiscoveredServer>>()
    val discoveredServers: LiveData<List<DiscoveredServer>>
        get() = _discoveredServers

    init {
        verbose("DiscoveryDialogViewModel.init()")
        // Launch on viewModelScope since the discover procedure makes sense only until this view is
        // valid, but perform the job on IO dispatcher since it is a network task that cannot be
        // performed on main thread
        viewModelScope.launch(ioDispatcher) {
            debug("Discovery coroutine launched")
            try {
                // Start the discover procedure and push the discovered servers
                // to the retrieved server list
                serverDiscoverer.discoverServers().collect { discoveredServer ->
                    debug("DiscoverDialogViewModel received discoveredServer: $discoveredServer")
                    __discoveredServers.add(discoveredServer)
                    _discoveredServers.postValue(__discoveredServers) // trigger update
                }
            } catch (e: IOException) {
                error("Error occurred while discovering", e)
                _discoverError.postValue(e.asMessage())
            }
        }

        // Declare the discover procedure as finished after a certain time (actually
        // it's never finished, since we could receive a response from a slow server
        // at any time, but it's unlikely that we'll receive a response after ~10 seconds)
        viewModelScope.launch {
            delay(ESTIMATED_DISCOVER_TIME.toLong())
            debug("$ESTIMATED_DISCOVER_TIME ms elapsed: discovery is probably over")
            _isDiscovering.value = false
        }
    }

    override fun onCleared() {
        verbose("DiscoverDialogViewModel.onCleared()")
    }

    fun insert(server: DiscoveredServer) {
        ioScope.launch {
            val s = Server(AUTO_ID, server.address, server.port, server.hostname, null)
            serverRepository.save(s)
        }
    }
}