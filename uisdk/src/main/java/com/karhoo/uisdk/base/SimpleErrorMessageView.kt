package com.karhoo.uisdk.base

import androidx.annotation.StringRes

interface SimpleErrorMessageView {

    fun showError(@StringRes errorMessage: Int)

}
