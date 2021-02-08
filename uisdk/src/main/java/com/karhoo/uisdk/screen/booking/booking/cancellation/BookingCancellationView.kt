package com.karhoo.uisdk.screen.booking.booking.cancellation

import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogAction
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogConfig
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogHelper

class BookingCancellationView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr), BookingCancellationMVP.View {

    private val presenter: BookingCancellationMVP.Presenter = BookingCancellationPresenter(this)

    override fun cancelTrip(tripId: String) {
        presenter.handleCancellationRequest(tripId)
    }

    override fun showCancellationFee(formattedPrice: String, tripId: String) {
        val messageResId = if (formattedPrice.isNotEmpty()) R.string.you_may_be_charged else R
                .string.would_you_like_to_proceed
        val config = KarhooAlertDialogConfig(
                titleResId = R.string.cancel_your_ride,
                messageResId = messageResId,
                cancellable = false,
                positiveButton = KarhooAlertDialogAction(R.string.dismiss,
                                                         DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() }),
                negativeButton = KarhooAlertDialogAction(R.string.ok,
                                                         DialogInterface.OnClickListener { _, _
                                                             ->
                                                             presenter
                                                                     .handleCancellationRequest(tripId)
                                                         }))
        KarhooAlertDialogHelper(context).showAlertDialog(config)
    }

    override fun showCancellationFeeError() {
        val config = KarhooAlertDialogConfig(
                titleResId = R.string.difficulties_cancelling_title,
                messageResId = R.string.difficulties_cancelling_message,
                cancellable = false,
                positiveButton = KarhooAlertDialogAction(R.string.dismiss,
                                                         DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() }),
                negativeButton = KarhooAlertDialogAction(R.string.cancel,
                                                         DialogInterface.OnClickListener { _, _
                                                             ->
                                                             //TODO Add Call Fleet functionality
                                                         }))
        KarhooAlertDialogHelper(context).showAlertDialog(config)
    }

    override fun showCancellationError() {
        //TODO What do we do here?
        val config = KarhooAlertDialogConfig(
                titleResId = R.string.cancel_your_ride,
                messageResId = R.string.difficulties_cancelling_message,
                cancellable = false,
                positiveButton = KarhooAlertDialogAction(R.string.dismiss,
                                                         DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() }),
                negativeButton = KarhooAlertDialogAction(R.string.cancel,
                                                         DialogInterface.OnClickListener { _, _
                                                             ->
                                                             //TODO Add Call Fleet functionality?
                                                         }))
        KarhooAlertDialogHelper(context).showAlertDialog(config)
    }

    override fun showCancellationSuccess() {
        val config = KarhooAlertDialogConfig(
                titleResId = R.string.cancel_ride_successful,
                messageResId = R.string.cancel_ride_successful_message,
                positiveButton = KarhooAlertDialogAction(R.string.dismiss,
                                                         DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() }))
        KarhooAlertDialogHelper(context).showAlertDialog(config)
    }

}
