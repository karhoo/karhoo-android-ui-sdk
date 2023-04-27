package com.karhoo.uisdk.base.listener

import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError
import com.karhoo.uisdk.base.snackbar.SnackbarConfig

interface ErrorView {

    fun showSnackbar(snackbarConfig: SnackbarConfig)

    fun showTopBarNotification(@StringRes stringId: Int)

    fun showTopBarNotification(value: String)

    fun showErrorDialog(@StringRes stringId: Int, karhooError: KarhooError? = null)

    fun showBlockingErrorDialog(@StringRes stringId: Int, karhooError: KarhooError? = null)

    fun dismissSnackbar()

}
