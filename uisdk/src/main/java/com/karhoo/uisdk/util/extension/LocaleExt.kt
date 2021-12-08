package com.karhoo.uisdk.util.extension

import java.util.Locale

fun Locale?.toNormalizedLocale() : String {
    this?.let { locale ->
        if(locale.toString() in arrayOf("en_US", "en_GB", "fr_FR"))
            return locale.toString().replace('_', '-')
        else
            return "en-GB"
    } ?: run {
        return ""
    }

}
