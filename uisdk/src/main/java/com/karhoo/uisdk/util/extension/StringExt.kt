package com.karhoo.uisdk.util.extension

fun String?.removeLastOccurrenceOf(substring: String?, caseInsensitive: Boolean = true, prettify: Boolean = true): String {
    this.let { string ->
        if (string == null) {
            return ""
        }

        if (substring == null || !string.lowercase().contains(substring.lowercase())) {
            return string
        }

        try {
            // Replace the last occurrence of the substring
            val pattern = "(\\b".plus(substring).plus("\\b)(?!.*\\1)")
            // Take into account the case sensitivity preference
            val regex = if (caseInsensitive) Regex(pattern, RegexOption.IGNORE_CASE) else Regex(pattern)
            var result = string.replace(regex, "")

            if (prettify) {
                result = result.removeSubstringWithRegexUsing(" +,{1} +", " ")
            }
            return result
        }
        catch (e: java.lang.Exception) {
            println(e)
            return string
        }
    }
}

fun String?.removeSubstringWithRegexUsing(pattern: String, replacement: String = ""): String {
    this.let { string ->
        try {
            val regex = Regex(pattern)
            return string?.replace(regex, replacement) ?: ""
        }
        catch(e: java.lang.Exception) {
            println(e)
            return string ?: ""
        }
    }
}
