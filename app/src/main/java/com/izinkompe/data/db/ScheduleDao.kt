package com.izinkompe.data.db

import androidx.room.*
import com.izinkompe.data.model.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedules LIMIT 1")
    fun getSchedule(): Flow<Schedule?>

    @Query("SELECT * FROM schedules LIMIT 1")
    suspend fun getScheduleOnce(): Schedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: Schedule)

    @Update
    suspend fun update(schedule: Schedule)

    @Query("DELETE FROM schedules")
    suspend fun deleteAll()
}
