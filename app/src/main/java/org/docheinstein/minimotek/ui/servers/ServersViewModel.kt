package org.docheinstein.minimotek.ui.servers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.docheinstein.minimotek.data.DB
import org.docheinstein.minimotek.data.server.Server
import org.docheinstein.minimotek.data.server.ServerRepository
import org.docheinstein.minimotek.util.debug

class ServersViewModel(app: Application) : AndroidViewModel(app) {
    private val serverRepository: ServerRepository
    val servers: LiveData<List<Server>>

    init {
        debug("ServersViewModel.init()")
        serverRepository = ServerRepository(DB.getInstance(app).serverDao())
        servers = serverRepository.getAll()
    }
}