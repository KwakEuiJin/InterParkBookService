package com.example.part3_chapter4.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.part3_chapter4.model.Review

@Dao
interface ReviewDao {
    @Query("Select * From Review where id == :id")
    fun getOneReview(id: Int) : Review?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveReview(review: Review)

}