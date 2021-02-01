package com.karhoo.karhootraveller.presentation.base

import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError

interface ErrorMessageView {

    fun showError(@StringRes errorMessage: Int, karhooError: KarhooError?)

}
