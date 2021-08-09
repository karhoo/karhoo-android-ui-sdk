package com.karhoo.uisdk.screen.booking.checkout.checkoutActivity.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.QuoteVehicle
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.PoiType
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.booking.BookingCodes
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogAction
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogConfig
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogHelper
import com.karhoo.uisdk.screen.booking.checkout.checkoutActivity.fragment.CheckoutFragmentContract
import com.karhoo.uisdk.screen.booking.checkout.payment.BookingPaymentMVP
import com.karhoo.uisdk.screen.booking.checkout.prebookconfirmation.PrebookConfirmationView
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.quotes.extendedcapabilities.Capability
import com.karhoo.uisdk.service.preference.KarhooPreferenceStore
import com.karhoo.uisdk.util.DateUtil
import com.karhoo.uisdk.util.extension.hideSoftKeyboard
import com.karhoo.uisdk.util.extension.isGuest
import kotlinx.android.synthetic.main.uisdk_booking_checkout_view.view.*
import kotlinx.android.synthetic.main.uisdk_view_booking_button.view.*
import org.joda.time.DateTime
import java.util.*

internal class CheckoutView @JvmOverloads constructor(context: Context,
                                                      attrs: AttributeSet? = null,
                                                      defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr), CheckoutViewContract.View, CheckoutViewContract.Actions,
        BookingPaymentMVP.PaymentViewActions, BookingPaymentMVP.PaymentActions,
        CheckoutViewContract.BookingRequestViewWidget {
    private var isGuest: Boolean = false

    private var holdOpenForPaymentFlow = false

    private var presenter: CheckoutViewContract.Presenter
    private lateinit var loadingButtonCallback: CheckoutFragmentContract.LoadingButtonListener
    private lateinit var termsListener: CheckoutFragmentContract.TermsListener

    private val bookingComments: String
        get() = bookingRequestCommentsWidget.getBookingOptionalInfo()

    init {
        View.inflate(context, R.layout.uisdk_booking_checkout_view, this)

        presenter = CheckoutViewPresenter(this,
                KarhooUISDK.analytics,
                KarhooPreferenceStore.getInstance(context),
                KarhooApi.tripService,
                KarhooApi.userStore)

        isGuest = isGuest()
        attachListeners()
        bookingRequestFlightDetailsWidget.setHintText(context.getString(R.string.kh_uisdk_add_flight_details))
    }

    override fun setListeners(loadingButtonCallback: CheckoutFragmentContract.LoadingButtonListener, termsListener: CheckoutFragmentContract.TermsListener) {
        this.loadingButtonCallback = loadingButtonCallback
        this.termsListener = termsListener
    }

    override fun showGuestBookingFields(details: PassengerDetails?) {
        if (details == null) {
            bookingRequestLabel.text = resources.getString(R.string.kh_uisdk_checkout_as_guest)
        }

        bookingRequestCommentsWidget.visibility = View.VISIBLE
    }

    override fun showAuthenticatedUserBookingFields() {
        bookingRequestCommentsWidget.visibility = View.GONE
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        if (holdOpenForPaymentFlow) {
            showLoading(false)
            holdOpenForPaymentFlow = false
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        isGuest = isGuest()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        presenter.onPaymentFailureDialogCancelled()
        presenter.clearData()
    }

    private fun attachListeners() {
        bookingRequestLinearLayout.setOnClickListener {
            it.hideSoftKeyboard()
        }

        bookingRequestPaymentDetailsWidget.cardActions = this
        bookingRequestPaymentDetailsWidget.paymentActions = this
        bookingRequestTermsWidget.actions = this
    }

    override fun initialiseChangeCard(quote: Quote?) {
        bookingRequestPaymentDetailsWidget.initialiseChangeCard(quote = quote)
    }

    override fun showBookingRequest(quote: Quote, bookingStatus: BookingStatus?, outboundTripId: String?, bookingMetadata:
    HashMap<String, String>?) {
        loadingButtonCallback.onLoadingComplete()
        bookingRequestLayout.visibility = View.VISIBLE
        presenter.showBookingRequest(quote = quote, bookingStatus = bookingStatus, outboundTripId = outboundTripId, bookingMetadata = bookingMetadata)
    }

    override fun bindEta(quote: Quote, card: String) {
        bookingRequestPriceWidget.bindETAOnly(quote.vehicle.vehicleQta.highMinutes,
                context.getString(R.string.kh_uisdk_estimated_arrival_time),
                quote.quoteType)
    }

    override fun bindPrebook(quote: Quote, card: String, date: DateTime) {
        val time = DateUtil.getTimeFormat(context, date)
        val currency = Currency.getInstance(quote.price.currencyCode)

        bookingRequestPriceWidget.bindPrebook(quote,
                time,
                DateUtil.getDateFormat(date),
                currency)
    }

    override fun bindPriceAndEta(quote: Quote, card: String) {
        val currency = Currency.getInstance(quote.price.currencyCode)

        bookingRequestPriceWidget?.bindViews(quote, context.getString(R.string.kh_uisdk_estimated_arrival_time), currency)
    }

    override fun bindQuoteAndTerms(vehicle: Quote, isPrebook: Boolean) {
        bookingRequestQuotesWidget.bindViews(
                vehicle.fleet.logoUrl,
                vehicle.fleet.name.orEmpty(),
                vehicle.vehicle.vehicleClass.orEmpty(),
                vehicle.serviceAgreements?.freeCancellation,
                vehicle.vehicle.vehicleTags,
                vehicle.fleet.description,
                isPrebook
        )
        bookingRequestTermsWidget.bindViews(vehicle)
    }

    override fun onTripBookedSuccessfully(tripInfo: TripInfo) {
        loadingButtonCallback.onLoadingComplete()

        // TODO pass info to the fragment

//        val data = Intent()
//        data.putExtra(BookingCheckoutActivity.BOOKING_REQUEST_TRIP_INFO_KEY, tripInfo)
//
//        setResult(AppCompatActivity.RESULT_OK, data)
//        finish()
    }

    override fun onError() {
        loadingButtonCallback.onLoadingComplete()
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

    override fun setCapacityAndCapabilities(capabilities: List<Capability>, vehicle: QuoteVehicle) {
        bookingRequestQuotesWidget.setCapacity(
                luggage = vehicle.luggageCapacity,
                people = vehicle.passengerCapacity)

        bookingRequestQuotesWidget.setCapabilities(capabilities)
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
        KarhooAlertDialogHelper(context).showAlertDialog(config)
    }

    override fun handlePaymentDetailsUpdate() {
        // Do nothing
    }

    override fun showErrorDialog(stringId: Int, karhooError: KarhooError?) {
        presenter.handleError(stringId, karhooError)
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
        val activity = context as Activity
        val activityWasStartedForResult = activity.callingActivity != null

        if (activityWasStartedForResult) {
            val data = Intent().apply {
                putExtra(BookingCodes.BOOKED_TRIP, tripInfo)
            }
            activity.setResult(Activity.RESULT_OK, data)
            activity.finish()
        } else {
            loadingButtonCallback.onLoadingComplete()

            val prebookConfirmationView = PrebookConfirmationView(context).apply {
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
            loadingButtonCallback.showLoading()
        } else {
            loadingButtonCallback.onLoadingComplete()
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

    override fun showWebView(url: String?) {
        termsListener.showWebViewOnPress(url)
    }

    // fragment should pass on the activity result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        bookingRequestPaymentDetailsWidget.onActivityResult(requestCode, resultCode, data)
    }

    override fun startBooking() {
        if (!presenter.isPaymentSet()) {
            bookingRequestPaymentDetailsWidget.callOnClick()
        } else {
            (bookingRequestLayout as View).hideSoftKeyboard()
            presenter.makeBooking()
        }
    }
}
