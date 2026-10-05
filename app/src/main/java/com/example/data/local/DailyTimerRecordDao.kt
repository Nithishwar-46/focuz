package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the `daily_timer_records` Room table.
 *
 * Provides reactive SQLite queries and asynchronous transactions for archiving,
 * retrieving, and querying daily focus timer and social scroll records.
 */
@Dao
interface DailyTimerRecordDao {

  /**
   * Observes all archived daily timer records in descending chronological order.
   * Emits a new list whenever any record is inserted, updated, or removed.
   */
  @Query("SELECT * FROM daily_timer_records ORDER BY date DESC")
  fun getAllRecords(): Flow<List<DailyTimerRecordEntity>>

  /**
   * Retrieves a single day's record by its canonical date key (format: "YYYY-MM-DD").
   * Returns null if no record exists for that date.
   */
  @Query("SELECT * FROM daily_timer_records WHERE date = :date LIMIT 1")
  suspend fun getRecordForDate(date: String): DailyTimerRecordEntity?

  /**
   * Inserts or updates a daily timer record.
   * Utilizes [OnConflictStrategy.REPLACE] to ensure idempotent writes when 12:00 AM
   * midnight rollover triggers or when daily stats are updated.
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(record: DailyTimerRecordEntity)

  /**
   * Deletes a specific day's record by its date key.
   */
  @Query("DELETE FROM daily_timer_records WHERE date = :date")
  suspend fun deleteRecord(date: String)

  /**
   * Computes the total number of historical days where the daily focus goal was achieved.
   */
  @Query("SELECT COUNT(*) FROM daily_timer_records WHERE goalAchieved = 1")
  suspend fun getDaysGoalAchievedCount(): Int
}
