package edu.vt.cs.cs5254.dreamcatcher

import androidx.room.*

@Database(entities = [Dream::class, DreamEntry::class], version = 1)
@TypeConverters(DreamTypeConverters::class)
abstract class DreamDatabase : RoomDatabase(){
    abstract fun dreamDao(): DreamDao
}