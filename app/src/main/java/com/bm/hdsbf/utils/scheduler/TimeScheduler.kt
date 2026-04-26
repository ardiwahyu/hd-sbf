package com.bm.hdsbf.utils.scheduler

enum class TimeScheduler(val hour: Int, val minute: Int) {
    NOW(3, 0),
    TOMORROW(16, 30)
}