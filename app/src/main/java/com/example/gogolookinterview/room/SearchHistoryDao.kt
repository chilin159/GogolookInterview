package com.example.gogolookinterview.room

import androidx.room.*

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM searchhistory")
    fun getAll(): List<SearchHistory>

    @Insert
    fun insertAll(vararg searchHistories: SearchHistory)
}