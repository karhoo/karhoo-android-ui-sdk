package com.karhoo.uisdk.screen.booking.checkout.component.views

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
import com.karhoo.sdk.api.model.PoiType
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.QuoteVehicle
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogAction
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogConfig
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogHelper
import com.karhoo.uisdk.base.view.countrycodes.CountryPickerActivity
import com.karhoo.uisdk.base.view.countrycodes.CountryUtils.getDefaultCountryCode
import com.karhoo.uisdk.base.view.countrycodes.CountryUtils.getDefaultCountryDialingCode
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity.Companion.BOOKING_CHECKOUT_PREBOOK_QUOTE_TYPE_KEY
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity.Companion.BOOKING_CHECKOUT_PREBOOK_TRIP_INFO_KEY
import com.karhoo.uisdk.screen.booking.checkout.component.fragment.CheckoutFragmentContract
import com.karhoo.uisdk.screen.booking.checkout.loyalty.LoyaltyContract
import com.karhoo.uisdk.screen.booking.checkout.loyalty.LoyaltyMode
import com.karhoo.uisdk.screen.booking.checkout.loyalty.LoyaltyViewDataModel
import com.karhoo.uisdk.screen.booking.checkout.passengerdetails.PassengerDetailsContract
import com.karhoo.uisdk.screen.booking.checkout.payment.BookingPaymentContract
import com.karhoo.uisdk.screen.booking.checkout.payment.WebViewActions
import com.karhoo.uisdk.screen.booking.domain.address.BookingInfo
import com.karhoo.uisdk.screen.booking.quotes.extendedcapabilities.Capability
import com.karhoo.uisdk.service.preference.KarhooPreferenceStore
import com.karhoo.uisdk.util.DateUtil
import com.karhoo.uisdk.util.VehicleTags
import com.karhoo.uisdk.util.extension.categoryToLocalisedString
import com.karhoo.uisdk.util.extension.hideSoftKeyboard
import com.karhoo.uisdk.util.extension.isGuest
import kotlinx.android.synthetic.main.uisdk_booking_checkout_view.view.bookingCheckoutPassengerView
import kotlinx.android.synthetic.main.uisdk_booking_checkout_view.view.bookingCheckoutViewLayout
import kotlinx.android.synthetic.main.uisdk_booking_checkout_view.view.bookingRequestCommentsWidget
import kotlinx.android.synthetic.main.uisdk_booking_checkout_view.view.bookingRequestFlightDetailsWidget
import kotlinx.android.synthetic.main.uisdk_booking_checkout_view.view.bookingRequestLinearLayout
import kotlinx.android.synthetic.main.uisdk_booking_checkout_view.view.bookingRequestPaymentDetailsWidget
import kotlinx.android.synthetic.main.uisdk_booking_checkout_view.view.bookingRequestPriceWidget
import kotlinx.android.synthetic.main.uisdk_booking_checkout_view.view.bookingRequestQuotesWidget
import kotlinx.android.synthetic.main.uisdk_booking_checkout_view.view.bookingRequestTermsWidget
import kotlinx.android.synthetic.main.uisdk_booking_checkout_view.view.loyaltyView
import kotlinx.android.synthetic.main.uisdk_booking_checkout_view.view.passengersDetailLayout
import org.joda.time.DateTime
import java.util.Currency
import java.util.HashMap

@Suppress("TooManyFunctions")
internal class CheckoutView @JvmOverloads constructor(context: Context,
                                                      attrs: AttributeSet? = null,
                                                      defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr), CheckoutViewContract.View,
                                                                               BookingPaymentContract.PaymentViewActions, BookingPaymentContract.PaymentActions,
                                                                               CheckoutViewContract.BookingRequestViewWidget, WebViewActions {
    private var isGuest: Boolean = false

    private var holdOpenForPaymentFlow = false

    private var presenter: CheckoutViewContract.Presenter
    private lateinit var loadingButtonCallback: CheckoutFragmentContract.LoadingButtonListener
    private lateinit var webViewListener: CheckoutFragmentContract.WebViewListener
    private lateinit var passengersListener: CheckoutFragmentContract.PassengersListener
    private lateinit var bookingListener: CheckoutFragmentContract.BookingListener

    private val bookingComments: String
        get() = bookingRequestCommentsWidget.getBookingOptionalInfo()

    private val flightInfo: String
        get() = bookingRequestFlightDetailsWidget.getBookingOptionalInfo()

    init {
        View.inflate(context, R.layout.uisdk_booking_checkout_view, this)

        bookingRequestPaymentDetailsWidget.cardActions = this
        bookingRequestPaymentDetailsWidget.paymentActions = this
        bookingRequestTermsWidget.actions = this

        presenter = CheckoutViewPresenter(this,
                                          KarhooUISDK.analytics,
                                          KarhooPreferenceStore.getInstance(context),
                                          KarhooApi.tripService,
                                          KarhooApi.userStore)

        isGuest = isGuest()

        bookingRequestLinearLayout.setOnClickListener {
            it.hideSoftKeyboard()
        }

        bookingRequestFlightDetailsWidget.setHintText(context.getString(R.string.kh_uisdk_add_flight_details))

        presenter.retrievePassengerDetailsForShowing()

        bookingCheckoutPassengerView.setOnClickListener {
            showPassengerDetailsLayout(true)
        }

        passengersDetailLayout.validationCallback = object : PassengerDetailsContract.Validator {
            override fun onFieldsValidated(validated: Boolean) {
                loadingButtonCallback.enableButton(validated)
            }
        }

        showLoyaltyView(false)
    }

    override fun setListeners(loadingButtonCallback: CheckoutFragmentContract.LoadingButtonListener,
                              webViewListener: CheckoutFragmentContract.WebViewListener,
                              passengersListener: CheckoutFragmentContract.PassengersListener,
                              bookingListener: CheckoutFragmentContract.BookingListener) {
        this.loadingButtonCallback = loadingButtonCallback
        this.webViewListener = webViewListener
        this.passengersListener = passengersListener
        this.bookingListener = bookingListener
    }

    override fun fillInPassengerDetails(details: PassengerDetails?) {
        if (details == null && !isGuest) {
            bindPassenger(passengersDetailLayout.retrievePassenger())
        } else {
            bindPassenger(details)
        }
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

    override fun initialiseChangeCard(quote: Quote?) {
        bookingRequestPaymentDetailsWidget.initialiseChangeCard(quote = quote)
    }

    override fun showBookingRequest(quote: Quote, bookingInfo: BookingInfo?,
                                    outboundTripId: String?,
                                    bookingMetadata: HashMap<String, String>?,
                                    passengerDetails: PassengerDetails?,
                                    comments: String?) {
        loadingButtonCallback.onLoadingComplete()
        bookingCheckoutViewLayout.visibility = View.VISIBLE
        comments?.let {
            bookingRequestCommentsWidget.setBookingOptionalInfo(comments)
        }

        presenter.showBookingRequest(
                quote = quote,
                bookingInfo = bookingInfo,
                outboundTripId = outboundTripId,
                bookingMetadata = bookingMetadata,
                passengerDetails = passengerDetails
                                    )

        bookingRequestPaymentDetailsWidget.getPaymentProvider()

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

        bookingRequestPriceWidget?.bindViews(quote,
                                             context.getString(R.string.kh_uisdk_estimated_arrival_time),
                                             currency)
    }

    override fun bindQuoteAndTerms(vehicle: Quote, isPrebook: Boolean) {
        bookingRequestQuotesWidget.bindViews(vehicle.fleet.logoUrl,
                                             vehicle.fleet.name.orEmpty(),
                                             vehicle.vehicle.categoryToLocalisedString(this.context).orEmpty(),
                                             vehicle.serviceAgreements?.freeCancellation,
                                             vehicle.vehicle.vehicleTags.map {
                                                 return@map VehicleTags(it)
                                             },
                                             vehicle.fleet.description,
                                             isPrebook)
        bookingRequestTermsWidget.bindViews(vehicle)
    }

    override fun onTripBookedSuccessfully(tripInfo: TripInfo) {
        loadingButtonCallback.onLoadingComplete()

        bookingListener.onTripBooked(tripInfo)
    }

    override fun onError(error: KarhooError?) {
        loadingButtonCallback.onLoadingComplete()

        bookingListener.onBookingFailed(error)
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
                people = vehicle.passengerCapacity,
                capabilitiesCount = capabilities.size
                                              )

        bookingRequestQuotesWidget.setCapabilities(capabilities)
    }

    override fun showPaymentFailureDialog(stringId: Int?, error: KarhooError?) {
        val config = KarhooAlertDialogConfig(
                titleResId = R.string.kh_uisdk_payment_issue,
                messageResId = stringId ?: R.string.kh_uisdk_payment_issue_message,
                karhooError = error,
                positiveButton = KarhooAlertDialogAction(R.string.kh_uisdk_add_card
                                                        ) { d, _ ->
                    presenter.onPaymentFailureDialogPositive()
                    d.dismiss()
                },
                negativeButton = KarhooAlertDialogAction(R.string.kh_uisdk_cancel
                                                        ) { d, _ ->
                    presenter.onPaymentFailureDialogCancelled()
                    d.dismiss()
                })
        KarhooAlertDialogHelper(context).showAlertDialog(config)
    }

    override fun handlePaymentDetailsUpdate() {
        loadingButtonCallback.setState(presenter.getBookingButtonState(arePassengerDetailsValid(), isPaymentMethodValid()))
    }

    override fun showErrorDialog(stringId: Int, karhooError: KarhooError?) {
        showPaymentFailureDialog(stringId, karhooError)
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
        hideSoftKeyboard()

        val activity = context as Activity
        val data = Intent().apply {
            putExtra(BOOKING_CHECKOUT_PREBOOK_TRIP_INFO_KEY, tripInfo)
            putExtra(BOOKING_CHECKOUT_PREBOOK_QUOTE_TYPE_KEY, quoteType)
        }
        activity.setResult(Activity.RESULT_OK, data)
        activity.finish()
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
        presenter.passBackPaymentIdentifiers(threeDSNonce, tripId, passengersDetailLayout.retrievePassenger(), bookingComments, flightInfo)
    }

    override fun initialisePaymentProvider(quote: Quote?) {
        bookingRequestPaymentDetailsWidget.initialisePaymentFlow(quote)
    }

    override fun initialiseGuestPayment(quote: Quote?) {
        bookingRequestPaymentDetailsWidget.initialiseGuestPayment(quote)
    }

    override fun showPaymentDialog(error: KarhooError?) {
        showPaymentFailureDialog(null, error)
    }

    override fun showWebView(url: String?) {
        webViewListener.showWebViewOnPress(url)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CountryPickerActivity.COUNTRY_PICKER_ACTIVITY_CODE && resultCode ==
                CountryPickerActivity.COUNTRY_PICKER_ACTIVITY_RESULT_CODE) {

            val countryCode = data?.getStringExtra(CountryPickerActivity.COUNTRY_CODE_KEY) ?: ""
            val dialingCode = data?.getStringExtra(CountryPickerActivity
                                                           .COUNTRY_DIALING_CODE_KEY) ?: ""

            passengersDetailLayout.setCountryFlag(countryCode, dialingCode, true,
                                                  focusPhoneNumber = true
                                                 )
        } else {
            bookingRequestPaymentDetailsWidget.setPassengerDetails(passengersDetailLayout.getPassengerDetails())
            bookingRequestPaymentDetailsWidget.onActivityResult(requestCode, resultCode, data)
            loadingButtonCallback.onLoadingComplete()
        }
    }

    override fun startBooking() {
        if (!presenter.isPaymentSet()) {
            bookingRequestPaymentDetailsWidget.setPassengerDetails(passengersDetailLayout.getPassengerDetails())
            bookingRequestPaymentDetailsWidget.callOnClick()
        } else {
            (bookingCheckoutViewLayout as View).hideSoftKeyboard()
            presenter.makeBooking()
        }
    }

    override fun bindPassenger(passengerDetails: PassengerDetails?) {
        bookingCheckoutPassengerView.setActionIcon(R.drawable.kh_uisdk_ic_passenger)

        passengerDetails?.let {
            bookingCheckoutPassengerView.setTitle(passengerDetails.firstName + " " + passengerDetails.lastName)
            bookingCheckoutPassengerView.setSubtitle(resources.getString(R.string.kh_uisdk_booking_checkout_edit_passenger))
            passengersDetailLayout.setPassengerDetails(it)

        } ?: run {
            bookingCheckoutPassengerView.setTitle(resources.getString(R.string.kh_uisdk_booking_checkout_passenger))
            bookingCheckoutPassengerView.setSubtitle(resources.getString(R.string.kh_uisdk_booking_checkout_add_passenger))

            val countryCode = getDefaultCountryCode(context)
            passengersDetailLayout.setCountryFlag(countryCode,
                                                  getDefaultCountryDialingCode(countryCode),
                                                  false)
        }

        bookingCheckoutPassengerView.setDottedBackground(!arePassengerDetailsValid())
    }

    /**
     * The user clicked to save the current passenger details
     */
    override fun clickedPassengerSaveButton() {
        passengersDetailLayout.clickOnSaveButton()
        showPassengerDetailsLayout(false)
        passengersListener.onPassengerSelected(passengersDetailLayout.retrievePassenger())
        bindPassenger(passengersDetailLayout.retrievePassenger())
    }

    /**
     * Display the passenger details page and disable the save button if the details are not valid
     */
    override fun showPassengerDetailsLayout(show: Boolean) {
        this.passengersDetailLayout.visibility = if (show) VISIBLE else GONE
        bookingCheckoutViewLayout.visibility = if (show) GONE else VISIBLE

        fillInPassengerDetails(passengersDetailLayout.getPassengerDetails())

        passengersListener.onPassengerPageVisibilityChanged(show)

        loadingButtonCallback.enableButton(if (show) arePassengerDetailsValid() else true)

        if (!show) {
            bookingCheckoutPassengerView.setDottedBackground(!arePassengerDetailsValid())
        }
    }

    override fun retrieveLoyaltyStatus() {
        presenter.createLoyaltyViewResponse()
        loyaltyView.getLoyaltyStatus()
    }

    override fun showLoyaltyView(show: Boolean, loyaltyViewDataModel: LoyaltyViewDataModel?) {
        loyaltyView.visibility = if (show) VISIBLE else GONE
        loyaltyViewDataModel?.let {
            loyaltyView.set(it)
            loyaltyView.setLoyaltyModeCallback(object : LoyaltyContract.LoyaltyModeCallback {
                override fun onModeChanged(mode: LoyaltyMode) {
                    if (mode == LoyaltyMode.ERROR) {
                        loadingButtonCallback.enableButton(false)
                    } else {
                        loadingButtonCallback.enableButton(true)
                    }
                }

                override fun onPreAuthorizationError(reasonId: Int) {
                    val config = KarhooAlertDialogConfig(
                            titleResId = R.string.error_dialog_title,
                            messageResId = reasonId,
                            karhooError = null,
                            positiveButton = KarhooAlertDialogAction(R.string.kh_uisdk_ok
                                                                    ) { d, _ ->
                                loadingButtonCallback.onLoadingComplete()
                                d.dismiss()
                            },
                            negativeButton = null)
                    KarhooAlertDialogHelper(context).showAlertDialog(config)
                }

                override fun onPreAuthorized(nonce: String) {
                    presenter.setLoyaltyNonce(nonce)
                    startBooking()
                }
            })
        }
        loyaltyView.set(LoyaltyMode.NONE)
    }

    override fun getDeviceLocale(): String {
        return resources.getString(R.string.karhoo_uisdk_locale)
    }

    override fun consumeBackPressed(): Boolean = presenter.consumeBackPressed()

    override fun isPassengerDetailsViewVisible(): Boolean = passengersDetailLayout.visibility ==
            VISIBLE

    override fun arePassengerDetailsValid(): Boolean = passengersDetailLayout.areFieldsValid()

    override fun isPaymentMethodValid(): Boolean = bookingRequestPaymentDetailsWidget.hasValidPaymentType()

    override fun checkLoyaltyEligiblityAndStartPreAuth(): Boolean {
        return if (loyaltyView.visibility == VISIBLE && loyaltyView.getCurrentMode() != LoyaltyMode.ERROR) {
            loyaltyView.preAuthorize()
            true
        } else {
            false
        }
    }
}
