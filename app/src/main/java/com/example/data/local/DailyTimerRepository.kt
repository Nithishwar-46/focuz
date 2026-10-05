package com.example.data.local

import kotlinx.coroutines.flow.Flow

/**
 * Repository mediating access to persisted daily timer records in Room SQLite.
 *
 * Exposes reactive Kotlin [Flow] streams and coroutine-based write operations
 * to decouple the ViewModel and UI layers from Room implementation specifics.
 */
class DailyTimerRepository(private val dao: DailyTimerRecordDao) {

  /**
   * Cold Flow of archived daily records ordered by date descending.
   */
  val allRecords: Flow<List<DailyTimerRecordEntity>> = dao.getAllRecords()

  /**
   * Fetches an archived day record by its "YYYY-MM-DD" date key.
   */
  suspend fun getRecordForDate(date: String): DailyTimerRecordEntity? {
    return dao.getRecordForDate(date)
  }

  /**
   * Persists or replaces a daily timer summary record.
   */
  suspend fun saveRecord(record: DailyTimerRecordEntity) {
    dao.insertOrUpdate(record)
  }

  /**
   * Removes a record for a specific date.
   */
  suspend fun deleteRecord(date: String) {
    dao.deleteRecord(date)
  }

  /**
   * Returns the count of days where the daily focus target was achieved.
   */
  suspend fun getGoalAchievedDaysCount(): Int {
    return dao.getDaysGoalAchievedCount()
  }
}
