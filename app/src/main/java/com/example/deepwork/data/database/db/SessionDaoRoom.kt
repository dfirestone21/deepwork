package com.example.deepwork.data.database.db

import com.example.deepwork.data.database.room.dao.SessionDao as RoomSessionDao
import com.example.deepwork.data.database.room.model.scheduled_session.ScheduledSessionEntity
import javax.inject.Inject
// TODO remove class and just use Dao interface
class SessionDaoRoom @Inject constructor(
    private val roomSessionDao: RoomSessionDao
) : SessionDao {

    override suspend fun upsert(session: ScheduledSessionEntity): ScheduledSessionEntity {
        roomSessionDao.insertSession(session)
        return session
    }
}
