package com.bm.hdsbf.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bm.hdsbf.data.local.db.entities.ScheduleVo

@Dao
interface ScheduleDao {
    @Query("SELECT DISTINCT month FROM schedule")
    suspend fun getAllMonth(): List<String>

    @Query("DELETE FROM schedule WHERE month in (:months)")
    suspend fun deleteMonths(months: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(listScheduleVo: List<ScheduleVo>)

    @Query("SELECT * FROM schedule")
    suspend fun getAllSchedule(): List<ScheduleVo>

    @Query("SELECT * FROM schedule WHERE name = :name OR type LIKE 'OFF%'")
    suspend fun getAllScheduleByName(name: String): List<ScheduleVo>

    @Query("SELECT * FROM schedule WHERE month = :month AND date = :date ORDER BY type ASC")
    suspend fun getScheduleByDate(month: String, date: Int): List<ScheduleVo>

    @Query("SELECT DISTINCT name FROM schedule WHERE month = :month ORDER BY name ASC")
    suspend fun getName(month: String): List<String>

    @Query("SELECT * FROM schedule WHERE month = :month AND date = :date AND name = :name AND type LIKE 'HD%' ORDER BY type ASC")
    suspend fun getScheduleByDateAndName(month: String, date: Int, name: String): List<ScheduleVo>
}