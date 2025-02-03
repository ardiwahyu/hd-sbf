package com.bm.hdsbf.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bm.hdsbf.data.local.db.dao.ScheduleDao
import com.bm.hdsbf.data.local.db.entities.ScheduleVo

@Database(entities = [ScheduleVo::class], version = 2, exportSchema = false)
abstract class HdSbfDB: RoomDatabase() {
    companion object {
        const val NAME = "HdSbf.db"
    }
    abstract fun scheduleDao(): ScheduleDao
}