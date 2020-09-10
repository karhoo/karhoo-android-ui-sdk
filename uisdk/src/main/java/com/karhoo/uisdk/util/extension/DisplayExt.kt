package com.karhoo.uisdk.util.extension

import android.content.res.Resources

const val DENSITY_DPI_FACTOR = 160f
fun Float.convertDpToPixels() = (this * (Resources.getSystem().displayMetrics.densityDpi / DENSITY_DPI_FACTOR)).toInt()
