package com.dicoding.asclepius.data

import com.dicoding.asclepius.data.local.entity.HistoryEntity
import com.dicoding.asclepius.data.local.room.HistoryDao

class HistoryRepository(private val historyDao: HistoryDao) {
    suspend fun insertHistory(historyEntity: HistoryEntity) {
        historyDao.insertHistory(historyEntity)
    }

    suspend fun getAllHistory(): List<HistoryEntity> {
        return historyDao.getAllHistory()
    }

    suspend fun deleteAllHistory() {
        historyDao.deleteAllHistory()
    }
}