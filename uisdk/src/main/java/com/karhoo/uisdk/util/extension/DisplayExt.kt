package com.karhoo.uisdk.util.extension

import android.content.res.Resources

fun Float.convertDpToPixels() = (this * (Resources.getSystem().displayMetrics.densityDpi / 160f)).toInt()