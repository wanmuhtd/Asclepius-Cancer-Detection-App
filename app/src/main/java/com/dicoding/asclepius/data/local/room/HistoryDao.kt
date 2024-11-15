package com.dicoding.asclepius.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dicoding.asclepius.data.local.entity.HistoryEntity

@Dao
interface HistoryDao {
    @Insert
    suspend fun insertHistory(historyEntity: HistoryEntity)

    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    suspend fun getAllHistory(): List<HistoryEntity>

    @Query("DELETE FROM history")
    suspend fun deleteAllHistory()
}