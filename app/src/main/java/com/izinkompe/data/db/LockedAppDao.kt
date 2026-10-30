package com.izinkompe.data.db

import androidx.room.*
import com.izinkompe.data.model.LockedApp
import kotlinx.coroutines.flow.Flow

@Dao
interface LockedAppDao {

    @Query("SELECT * FROM locked_apps WHERE isLocked = 1")
    fun getLockedApps(): Flow<List<LockedApp>>

    @Query("SELECT * FROM locked_apps WHERE isLocked = 1")
    suspend fun getLockedAppsList(): List<LockedApp>

    @Query("SELECT packageName FROM locked_apps WHERE isLocked = 1")
    suspend fun getLockedPackageNames(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM locked_apps WHERE packageName = :packageName AND isLocked = 1)")
    suspend fun isAppLocked(packageName: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<LockedApp>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: LockedApp)

    @Delete
    suspend fun delete(app: LockedApp)

    @Query("DELETE FROM locked_apps")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM locked_apps WHERE isLocked = 1")
    fun getLockedCount(): Flow<Int>
}
