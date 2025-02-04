package com.bm.hdsbf.data.local.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "schedule")
class ScheduleVo (
    @PrimaryKey(autoGenerate = false) val id: Int,
    val month: String,
    val date: Int,
    val name: String,
    val type: String
): Parcelable