package com.bm.hdsbf.utils

import android.content.Context
import android.view.View
import android.widget.Toast

object ViewUtil {
    fun View.setVisible() {
        this.visibility = View.VISIBLE
    }

    fun View.setInvisible() {
        this.visibility = View.INVISIBLE
    }

    fun View.setGone() {
        this.visibility = View.GONE
    }

    fun Context.showShortToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun Context.showLongToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}