package com.example.data.local

import kotlinx.coroutines.flow.Flow

class DailyTimerRepository(private val dao: DailyTimerRecordDao) {
  val allRecords: Flow<List<DailyTimerRecordEntity>> = dao.getAllRecords()

  suspend fun getRecordForDate(date: String): DailyTimerRecordEntity? {
    return dao.getRecordForDate(date)
  }

  suspend fun saveRecord(record: DailyTimerRecordEntity) {
    dao.insertOrUpdate(record)
  }

  suspend fun deleteRecord(date: String) {
    dao.deleteRecord(date)
  }

  suspend fun getGoalAchievedDaysCount(): Int {
    return dao.getDaysGoalAchievedCount()
  }
}
