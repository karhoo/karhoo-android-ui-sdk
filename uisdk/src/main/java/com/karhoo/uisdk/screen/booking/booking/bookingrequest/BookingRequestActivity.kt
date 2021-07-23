package com.karhoo.uisdk.screen.booking.booking.bookingrequest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.widget.TextViewCompat
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.QuoteVehicle
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.sdk.api.model.PoiType
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteType
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
import com.karhoo.uisdk.service.preference.KarhooPreferenceStore
import com.karhoo.uisdk.util.DateUtil
import com.karhoo.uisdk.util.extension.hideSoftKeyboard
import com.karhoo.uisdk.util.extension.isGuest
import kotlinx.android.synthetic.main.uisdk_activity_base.khWebView
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
                KarhooPreferenceStore.getInstance(applicationContext),
                KarhooApi.tripService,
                KarhooApi.userStore)

        isGuest = isGuest()
        attachListeners()
        bookingRequestFlightDetailsWidget.setHintText(getString(R.string.kh_uisdk_add_flight_details))
        TextViewCompat.setTextAppearance(bookingRequestLabel, R.style.ButtonText)

        extras?.let { extras ->
            val quote = extras.getParcelable<Quote>(BOOKING_REQUEST_QUOTE_KEY)
            val bookingStatus = extras.getParcelable<BookingStatus>(BOOKING_REQUEST_STATUS_KEY)

            if (quote != null) {
                bookingRequestButton.onLoadingComplete()
                bookingRequestLayout.visibility = VISIBLE
                presenter.setBookingStatus(bookingStatus)
                presenter.showBookingRequest(
                        quote = quote,
                        outboundTripId = extras.getString(BOOKING_REQUEST_OUTBOUND_TRIP_ID_KEY),
                        bookingMetadata = extras.getSerializable(BOOKING_REQUEST_METADATA_KEY) as HashMap<String, String>?
                )
            } else {
                finishWithError(BOOKING_REQUEST_ERROR_NO_QUOTE)
            }
        } ?: run {
            finishWithError(BOOKING_REQUEST_ERROR_NO_QUOTE)
        }
    }

    /**
     * Method used for finishing up the booking request activity with an error
     * The activity which launches the BookingRequestActivity should handle the error result
     * under BOOKING_REQUEST_ERROR (result code 10) and act accordingly
     * @param error reason for closing the BookingRequestActivity
     */
    private fun finishWithError(error: String) {
        val data = Intent()
        data.putExtra(BOOKING_REQUEST_ERROR_KEY, error)

        setResult(BOOKING_REQUEST_ERROR, data)
        finish()
    }

    override fun showGuestBookingFields(details: PassengerDetails?) {
        if (details == null) {
            bookingRequestLabel.text = resources.getString(R.string.kh_uisdk_checkout_as_guest)
        }

        bookingRequestCommentsWidget.visibility = VISIBLE
    }

    override fun showAuthenticatedUserBookingFields() {
        bookingRequestCommentsWidget.visibility = GONE
    }

    override fun onPause() {
        super.onPause()
        if (holdOpenForPaymentFlow) {
            showLoading(false)
            holdOpenForPaymentFlow = false
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
            presenter.onPaymentFailureDialogCancelled()
            finish();
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
        bookingRequestLayout.visibility = VISIBLE
        presenter.showBookingRequest(quote = quote, outboundTripId = outboundTripId, bookingMetadata = bookingMetadata)
    }

    override fun bindEta(quote: Quote, card: String) {
        bookingRequestPriceWidget.bindETAOnly(quote.vehicle.vehicleQta.highMinutes,
                getString(R.string.kh_uisdk_estimated_arrival_time),
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

        bookingRequestPriceWidget?.bindViews(quote, getString(R.string.kh_uisdk_estimated_arrival_time), currency)
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

    override fun onLoadingButtonClick() {
        if (KarhooUISDKConfigurationProvider.configuration.authenticationMethod() !is AuthenticationMethod.KarhooUser) {
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

    override fun enableCancelButton() {
        cancelButton.isEnabled = true
    }

    override fun displayFlightDetailsField(poiType: PoiType?) {
        when (poiType) {
            PoiType.AIRPORT -> {
                bookingRequestFlightDetailsWidget.visibility = VISIBLE
            }
            else -> bookingRequestFlightDetailsWidget.visibility = GONE
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
                positiveButton = KarhooAlertDialogAction(R.string.kh_uisdk_add_card) { dialog, _ ->
                    onPaymentHandle(true)
                    dialog.dismiss()
                },
                negativeButton = KarhooAlertDialogAction(R.string.kh_uisdk_cancel) { dialog, _ ->
                    onPaymentHandle(false)
                    dialog.dismiss()
                })
        KarhooAlertDialogHelper(this).showAlertDialog(config)
    }

    override fun handlePaymentDetailsUpdate() {
        // Do nothing
    }

    override fun showErrorDialog(stringId: Int, karhooError: KarhooError?) {
        presenter.handleError(stringId, karhooError)
    }

    override fun handleExtras() {
        // Do nothings
    }

    override fun handleChangeCard() {
        presenter.handleChangeCard()
    }

    override fun handleViewVisibility(visibility: Int) {
        bookingRequestPaymentDetailsWidget.visibility = visibility
    }

    override fun showPaymentUI() {
        holdOpenForPaymentFlow = true
    }

    override fun showPrebookConfirmationDialog(quoteType: QuoteType?, tripInfo: TripInfo) {
        val activityWasStartedForResult = callingActivity != null

        if (activityWasStartedForResult) {
            val data = Intent().apply {
                putExtra(BookingCodes.BOOKED_TRIP, tripInfo)
            }
            setResult(Activity.RESULT_OK, data)
            finish()
        } else {
            bookingRequestButton.onLoadingComplete()

            val prebookConfirmationView = PrebookConfirmationView(this).apply {
                bind(quoteType, tripInfo)
            }
            prebookConfirmationView.actions = this
        }
    }

    override fun finishedBooking() {
        presenter.resetBooking()
    }

    private fun onPaymentHandle(positive: Boolean) = if (positive) {
        presenter.onPaymentFailureDialogPositive()
    } else {
        presenter.onPaymentFailureDialogCancelled()
    }

    override fun showLoading(show: Boolean) {
        if (show) {
            cancelButton.isEnabled = false
            bookingRequestButton.showLoading()
        } else {
            cancelButton.isEnabled = true
            bookingRequestButton.onLoadingComplete()
        }
    }

    override fun showUpdatedPaymentDetails(savedPaymentInfo: SavedPaymentInfo?) {
        bookingRequestPaymentDetailsWidget.bindPaymentDetails(savedPaymentInfo)
    }

    override fun threeDSecureNonce(threeDSNonce: String, tripId: String?) {
        showLoading(true)
        presenter.passBackPaymentIdentifiers(threeDSNonce, tripId, null, bookingComments)
    }

    override fun initialisePaymentProvider(quote: Quote?) {
        bookingRequestPaymentDetailsWidget.initialisePaymentFlow(quote)
    }

    override fun initialiseGuestPayment(quote: Quote?) {
        bookingRequestPaymentDetailsWidget.initialiseGuestPayment(quote)
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

    /**
     * Intent Builder
     */
    class Builder {

        private val extrasBundle: Bundle = Bundle()

        /**
         * The activity will take the quote object and
         * use this to prepopulate eta, pricing, vechicle details fields
         * @param quote mandatory param for starting up the Booking Request Activity
         */
        fun quote(quote: Quote): Builder {
            extrasBundle.putParcelable(BOOKING_REQUEST_QUOTE_KEY, quote)
            return this
        }

        /**
         * The [outboundTripId] is expected when the trip is booked from a 'rebook' button in another activity,
         * It's used for analytics purposes only
         */
        fun outboundTripId(outboundTripId: String?): Builder {
            extrasBundle.putString(BOOKING_REQUEST_OUTBOUND_TRIP_ID_KEY, outboundTripId)
            return this
        }

        /**
         * If an [metadata] is passed in the activity, it will be used as part of the
         * Booking API meta data
         */
        fun bookingMetadata(metadata: HashMap<String, String>?): Builder {
            extrasBundle.putSerializable(BOOKING_REQUEST_METADATA_KEY, metadata)
            return this
        }

        /**
         * By passing booking status into the Booking activity it will automatically prefill the origin
         * destination and date of the desired trip. This will only use the details available inside
         * the BookingStatus object.
         */
        fun bookingStatus(bookingStatus: BookingStatus): Builder {
            extrasBundle.putParcelable(BOOKING_REQUEST_STATUS_KEY, bookingStatus)
            return this
        }

        /**
         * If a passenger is added, then it will be used to prefill the passenger details in the booking request component
         */
        fun passengerDetails(passenger: PassengerDetails): Builder {
            extrasBundle.putParcelable(BOOKING_REQUEST_PASSENGER_KEY, passenger)
            return this
        }

        /**
         * Returns a launchable Intent to the configured booking activity with the given
         * builder parameters in the extras bundle
         */
        fun build(context: Context): Intent = Intent(context, KarhooUISDK.Routing.bookingRequest).apply {
            putExtras(extrasBundle)
        }
    }

    companion object {
        const val BOOKING_REQUEST_QUOTE_KEY = "BOOKING_REQUEST_INPUT_QUOTE"
        const val BOOKING_REQUEST_OUTBOUND_TRIP_ID_KEY = "BOOKING_REQUEST_OUTBOUND_TRIP_ID_KEY"
        const val BOOKING_REQUEST_METADATA_KEY = "BOOKING_REQUEST_METADATA_KEY"
        const val BOOKING_REQUEST_STATUS_KEY = "BOOKING_STATUS_KEY"
        const val BOOKING_REQUEST_TRIP_INFO_KEY = "TRIP_INFO_KEY"
        const val BOOKING_REQUEST_PASSENGER_KEY = "PASSENGER_KEY"

        /** Errors outputted by the Booking Request Activity**/
        const val BOOKING_REQUEST_ERROR = 10
        const val BOOKING_REQUEST_ERROR_KEY = "BOOKING_REQUEST_ERROR_KEY"
        const val BOOKING_REQUEST_ERROR_NO_QUOTE = "BOOKING_REQUEST_ERROR_NO_QUOTE"
    }
}
