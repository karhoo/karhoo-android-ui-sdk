package com.karhoo.uisdk.util.extension

import java.util.Locale

fun Locale?.toNormalizedLocale() : String {
    this?.let { locale ->
        return locale.toString().replace('_', '-')
    } ?: run {
        return ""
    }

}
