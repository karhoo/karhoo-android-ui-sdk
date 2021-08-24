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
        if (source?.equals("") == true) { // for backspace
            return source
        }
        return if (source?.toString()?.matches(Regex("[a-zA-Z ]+")) == true) {
            source
        } else {
            source?.dropLast(1).toString()
        }
    }
}
