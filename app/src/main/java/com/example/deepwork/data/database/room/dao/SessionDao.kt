package com.example.deepwork.data.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.deepwork.data.database.room.model.scheduled_session.ScheduledSessionEntity
import java.util.UUID

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ScheduledSessionEntity)

    @Query("SELECT * FROM scheduled_session WHERE id = :id")
    suspend fun getSessionById(id: UUID): ScheduledSessionEntity?
}
