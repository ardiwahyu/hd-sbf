package com.bm.hdsbf.data.local.sp

import android.content.Context
import android.content.SharedPreferences
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
}