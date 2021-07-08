package com.karhoo.uisdk.screen.booking.booking.bookingrequest

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.LifecycleOwner
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.*
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.base.booking.BookingCodes
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogAction
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogConfig
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogHelper
import com.karhoo.uisdk.base.view.LoadingButtonView
import com.karhoo.uisdk.screen.booking.booking.payment.BookingPaymentMVP
import com.karhoo.uisdk.screen.booking.booking.prebookconfirmation.PrebookConfirmationView
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStateViewModel
import com.karhoo.uisdk.service.preference.KarhooPreferenceStore
import com.karhoo.uisdk.util.DateUtil
import com.karhoo.uisdk.util.extension.hideSoftKeyboard
import com.karhoo.uisdk.util.extension.isGuest
import kotlinx.android.synthetic.main.uisdk_activity_base.*
import kotlinx.android.synthetic.main.uisdk_booking_request.*
import kotlinx.android.synthetic.main.uisdk_view_booking_button.*
import org.joda.time.DateTime
import java.util.*

class BookingRequestActivity : BaseActivity(), BookingRequestContract.View, BookingRequestContract.Actions,
        BookingPaymentMVP.PaymentViewActions, BookingPaymentMVP.PaymentActions,
        BookingRequestViewContract.BookingRequestWidget, LoadingButtonView.Actions {
    override val layout: Int
        get() = R.layout.uisdk_booking_request

    private var isGuest: Boolean = false

    private var holdOpenForPaymentFlow = false

    private lateinit var presenter: BookingRequestContract.Presenter

    private val bookingComments: String
        get() = bookingRequestCommentsWidget.getBookingOptionalInfo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = BookingRequestActivityPresenter(this,
                KarhooUISDK.analytics,
                KarhooPreferenceStore.getInstance(this.applicationContext),
                KarhooApi.tripService,
                KarhooApi.userStore)

        isGuest = isGuest()
        attachListeners()
        bookingRequestFlightDetailsWidget.setHintText(this.getString(R.string.kh_uisdk_add_flight_details))
        presenter.setBookingFields(bookingRequestPassengerDetailsWidget.allFieldsValid())
        TextViewCompat.setTextAppearance(bookingRequestLabel, R.style.ButtonText)

        extras?.let { extras ->
            val quote = extras.getParcelable<Quote>(BOOKING_REQUEST_QUOTE_KEY)
            val bookingStatus = extras.getParcelable<BookingStatus>(BOOKING_REQUEST_STATUS_KEY)

            if (quote != null) {
                bookingRequestButton.onLoadingComplete()
                bookingRequestLayout.visibility = View.VISIBLE
                presenter.setBookingStatus(bookingStatus)
                presenter.showBookingRequest(
                        quote = quote,
                        outboundTripId = extras.getString(BOOKING_REQUEST_OUTBOUND_TRIP_ID_KEY),
                        bookingMetadata = extras.getSerializable(BOOKING_REQUEST_METADATA_KEY) as HashMap<String, String>?
                )
                //initialiseChangeCard(quote)
            }
        }

    }

    override fun handleExtras() {}

    override fun showGuestBookingFields(details: PassengerDetails) {
        bookingRequestPassengerDetailsWidget.visibility = ConstraintLayout.VISIBLE
        bookingRequestCommentsWidget.visibility = ConstraintLayout.VISIBLE
        passengerDetailsHeading.visibility = ConstraintLayout.VISIBLE
        bookingRequestPassengerDetailsWidget.setPassengerDetails(details)
    }

    override fun updateBookingButtonForGuest() {
        bookingRequestLabel.text = resources.getString(R.string.kh_uisdk_checkout_as_guest)
    }

    override fun showAuthenticatedUserBookingFields() {
        bookingRequestPassengerDetailsWidget.visibility = ConstraintLayout.GONE
        bookingRequestCommentsWidget.visibility = ConstraintLayout.GONE
        passengerDetailsHeading.visibility = ConstraintLayout.GONE
    }

    override fun onPause() {
        super.onPause()
        if (holdOpenForPaymentFlow) {
            resetBookingButton()
            holdOpenForPaymentFlow = false
        } else {
            presenter.hideBookingRequest()
        }
    }


    override fun onResume() {
        super.onResume()
        isGuest = isGuest()
    }

    override fun onStop() {
        super.onStop()
        presenter.clearData()
    }

    private fun attachListeners() {
        bookingRequestQuotesWidget.setOnClickListener {}
        cancelButton.setOnClickListener {
            presenter.clearData()
            it.hideSoftKeyboard()
            hideWindow()
        }

        bookingRequestLinearLayout.setOnClickListener {
            it.hideSoftKeyboard()
        }

        bookingRequestButton.actions = this
        bookingRequestPaymentDetailsWidget.cardActions = this
        bookingRequestPaymentDetailsWidget.paymentActions = this
        bookingRequestTermsWidget.actions = this

    }

    override fun initialiseChangeCard(quote: Quote?) {
        bookingRequestPaymentDetailsWidget.initialiseChangeCard(quote = quote)
    }

    override fun showBookingRequest(quote: Quote, outboundTripId: String?, bookingMetadata:
    HashMap<String, String>?) {
        bookingRequestButton.onLoadingComplete()
        bookingRequestLayout.visibility = View.VISIBLE
        presenter.showBookingRequest(quote = quote, outboundTripId = outboundTripId, bookingMetadata = bookingMetadata)
    }

    override fun bindViewToBookingStatus(lifecycleOwner: LifecycleOwner, bookingStatusStateViewModel: BookingStatusStateViewModel) {
    }

    override fun bindViewToBookingRequest(lifecycleOwner: LifecycleOwner, bookingRequestStateViewModel: BookingRequestStateViewModel) {
    }

    override fun bindEta(quote: Quote, card: String) {
        bookingRequestPriceWidget.bindETAOnly(quote.vehicle.vehicleQta.highMinutes,
                this.getString(R.string.kh_uisdk_estimated_arrival_time),
                quote.quoteType)
    }

    override fun bindPrebook(quote: Quote, card: String, date: DateTime) {
        val time = DateUtil.getTimeFormat(this, date)
        val currency = Currency.getInstance(quote.price.currencyCode)

        bookingRequestPriceWidget.bindPrebook(quote,
                time,
                DateUtil.getDateFormat(date),
                currency)
    }

    override fun bindPriceAndEta(quote: Quote, card: String) {
        val currency = Currency.getInstance(quote.price.currencyCode)

        bookingRequestPriceWidget?.bindViews(quote, this.getString(R.string.kh_uisdk_estimated_arrival_time), currency)
    }

    override fun bindQuoteAndTerms(vehicle: Quote, isPrebook: Boolean) {
        bookingRequestQuotesWidget.bindViews(
                vehicle.fleet.logoUrl,
                vehicle.fleet.name.orEmpty(),
                vehicle.vehicle.vehicleClass.orEmpty(),
                vehicle.serviceAgreements?.freeCancellation,
                isPrebook
        )
        bookingRequestTermsWidget.bindViews(vehicle)
    }

    private fun hideWindow() {
        presenter.onPaymentFailureDialogCancelled()
        finish();
    }

    override fun onLoadingButtonClick() {
        if (!(KarhooUISDKConfigurationProvider.configuration.authenticationMethod() is AuthenticationMethod.KarhooUser)
                && bookingRequestPassengerDetailsWidget.findAndfocusFirstInvalid()) {
            bookingRequestButton.onLoadingComplete()
        } else if (!presenter.isPaymentSet()) {
            bookingRequestPaymentDetailsWidget.callOnClick()
        } else {
            bookingRequestLayout.hideSoftKeyboard()
            presenter.makeBooking()
            cancelButton.isEnabled = false
        }
    }

    override fun onTripBookedSuccessfully(tripInfo: TripInfo) {
        bookingRequestButton.onLoadingComplete()

        val data = Intent()
        data.putExtra(BOOKING_REQUEST_TRIP_INFO_KEY, tripInfo)

        setResult(RESULT_OK, data)
        finish()
    }

    override fun onError() {
        bookingRequestButton.onLoadingComplete()
    }

    override fun resetBookingButton() {
        bookingRequestButton.onLoadingComplete()
        cancelButton.isEnabled = true
    }

    override fun enableCancelButton() {
        cancelButton.isEnabled = true
    }

    override fun displayFlightDetailsField(poiType: PoiType?) {
        when (poiType) {
            PoiType.AIRPORT -> {
                bookingRequestFlightDetailsWidget.visibility = View.VISIBLE
            }
            else -> bookingRequestFlightDetailsWidget.visibility = View.GONE
        }
    }

    override fun populateFlightDetailsField(flightNumber: String?) {
        flightNumber?.let { bookingRequestFlightDetailsWidget.setBookingOptionalInfo(it) }
    }

    override fun setCapacity(vehicle: QuoteVehicle) {
        bookingRequestQuotesWidget.setCapacity(
                luggage = vehicle.luggageCapacity,
                people = vehicle.passengerCapacity)
    }

    override fun showPaymentFailureDialog(error: KarhooError?) {
        val config = KarhooAlertDialogConfig(
                titleResId = R.string.kh_uisdk_payment_issue,
                messageResId = R.string.kh_uisdk_payment_issue_message,
                karhooError = error,
                positiveButton = KarhooAlertDialogAction(R.string.kh_uisdk_add_card,
                        DialogInterface.OnClickListener { dialog, _ ->
                            onPaymentHandlePositive()
                            dialog.dismiss()
                        }),
                negativeButton = KarhooAlertDialogAction(R.string.kh_uisdk_cancel,
                        DialogInterface.OnClickListener { dialog, _ ->
                            onPaymentHandleCancelled()
                            dialog.dismiss()
                        }))
        KarhooAlertDialogHelper(this).showAlertDialog(config)

    }

    override fun showErrorDialog(stringId: Int, karhooError: KarhooError?) {
        resetBookingButton()
        presenter.handleError(stringId, karhooError)
    }

    override fun handleChangeCard() {
        initialiseChangeCard()
    }

    override fun handleViewVisibility(visibility: Int) {
        bookingRequestPaymentDetailsWidget.visibility = visibility
    }

    override fun showPaymentUI() {
        holdOpenForPaymentFlow = true
    }

    override fun showPrebookConfirmationDialog(quoteType: QuoteType?, tripInfo: TripInfo) {
        val activity = this as Activity
        val activityWasStartedForResult = activity.callingActivity != null

        if (activityWasStartedForResult) {
            val data = Intent().apply {
                putExtra(BookingCodes.BOOKED_TRIP, tripInfo)
            }
            activity.setResult(Activity.RESULT_OK, data)
            activity.finish()
        } else {
            bookingRequestButton.onLoadingComplete()

            var prebookConfirmationView = PrebookConfirmationView(this).apply {
                bind(quoteType, tripInfo)
            }
            prebookConfirmationView.actions = this
        }
    }

    override fun finishedBooking() {
        presenter.resetBooking()
    }

    private fun onPaymentHandlePositive() {
        presenter.onPaymentFailureDialogPositive()
    }

    private fun onPaymentHandleCancelled() {
        presenter.onPaymentFailureDialogCancelled()
    }

    override fun hideLoading() {
        cancelButton.isEnabled = true
        bookingRequestButton.onLoadingComplete()
    }

    private fun showLoading() {
        cancelButton.isEnabled = false
        bookingRequestButton.showLoading()
    }

    override fun showUpdatedPaymentDetails(savedPaymentInfo: SavedPaymentInfo?) {
        bookingRequestPaymentDetailsWidget.bindPaymentDetails(savedPaymentInfo)
    }

    override fun threeDSecureNonce(threeDSNonce: String, tripId: String?) {
        showLoading()
        presenter.passBackPaymentIdentifiers(threeDSNonce, tripId,
                bookingRequestPassengerDetailsWidget.getPassengerDetails(), bookingComments)
    }

    override fun initialisePaymentProvider(quote: Quote?) {
        bookingRequestPaymentDetailsWidget.initialisePaymentFlow(quote)
    }

    override fun initialiseGuestPayment(quote: Quote?) {
        bookingRequestPaymentDetailsWidget.initialiseGuestPayment(quote)
    }

    override fun handlePaymentDetailsUpdate() {
        // not used
    }

    override fun showPaymentDialog(error: KarhooError?) {
        showPaymentFailureDialog(error)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        bookingRequestPaymentDetailsWidget.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun showWebView(url: String?) {
        url?.let { khWebView?.show(it) }
    }

    companion object {
        const val BOOKING_REQUEST_QUOTE_KEY = "BOOKING_REQUEST_INPUT_QUOTE"
        const val BOOKING_REQUEST_OUTBOUND_TRIP_ID_KEY = "BOOKING_REQUEST_OUTBOUND_TRIP_ID_KEY"
        const val BOOKING_REQUEST_METADATA_KEY = "BOOKING_REQUEST_METADATA_KEY"
        const val BOOKING_REQUEST_STATUS_KEY = "BOOKING_STATUS_KEY"
        const val BOOKING_REQUEST_TRIP_INFO_KEY = "TRIP_INFO_KEY"
        const val BOOKING_REQUEST_PASSENGER_KEY = "PASSENGER_KEY"
    }
}