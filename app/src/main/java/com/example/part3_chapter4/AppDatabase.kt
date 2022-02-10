package com.example.part3_chapter4

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.part3_chapter4.dao.HistoryDao
import com.example.part3_chapter4.dao.ReviewDao
import com.example.part3_chapter4.model.History
import com.example.part3_chapter4.model.Review

@Database(entities = [History::class,Review::class], version = 2)
abstract class AppDatabase: RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun reviewDao(): ReviewDao
}

fun getAppdataBase(context: Context):AppDatabase{
    val migration_2_3 = object : Migration(1,2){
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("Create Table 'Review'('id' Integer,'review' TEXT,"+"Primary Key('id'))")
        }

    }


    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "BooksearchDB"
    )
        .addMigrations(migration_2_3)
        .build()
}