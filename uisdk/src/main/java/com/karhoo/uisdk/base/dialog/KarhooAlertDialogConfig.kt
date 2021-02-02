package com.karhoo.uisdk.base.dialog

import android.content.DialogInterface
import android.view.View
import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError

data class KarhooAlertDialogConfig(val title: String? = null,
                                   val message: String? = null,
                                   @StringRes val titleResId: Int = -1,
                                   @StringRes val messageResId: Int = -1,
                                   val cancellable: Boolean = false,
                                   val karhooError: KarhooError? = null,
                                   val positiveButton: KarhooAlertDialogAction? = null,
                                   val negativeButton: KarhooAlertDialogAction? = null,
                                   val view: View? = null)

data class KarhooAlertDialogAction(@StringRes val buttonLabel: Int = -1,
                                   val buttonListener: DialogInterface.OnClickListener)
