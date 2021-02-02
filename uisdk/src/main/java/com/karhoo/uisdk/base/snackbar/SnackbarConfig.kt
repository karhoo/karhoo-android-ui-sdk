package com.karhoo.uisdk.base.snackbar

import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError

data class SnackbarConfig(val type: SnackbarType = SnackbarType.TEMPORARY,
                          val priority: SnackbarPriority = SnackbarPriority.NORMAL,
                          val action: SnackbarAction? = null,
                          val text: String?,
                          @StringRes val messageResId: Int = -1,
                          val karhooError: KarhooError? = null)

enum class SnackbarType {
    BLOCKING,
    BLOCKING_DISMISSIBLE,
    TEMPORARY
}

enum class SnackbarPriority {
    HIGHEST,
    HIGH,
    NORMAL
}

class SnackbarAction(val text: String,
                     val action: () -> Unit)
