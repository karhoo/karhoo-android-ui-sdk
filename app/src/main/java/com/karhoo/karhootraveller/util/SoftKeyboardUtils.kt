package com.karhoo.karhootraveller.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

object SoftKeyboardUtils {

    fun hideSoftKeyboard(currentFocus: View?) {
        currentFocus?.let {
            val context = it.context
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethodManager.isAcceptingText) {
                inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
            }
        }
    }

}
