package com.example.part3_chapter4.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.part3_chapter4.model.History

@Dao
interface HistoryDao {
    @Query("Select * from History")
    fun getAll():List<History>

    @Insert
    fun insertHistory(history: History)

    @Query("Delete from history where keyword == :keyword")
    fun delete(keyword: String)
}