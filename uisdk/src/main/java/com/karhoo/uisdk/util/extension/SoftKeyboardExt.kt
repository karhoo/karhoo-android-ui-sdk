package com.karhoo.uisdk.util.extension

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Hides the soft keyboard
 */
fun View?.hideSoftKeyboard() {
    this?.let {
        val inputMethodManager = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager.isAcceptingText) {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}

/**
 * Shows the soft keyboard
 */
fun View?.showSoftKeyboard() {
    this?.let {
        val inputMethodManager = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(it, 0)
    }
}

