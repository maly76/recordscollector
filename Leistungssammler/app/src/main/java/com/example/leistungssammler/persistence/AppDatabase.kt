package com.example.leistungssammler.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.leistungssammler.model.Record

@Database(entities = [  Record::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDAO
}