package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DailyTimerRecordEntity::class], version = 1, exportSchema = false)
abstract class FocuzDatabase : RoomDatabase() {
  abstract fun dailyTimerRecordDao(): DailyTimerRecordDao

  companion object {
    @Volatile
    private var INSTANCE: FocuzDatabase? = null

    fun getDatabase(context: Context): FocuzDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          FocuzDatabase::class.java,
          "focuz_local_db"
        ).fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
