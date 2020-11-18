package com.karhoo.uisdk.base.snackbar

import androidx.annotation.StringRes

data class SnackbarConfig(val type: SnackbarType = SnackbarType.TEMPORARY,
                          val priority: SnackbarPriority = SnackbarPriority.NORMAL,
                          val action: SnackbarAction? = null,
                          val text: String?,
                          @StringRes val stringId: Int = -1)

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
