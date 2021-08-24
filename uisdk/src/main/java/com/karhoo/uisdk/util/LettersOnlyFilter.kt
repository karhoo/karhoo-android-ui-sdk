package com.karhoo.uisdk.util

import android.text.InputFilter
import android.text.Spanned

class LettersOnlyFilter : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
                       ): CharSequence {
        if (source == null) return ""
        if (source == "") { // for backspace
            return source
        }
        return Regex("[^A-Za-z ]").replace(source, "")
    }
}
