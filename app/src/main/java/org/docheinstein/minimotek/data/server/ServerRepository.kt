package org.docheinstein.minimotek.data.server

import androidx.lifecycle.LiveData

class ServerRepository(private val serverDao: ServerDao) {

    fun getAll(): LiveData<List<Server>> {
        return serverDao.getAll()
    }
}