package com.karhoo.uisdk.util

import android.util.Log

internal object Logger {
    fun warn(msg: String, tag: String? = null) {
        if (tag != null) Log.w(tag, msg) else Log.w(KARHOO_UISDK_TAG, msg)
    }

    fun debug(msg: String, tag: String? = null) {
        if (tag != null) Log.d(tag, msg) else Log.d(KARHOO_UISDK_TAG, msg)
    }

    fun error(msg: String, tag: String? = null) {
        if (tag != null) Log.e(tag, msg) else Log.e(KARHOO_UISDK_TAG, msg)
    }

    const val KARHOO_UISDK_TAG = "KARHOO_UISDK"
}
