package com.karhoo.uisdk.util.extension

import java.util.Locale

fun Locale?.toNormalizedLocale() : String {
    this?.let { locale ->
        if(locale.toString() in arrayOf("en_US", "en_GB", "fr_FR", "es_ES", "de_DE", "it_IT",
                                        "nl_NL"))
            return locale.toString().replace('_', '-')
        else
            return "en-GB"
    } ?: run {
        return ""
    }

}
