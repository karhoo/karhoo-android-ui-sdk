package com.karhoo.uisdk.util.extension

import com.google.android.material.snackbar.Snackbar
import com.karhoo.uisdk.base.SnackbarState

fun Snackbar.showWithCheck(currentState: SnackbarState): Snackbar {
    when (currentState) {
        SnackbarState.NETWORK,
        SnackbarState.INVITE,
        SnackbarState.GENERIC -> show()

        else -> {}
    }
    return this
}
