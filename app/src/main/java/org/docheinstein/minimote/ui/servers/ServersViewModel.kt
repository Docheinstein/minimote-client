package org.docheinstein.minimote.ui.servers

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.docheinstein.minimote.database.server.ServerRepository
import org.docheinstein.minimote.di.IOGlobalScope
import org.docheinstein.minimote.util.verbose
import javax.inject.Inject

@HiltViewModel
class ServersViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    private val serverRepository: ServerRepository
) : ViewModel() {

    val servers = serverRepository.observeAll().asLiveData()

    init {
        verbose("ServersViewModel.init()")
    }

    override fun onCleared() {
        verbose("ServersViewModel.onCleared()")
    }

    fun delete(id: Long) {
        ioScope.launch {
            serverRepository.delete(id)
        }
    }
}