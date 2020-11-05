package com.karhoo.uisdk.screen.trip.bookingstatus.contact

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
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
        contactFleetButton.setOnClickListener { presenter.contactFleet() }
    }

    override fun showCancelConfirmationDialog() {
        cancellationDialog = AlertDialog.Builder(context, R.style.DialogTheme)
                .setTitle(R.string.cancel_your_ride)
                .setMessage(R.string.cancellation_fee)
                .setPositiveButton(R.string.cancel) { _, _ -> presenter.cancelTrip() }
                .setNegativeButton(R.string.dismiss) { dialog, _ -> dialog.cancel() }
                .show()
    }

    override fun showTripCancelledDialog() {
        AlertDialog.Builder(context, R.style.DialogTheme)
                .setTitle(R.string.cancel_ride_successful)
                .setMessage(R.string.cancel_ride_successful_message)
                .setPositiveButton(R.string.ok) { _, _ -> actions?.goToCleanBooking() }
                .show()
    }

    override fun showCallToCancelDialog(number: String, quote: String) {
        AlertDialog.Builder(context, R.style.DialogTheme)
                .setTitle(R.string.difficulties_cancelling_title)
                .setMessage(R.string.difficulties_cancelling_message)
                .setPositiveButton(R.string.call) { _, _ -> makeCall(number) }
                .setNegativeButton(R.string.dismiss) { dialog, _ -> dialog.cancel() }
                .show()
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

    override fun showError(@StringRes errorMessageId: Int) {
        actions?.showTemporaryError(resources.getString(errorMessageId))
    }
}
