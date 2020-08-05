package com.karhoo.karhootraveller.presentation.base

import androidx.annotation.StringRes

interface ErrorMessageView {

    fun showError(@StringRes errorMessage: Int)

}
