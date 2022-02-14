package org.docheinstein.minimotek.data.server

import kotlinx.coroutines.flow.Flow
import org.docheinstein.minimotek.util.info
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerRepository @Inject constructor(private val serverDao: ServerDao) {
    val servers: Flow<List<Server>> = serverDao.getAll()

    fun get(id: Long): Flow<Server> {
        return serverDao.get(id)
    }

    suspend fun add(server: Server) {
        info("Adding server $server")
        serverDao.add(server)
    }

    suspend fun update(server: Server) {
        info("Updating server $server")
        serverDao.update(server)
    }

    suspend fun delete(server: Server) {
        info("Deleting server $server")
        serverDao.delete(server)
    }
}