package com.karhoo.uisdk.base

import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError

interface SimpleErrorMessageView {

    fun showError(@StringRes errorMessage: Int, karhooError: KarhooError?)

}
