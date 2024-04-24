package com.bm.hdsbf.ui.setting

enum class ShowSetting {
    ALL {
        override fun text(): String = "Semua Jadwal"
       },
    NAME {
        override fun text(): String = "Hanya Jadwal Saya"
    };

    abstract fun text(): String
}