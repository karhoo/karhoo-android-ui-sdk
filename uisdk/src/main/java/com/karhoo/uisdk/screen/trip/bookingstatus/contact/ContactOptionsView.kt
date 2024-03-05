package com.karhoo.uisdk.screen.trip.bookingstatus.contact

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogAction
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogConfig
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogHelper
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.screen.rides.detail.RideDetailMVP
import com.karhoo.uisdk.screen.trip.bookingstatus.BookingStatusMVP
import com.karhoo.uisdk.util.IntentUtils

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

    private lateinit var cancelButton: Button
    private lateinit var contactDriverButton: Button
    private lateinit var contactFleetButton: Button

    init {
        inflate(context, R.layout.uisdk_view_contact_options, this)

        cancelButton = findViewById(R.id.cancelButton)
        contactDriverButton = findViewById(R.id.contactDriverButton)
        contactFleetButton = findViewById(R.id.contactFleetButton)

        contactDriverButton.setOnClickListener { presenter.contactDriver() }
        contactFleetButton.setOnClickListener {
            presenter.contactFleet()
        }
    }

    override fun showTripCancelledDialog() {

        val config = KarhooAlertDialogConfig(
                titleResId = R.string.kh_uisdk_cancel_ride_successful,
                messageResId = R.string.kh_uisdk_cancel_ride_successful_message,
                positiveButton = KarhooAlertDialogAction(R.string.kh_uisdk_dismiss,
                                                         DialogInterface.OnClickListener { _, _
                                                             -> actions?.goToNextScreen() }))
        KarhooAlertDialogHelper(context).showAlertDialog(config)
    }

    override fun showCallToCancelDialog(number: String, quote: String, karhooError: KarhooError) {
        val config = KarhooAlertDialogConfig(
                titleResId = R.string.kh_uisdk_difficulties_cancelling_title,
                messageResId = R.string.kh_uisdk_difficulties_cancelling_message,
                positiveButton = KarhooAlertDialogAction(R.string.kh_uisdk_call,
                                                         DialogInterface.OnClickListener { _, _ -> makeCall(number) }),
                negativeButton = KarhooAlertDialogAction(R.string.kh_uisdk_dismiss,
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
                setTitle(R.string.kh_uisdk_cancelling_ride)
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
        val message = if (formattedPrice.isNotEmpty())
            String.format(resources.getString(R.string.kh_uisdk_you_may_be_charged, formattedPrice)) else
            resources.getString(R.string.kh_uisdk_would_you_like_to_proceed)
        val config = KarhooAlertDialogConfig(
                titleResId = R.string.kh_uisdk_cancel_your_ride,
                message = message,
                cancellable = false,
                positiveButton = KarhooAlertDialogAction(R.string.kh_uisdk_dismiss,
                                                         DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() }),
                negativeButton = KarhooAlertDialogAction(R.string.kh_uisdk_ok,
                                                         DialogInterface.OnClickListener { _, _
                                                             ->
                                                             presenter.cancelTrip()
                                                         }))
        KarhooAlertDialogHelper(context).showAlertDialog(config)
    }

    private fun goToCleanBooking() {
        (context as Activity).startActivity(BookingActivity.Builder.builder.build(context))
    }
}
