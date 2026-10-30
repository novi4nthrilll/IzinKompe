package com.izinkompe.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.izinkompe.data.model.LockedApp
import com.izinkompe.data.model.Schedule

@Database(
    entities = [LockedApp::class, Schedule::class],
    version = 1,
    exportSchema = false
)
abstract class IzinKompeDatabase : RoomDatabase() {

    abstract fun lockedAppDao(): LockedAppDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: IzinKompeDatabase? = null

        fun getDatabase(context: Context): IzinKompeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IzinKompeDatabase::class.java,
                    "izinkompe_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
