package org.docheinstein.minimote.database.server

import kotlinx.coroutines.flow.Flow
import org.docheinstein.minimote.util.debug
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerRepository @Inject constructor(private val serverDao: ServerDao) {
    fun observe(id: Long): Flow<Server> {
        debug("ServerRepository.observe($id)")
        return serverDao.observe(id)
    }

    fun observeAll(): Flow<List<Server>> {
        debug("ServerRepository.observeAll()")
        return serverDao.observeAll()
    }

    suspend fun get(id: Long): Server? {
        debug("ServerRepository.get($id)")
        return serverDao.get(id)
    }

    suspend fun save(server: Server) {
        debug("ServerRepository.save($server)")
        serverDao.save(server)
    }

    suspend fun delete(id: Long) {
        debug("ServerRepository.delete($id)")
        serverDao.delete(id)
    }
}