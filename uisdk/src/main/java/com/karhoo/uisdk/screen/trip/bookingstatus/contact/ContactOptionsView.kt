package com.karhoo.uisdk.screen.trip.bookingstatus.contact

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogAction
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogConfig
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogHelper
import com.karhoo.uisdk.screen.rides.detail.RideDetailMVP
import com.karhoo.uisdk.screen.trip.bookingstatus.BookingStatusMVP
import com.karhoo.uisdk.util.IntentUtils
import kotlinx.android.synthetic.main.uisdk_view_contact_options.view.cancelButton
import kotlinx.android.synthetic.main.uisdk_view_contact_options.view.contactDriverButton
import kotlinx.android.synthetic.main.uisdk_view_contact_options.view.contactFleetButton

class ContactOptionsView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), ContactOptionsMVP.View {

    private var presenter = ContactOptionsPresenter(this, KarhooApi.tripService,
                                                    KarhooUISDK.analytics)
    var actions: ContactOptionsActions? = null

    private var progressDialog: ProgressDialog? = null
    private var cancellationDialog: AlertDialog? = null

    init {
        inflate(context, R.layout.uisdk_view_contact_options, this)

        contactDriverButton.setOnClickListener { presenter.contactDriver() }
        contactFleetButton.setOnClickListener {
            presenter.contactFleet() }
    }

    override fun showTripCancelledDialog() {

        val config = KarhooAlertDialogConfig(
                titleResId = R.string.cancel_ride_successful,
                messageResId = R.string.cancel_ride_successful_message,
                positiveButton = KarhooAlertDialogAction(R.string.dismiss,
                                                         DialogInterface.OnClickListener { _, _ -> actions?.goToCleanBooking() }))
        KarhooAlertDialogHelper(context).showAlertDialog(config)
    }

    override fun showCallToCancelDialog(number: String, quote: String, karhooError: KarhooError) {
        val config = KarhooAlertDialogConfig(
                titleResId = R.string.difficulties_cancelling_title,
                messageResId = R.string.difficulties_cancelling_message,
                positiveButton = KarhooAlertDialogAction(R.string.call,
                                                         DialogInterface.OnClickListener { _, _ -> makeCall(number) }),
                negativeButton = KarhooAlertDialogAction(R.string.dismiss,
                                                         DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() }))
        KarhooAlertDialogHelper(context).showAlertDialog(config)
    }

    override fun makeCall(number: String) {
        IntentUtils.dialIntent(number)?.let { context.startActivity(it) }
    }

    override fun enableCallDriver() {
        contactDriverButton.visibility = View.VISIBLE
    }

    override fun enableCallFleet() {
        contactFleetButton.visibility = View.VISIBLE
    }

    override fun disableCallFleet() {
        contactFleetButton.visibility = View.GONE
    }

    override fun disableCallDriver() {
        contactDriverButton.visibility = View.GONE
    }

    override fun enableCancelButton() {
        cancelButton.apply {
            cancelButton.setOnClickListener { presenter.cancelPressed() }
            visibility = View.VISIBLE
        }
    }

    override fun disableCancelButton() {
        if (cancellationDialog?.isShowing == true) {
            cancellationDialog?.dismiss()
        }
        cancelButton.apply {
            setOnClickListener { }
            visibility = View.INVISIBLE
        }
    }

    override fun showLoadingDialog(show: Boolean) {
        if (show) {
            progressDialog = ProgressDialog(context).apply {
                setTitle(R.string.cancelling_ride)
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                isIndeterminate = true
                setProgressNumberFormat(null)
                setProgressPercentFormat(null)
                show()
            }
        } else {
            progressDialog?.cancel()
        }
    }

    override fun observeTripStatus(bookingStatusPresenter: BookingStatusMVP.Presenter) {
        bookingStatusPresenter.addTripInfoObserver(presenter)
    }

    override fun observeTripStatus(rideDetailPresenter: RideDetailMVP.Presenter) {
        rideDetailPresenter.addTripInfoObserver(presenter)
    }

    override fun showError(@StringRes errorMessageId: Int, karhooError: KarhooError?) {
        actions?.showTemporaryError(resources.getString(errorMessageId), karhooError)
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
                                                             presenter.cancelTrip()
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
}
