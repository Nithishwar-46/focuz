package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTimerRecordDao {
  @Query("SELECT * FROM daily_timer_records ORDER BY date DESC")
  fun getAllRecords(): Flow<List<DailyTimerRecordEntity>>

  @Query("SELECT * FROM daily_timer_records WHERE date = :date LIMIT 1")
  suspend fun getRecordForDate(date: String): DailyTimerRecordEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(record: DailyTimerRecordEntity)

  @Query("DELETE FROM daily_timer_records WHERE date = :date")
  suspend fun deleteRecord(date: String)

  @Query("SELECT COUNT(*) FROM daily_timer_records WHERE goalAchieved = 1")
  suspend fun getDaysGoalAchievedCount(): Int
}
