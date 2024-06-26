package com.bm.hdsbf.data.local.sp

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferenceClass @Inject constructor(
    @ApplicationContext context: Context
) {
    companion object {
        private const val PREF_NAME = "SBF_HD_SP"
        private const val KEY_NAME = "key_name"
        private const val KEY_SHOW = "key_show"
        private const val KEY_REMINDER = "key_reminder"
        private const val KEY_LAST_MODIFIED = "key_last_modified"
        private const val KEY_TIME_SCHEDULE = "key_time_schedule"
        private const val KEY_IS_FIRST = "key_is_first"
    }
    private var pref: SharedPreferences
    private var editor: SharedPreferences.Editor
    private var privateMode = 0

    init {
        pref = context.getSharedPreferences(PREF_NAME, privateMode)
        editor = pref.edit()
        editor.apply()
    }

    fun setName(name: String) {
        editor.putString(KEY_NAME, name).commit()
    }

    fun getName(): String? {
        return pref.getString(KEY_NAME, null)
    }

    fun setShow(showSetting: String) {
        editor.putString(KEY_SHOW, showSetting).commit()
    }

    fun getShow(): String? {
        return pref.getString(KEY_SHOW, null)
    }

    fun setReminder(isActive: Boolean) {
        editor.putBoolean(KEY_REMINDER, isActive).commit()
    }

    fun getReminder(): Boolean {
        return pref.getBoolean(KEY_REMINDER, false)
    }

    fun setLastModified(time: Long) {
        editor.putLong(KEY_LAST_MODIFIED, time).commit()
    }

    fun getLastModified(): Long {
        return pref.getLong(KEY_LAST_MODIFIED, 0)
    }

    fun setTimeSchedule(time: HashMap<String, String>) {
        editor.putString(KEY_TIME_SCHEDULE, Gson().toJson(time)).commit()
    }

    fun getTimeSchedule(): HashMap<String, String>? {
        val type = object : TypeToken<HashMap<String, String>?>(){}.type
        return Gson().fromJson(pref.getString(KEY_TIME_SCHEDULE, ""), type)
    }

    fun setIsFirst(isFirst: Boolean) {
        editor.putBoolean(KEY_IS_FIRST, isFirst).commit()
    }

    fun getIsFirst(): Boolean {
        return pref.getBoolean(KEY_IS_FIRST, true)
    }
}