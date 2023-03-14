package com.karhoo.uisdk.screen.booking.checkout.component.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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
import com.karhoo.sdk.api.model.LoyaltyNonce
import com.karhoo.sdk.api.model.LoyaltyStatus
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogAction
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogConfig
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogHelper
import com.karhoo.uisdk.base.view.countrycodes.CountryPickerActivity
import com.karhoo.uisdk.base.view.countrycodes.CountryUtils.getDefaultCountryCode
import com.karhoo.uisdk.base.view.countrycodes.CountryUtils.getDefaultCountryDialingCode
import com.karhoo.uisdk.screen.address.static.AddressStaticComponent
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity.Companion.BOOKING_CHECKOUT_PREBOOK_QUOTE_KEY
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity.Companion.BOOKING_CHECKOUT_PREBOOK_QUOTE_TYPE_KEY
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity.Companion.BOOKING_CHECKOUT_PREBOOK_SKIP_RIDE_DETAILS_KEY
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity.Companion.BOOKING_CHECKOUT_PREBOOK_TRIP_INFO_KEY
import com.karhoo.uisdk.screen.booking.checkout.component.fragment.CheckoutFragmentContract
import com.karhoo.uisdk.screen.booking.checkout.loyalty.LoyaltyContract
import com.karhoo.uisdk.screen.booking.checkout.loyalty.LoyaltyMode
import com.karhoo.uisdk.screen.booking.checkout.loyalty.LoyaltyViewDataModel
import com.karhoo.uisdk.screen.booking.checkout.passengerdetails.PassengerDetailsContract
import com.karhoo.uisdk.screen.booking.checkout.payment.BookingPaymentContract
import com.karhoo.uisdk.screen.booking.checkout.payment.BookingPaymentHandler
import com.karhoo.uisdk.screen.booking.checkout.payment.WebViewActions
import com.karhoo.uisdk.screen.booking.checkout.bookingconfirmation.BookingConfirmationView
import com.karhoo.uisdk.screen.booking.checkout.comment.CheckoutCommentBottomSheet
import com.karhoo.uisdk.screen.booking.checkout.traveldetails.CheckoutTravelDetailsBottomSheet
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.quotes.VehicleMappingsProvider
import com.karhoo.uisdk.screen.booking.quotes.extendedcapabilities.Capability
import com.karhoo.uisdk.service.preference.KarhooPreferenceStore
import com.karhoo.uisdk.util.DateUtil
import com.karhoo.uisdk.util.extension.getCorrespondingLogoMapping
import com.karhoo.uisdk.util.extension.hideSoftKeyboard
import com.karhoo.uisdk.util.extension.isGuest
import com.karhoo.uisdk.util.returnErrorStringOrLogoutIfRequired
import kotlinx.android.synthetic.main.uisdk_booking_checkout_view.view.*
import kotlinx.android.synthetic.main.uisdk_view_booking_terms.view.*
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*

@Suppress("TooManyFunctions")
internal class CheckoutView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), CheckoutViewContract.View,
    BookingPaymentContract.PaymentViewActions, BookingPaymentContract.PaymentActions,
    CheckoutViewContract.BookingRequestViewWidget, WebViewActions {
    private var isGuest: Boolean = false
    private var isPrebook: Boolean = false

    private var holdOpenForPaymentFlow = false

    private var presenter: CheckoutViewContract.Presenter
    private lateinit var loadingButtonCallback: CheckoutFragmentContract.LoadingButtonListener
    private lateinit var webViewListener: CheckoutFragmentContract.WebViewListener
    private lateinit var passengersListener: CheckoutFragmentContract.PassengersListener
    private lateinit var bookingListener: CheckoutFragmentContract.BookingListener
    override var commentsListener: ((commentBottomSheet: CheckoutCommentBottomSheet) -> Unit?)? = null
    override var travelDetailsListener: ((travelDetailsBottomSheet: CheckoutTravelDetailsBottomSheet) -> Unit?)? = null

    private var bookingComments: String = ""

    private var flightInfo: String = ""
    private var trainInfo: String = ""

    private val bookingPaymentHandler = BookingPaymentHandler(context = context)

    init {
        View.inflate(context, R.layout.uisdk_booking_checkout_view, this)

        bookingPaymentHandler.cardActions = this
        bookingPaymentHandler.paymentActions = this
        bookingRequestTermsWidget.actions = this
        bookingRequestTermsWidget.checkBoxChangedCallback = ::termsAndConditionsCheckBoxCheckedChanged

        presenter = CheckoutViewPresenter(this,
                                          KarhooUISDK.analytics,
                                          KarhooPreferenceStore.getInstance(context),
                                          KarhooApi.tripService,
                                          KarhooApi.userStore)

        isGuest = isGuest()

        bookingRequestLinearLayout.setOnClickListener {
            it.hideSoftKeyboard()
        }

        presenter.retrievePassengerDetailsForShowing()

        bookingCheckoutPassengerView.setOnClickListener {
            showPassengerDetailsLayout(true)
        }

        bookingCheckoutCommentView.setOnClickListener {
            showCommentsDialog()
        }
        bindComments(null)

        bookingCheckoutTravelDetailsView.setOnClickListener {
            showTravelDetailsDialog()
        }
        presenter.identifyTravelDetails(isPrebook)

        passengersDetailLayout.validationCallback = object : PassengerDetailsContract.Validator {
            override fun onFieldsValidated(validated: Boolean) {
                loadingButtonCallback.enableButton(validated)
            }
        }

        loyaltyView.delegate = object : LoyaltyContract.LoyaltyViewDelegate {
            override fun onModeChanged(mode: LoyaltyMode) {
                //Might be implemented at some point
            }

            override fun onEndLoading() {
                //Maybe will be implemented
            }

            override fun onStartLoading() {
                //Maybe will be implemented
            }
        }

        showLoyaltyView(false)

        if (KarhooUISDKConfigurationProvider.configuration.isExplicitTermsAndConditionsConsentRequired()) {
            bookingRequestTermsWidget.khTermsAndConditionsCheckBox.setOnClickListener {
                loadingButtonCallback.checkState()
            }
        }
    }

    override fun setListeners(
        loadingButtonCallback: CheckoutFragmentContract.LoadingButtonListener,
        webViewListener: CheckoutFragmentContract.WebViewListener,
        passengersListener: CheckoutFragmentContract.PassengersListener,
        bookingListener: CheckoutFragmentContract.BookingListener
    ) {
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

    fun getPassengerDetails(): PassengerDetails? {
        return passengersDetailLayout.retrievePassenger()
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
        bookingPaymentHandler.initialiseChangeCard(quote = quote)
    }

    override fun showBookingRequest(
        quote: Quote, journeyDetails: JourneyDetails?,
        outboundTripId: String?,
        bookingMetadata: HashMap<String, String>?,
        passengerDetails: PassengerDetails?,
        comments: String?
    ) {
        loadingButtonCallback.onLoadingComplete()
        bookingCheckoutViewLayout.visibility = View.VISIBLE
        comments?.let {
            bindComments(it)
        }

        presenter.showBookingRequest(
            quote = quote,
            journeyDetails = journeyDetails,
            outboundTripId = outboundTripId,
            bookingMetadata = bookingMetadata,
            passengerDetails = passengerDetails
        )

        if (journeyDetails != null) {
            bindAddresses(journeyDetails, quote)
        }

        bookingPaymentHandler.getPaymentProvider()

    }

    override fun bindEta(quote: Quote, card: String) {
        bookingRequestPriceWidget.bindETAOnly(
            quote.vehicle.vehicleQta.highMinutes,
            context.getString(R.string.kh_uisdk_estimated_arrival_time),
            quote.quoteType
        )
    }

    override fun bindPrebook(quote: Quote, card: String, date: DateTime) {
        val time = DateUtil.getTimeFormat(context, date)
        val currency = Currency.getInstance(quote.price.currencyCode)
        isPrebook = true

        bottomPriceView.bindViews(quote, currency)
        bookingRequestPriceWidget.bindPrebook(
            quote,
            time,
            DateUtil.getDateFormat(date),
            currency
        )

        presenter.identifyTravelDetails(isPrebook)
    }

    override fun bindPriceAndEta(quote: Quote, card: String) {
        val currency = Currency.getInstance(quote.price.currencyCode)

        bottomPriceView.bindViews(quote, currency)
        bookingRequestPriceWidget?.bindViews(
            quote,
            context.getString(R.string.kh_uisdk_estimated_arrival_time),
            currency
        )
    }

    override fun bindQuoteAndTerms(vehicle: Quote, isPrebook: Boolean) {
        val logoImageUrl = VehicleMappingsProvider.getVehicleMappings()?.let {
            vehicle.vehicle.getCorrespondingLogoMapping(it)?.vehicleImagePNG
        } ?: vehicle.fleet.logoUrl

        bookingRequestQuotesWidget.bindViews(
            logoImageUrl,
            vehicle.fleet.name.orEmpty(),
            vehicle.vehicle.vehicleType,
            vehicle.serviceAgreements?.freeCancellation,
            vehicle.fleet.logoUrl,
            vehicle.vehicle.vehicleTags,
            isPrebook
        )
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

    override fun setCapacityAndCapabilities(capabilities: List<Capability>, vehicle: QuoteVehicle) {
        bookingRequestQuotesWidget.setCapacity(
            luggage = vehicle.luggageCapacity,
            people = vehicle.passengerCapacity,
            capabilitiesCount = capabilities.size
        )

//        bookingRequestQuotesWidget.setCapabilities(capabilities)
    }

    override fun showPaymentFailureDialog(stringId: Int?, error: KarhooError?) {
        val config = KarhooAlertDialogConfig(
            titleResId = R.string.kh_uisdk_payment_issue,
            messageResId = stringId ?: R.string.kh_uisdk_payment_issue_message,
            karhooError = error,
            positiveButton = KarhooAlertDialogAction(
                R.string.kh_uisdk_add_card
            ) { d, _ ->
                presenter.onPaymentFailureDialogPositive()
                d.dismiss()
            },
            negativeButton = KarhooAlertDialogAction(
                R.string.kh_uisdk_cancel
            ) { d, _ ->
                presenter.onPaymentFailureDialogCancelled()
                d.dismiss()
            })
        KarhooAlertDialogHelper(context).showPaymentFailureDialog(config)
    }

    override fun handlePaymentDetailsUpdate() {
        loadingButtonCallback.setState(
            presenter.getBookingButtonState(
                arePassengerDetailsValid(),
                isTermsCheckBoxValid()
            )
        )
    }

    override fun showErrorDialog(stringId: Int, karhooError: KarhooError?) {
        showPaymentFailureDialog(stringId, karhooError)
    }

    override fun handleChangeCard() {
        presenter.handleChangeCard()
    }

    override fun showPaymentUI() {
        holdOpenForPaymentFlow = true
    }

    override fun showPrebookConfirmationDialog(
        quoteType: QuoteType?,
        tripInfo: TripInfo?,
        journeyDetails: JourneyDetails?,
        quote: Quote?
    ) {
        hideSoftKeyboard()

        if (journeyDetails != null) {

            val bookingConfirmationView = BookingConfirmationView(
                journeyDetails,
                quote,
                tripInfo?.flightNumber,
                tripInfo?.trainNumber,
                tripInfo?.tripId,
                KarhooUISDKConfigurationProvider.configuration.useAddToCalendarFeature()
            )

            bookingConfirmationView.setLoyaltyProperties(
                loyaltyView.visibility == VISIBLE,
                loyaltyView.getCurrentMode(),
                loyaltyView.getPoints()
            )

            bookingConfirmationView.actions =
                object : CheckoutViewContract.BookingConfirmationActions {
                    override fun openRideDetails() {
                        val activity = context as Activity
                        val data = Intent().apply {
                            putExtra(BOOKING_CHECKOUT_PREBOOK_TRIP_INFO_KEY, tripInfo)
                            putExtra(BOOKING_CHECKOUT_PREBOOK_QUOTE_TYPE_KEY, quoteType)
                            putExtra(BOOKING_CHECKOUT_PREBOOK_QUOTE_KEY, quote)
                        }
                        activity.setResult(Activity.RESULT_OK, data)
                        activity.finish()
                    }

                    override fun dismissedPrebookDialog() {
                        val activity = context as Activity

                        val data = Intent().apply {
                            putExtra(BOOKING_CHECKOUT_PREBOOK_TRIP_INFO_KEY, tripInfo)
                            putExtra(BOOKING_CHECKOUT_PREBOOK_SKIP_RIDE_DETAILS_KEY, true)
                        }

                        activity.setResult(Activity.RESULT_OK, data)
                        activity.finish()
                    }
                }

            (context as CheckoutActivity).supportFragmentManager.let {
                bookingConfirmationView.show(it, BookingConfirmationView.TAG)
            }

        }
    }

    override fun showLoading(show: Boolean) {
        if (show) {
            loadingButtonCallback.showLoading()
        } else {
            loadingButtonCallback.onLoadingComplete()
        }
    }

    override fun showUpdatedPaymentDetails(savedPaymentInfo: SavedPaymentInfo?) {
        bookingPaymentHandler.bindPaymentDetails(savedPaymentInfo)
    }

    override fun threeDSecureNonce(threeDSNonce: String, tripId: String?) {
        showLoading(true)
        presenter.passBackPaymentIdentifiers(threeDSNonce, tripId, passengersDetailLayout.retrievePassenger(), bookingComments, flightInfo,
            trainInfo.ifEmpty { null })
    }

    override fun initialisePaymentProvider(quote: Quote?) {
        bookingPaymentHandler.initialisePaymentFlow(quote)
    }

    override fun initialiseGuestPayment(quote: Quote?) {
        bookingPaymentHandler.initialiseGuestPayment(quote)
    }

    override fun showWebView(url: String?) {
        webViewListener.showWebViewOnPress(url)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CountryPickerActivity.COUNTRY_PICKER_ACTIVITY_CODE && resultCode ==
            CountryPickerActivity.COUNTRY_PICKER_ACTIVITY_RESULT_CODE
        ) {

            val countryCode = data?.getStringExtra(CountryPickerActivity.COUNTRY_CODE_KEY) ?: ""
            val dialingCode = data?.getStringExtra(
                CountryPickerActivity
                    .COUNTRY_DIALING_CODE_KEY
            ) ?: ""

            passengersDetailLayout.setCountryFlag(
                countryCode, dialingCode, true,
                focusPhoneNumber = true
            )
        } else {
            bookingPaymentHandler.setPassengerDetails(passengersDetailLayout.getPassengerDetails())
            val paymentSuccess =
                bookingPaymentHandler.onActivityResult(requestCode, resultCode, data)
            if (requestCode != BookingActivity.REQ_CODE_BRAINTREE)
                loadingButtonCallback.onLoadingComplete()

            if (paymentSuccess) {
                bookingListener.startBookingProcess()
            }
        }
    }

    override fun startBooking() {
        if (!isTermsCheckBoxValid()) {
            loadingButtonCallback.onLoadingComplete()
            bookingRequestTermsWidget.khTermsAndConditionsCheckBox.buttonTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.kh_uisdk_error)
                )
            val shake: Animation = AnimationUtils.loadAnimation(context, R.anim.uisdk_shake_control)
            bookingRequestTermsWidget.khTermsAndConditionsCheckBox.startAnimation(shake)
            bookingRequestTermsWidget.khTermsAndConditionsCheckBox.requestFocus()
            bookingCheckoutViewLayout.smoothScrollTo(0, bookingRequestTermsWidget.bottom)
        }
        else if (!presenter.isPaymentSet()) {
            bookingPaymentHandler.setPassengerDetails(passengersDetailLayout.getPassengerDetails())
            bookingPaymentHandler.changeCard()
        } else {
            (bookingCheckoutViewLayout as View).hideSoftKeyboard()
            presenter.makeBooking()
        }
    }

    override fun bindPassenger(passengerDetails: PassengerDetails?) {
        bookingCheckoutPassengerView.setActionIcon(R.drawable.kh_uisdk_ic_passenger)

        passengerDetails?.let {
            passengersDetailLayout.setPassengerDetails(it)

        } ?: run {
            val countryCode = getDefaultCountryCode(context)
            passengersDetailLayout.setCountryFlag(
                countryCode,
                getDefaultCountryDialingCode(countryCode),
                false
            )
        }

        if (arePassengerDetailsValid()) {
            bookingCheckoutPassengerView.setTitle(passengerDetails?.firstName + " " + passengerDetails?.lastName)
            bookingCheckoutPassengerView.setSubtitle(passengerDetails?.phoneNumber.toString())
        } else {
            bookingCheckoutPassengerView.setTitle(resources.getString(R.string.kh_uisdk_booking_checkout_passenger))
            bookingCheckoutPassengerView.setSubtitle(resources.getString(R.string.kh_uisdk_booking_checkout_add_passenger))
        }
    }

    override fun bindAddresses(journeyDetails: JourneyDetails, quote: Quote) {
        checkoutAddressComponent.setup(
            pickup = journeyDetails.pickup!!,
            destination = journeyDetails.destination!!,
            time = journeyDetails.date,
            type = AddressStaticComponent.AddressComponentType.NORMAL
        )

        journeyDetails.date?.let {
            checkoutDateText.text = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                    .format(it.toDate())
                    .uppercase(Locale.getDefault())

            checkoutAddressComponent.setType(
                AddressStaticComponent.AddressComponentType.WITH_TIME,
                time = it
            )
        } ?: run {
            checkoutDateText.text = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                .format(DateTime().toDate())
                .uppercase(Locale.getDefault())

            val eta = String.format("%s %s", quote.vehicle.vehicleQta.highMinutes, context.getString(R.string.kh_uisdk_min))

            checkoutAddressComponent.setType(
                AddressStaticComponent.AddressComponentType.WITH_TEXT,
                eta.uppercase(Locale.getDefault())
            )
        }
    }

    private fun bindComments(comments: String?){
        bookingCheckoutCommentView.setActionIcon(R.drawable.kh_uisdk_ic_checkout_comments)

        bookingCheckoutCommentView.setTitle(context.getString(R.string.kh_uisdk_checkout_comments_title))
        comments?.let {
            bookingComments = it
            if (it.isEmpty())
                bookingCheckoutCommentView.setSubtitle(context.getString(R.string.kh_uisdk_checkout_comments_subtitle))
            else
                bookingCheckoutCommentView.setSubtitle(it)
        } ?: kotlin.run {
            bookingCheckoutCommentView.setSubtitle(context.getString(R.string.kh_uisdk_checkout_comments_subtitle))
        }
    }

    private fun showCommentsDialog() {
        val commentBottomSheet = CheckoutCommentBottomSheet().apply {
            onCommentsChanged = {
                bindComments(it)
            }
            bookingComments.isNotEmpty().let {
                initialComments = bookingComments
            }
        }
        commentsListener?.invoke(commentBottomSheet)
    }

    override fun bindTravelDetails(poiType: PoiType?, travelDetails: String?){
        when(poiType){
            PoiType.AIRPORT -> {
                setTravelDetailsView(true, travelDetails)
            }
            PoiType.TRAIN_STATION -> {
                setTravelDetailsView(false, travelDetails)
            }
            else -> {
                bookingCheckoutTravelDetailsView.visibility = GONE
            }
        }
    }

    private fun setTravelDetailsView(isAirport: Boolean, travelDetails: String?){
        bookingCheckoutTravelDetailsView.visibility = VISIBLE
        bookingCheckoutTravelDetailsView.setActionIcon(if(isAirport) R.drawable.kh_uisdk_ic_checkout_airport else R.drawable.kh_uisdk_ic_checkout_train)
        bookingCheckoutTravelDetailsView.setTitle(if(isAirport) context.getString(R.string.kh_uisdk_checkout_airport_title) else context.getString(R.string.kh_uisdk_checkout_train_title))
        bookingCheckoutTravelDetailsView.setSubtitle(if(isAirport) context.getString(R.string.kh_uisdk_checkout_airport_subtitle) else context.getString(R.string.kh_uisdk_checkout_train_subtitle))

        travelDetails?.let {
            flightInfo = if(isAirport) it else ""
            trainInfo = if(!isAirport) it else ""

            if(it.isNotEmpty())
                bookingCheckoutTravelDetailsView.setSubtitle(it)
            else
                bookingCheckoutTravelDetailsView.setSubtitle(if(isAirport) context.getString(R.string.kh_uisdk_checkout_airport_subtitle) else context.getString(R.string.kh_uisdk_checkout_train_subtitle))
        }
    }

    private fun showTravelDetailsDialog(){
        val travelDetailsBottomSheet = CheckoutTravelDetailsBottomSheet().apply {
            isFlight = presenter.getJourneyDetails()?.pickup?.details?.type == PoiType.AIRPORT
            onValueChanged = {
                bindTravelDetails(if(isFlight) PoiType.AIRPORT else PoiType.TRAIN_STATION, it)
            }
            if(flightInfo.isNotEmpty())
                initialValue = flightInfo
            if(trainInfo.isNotEmpty())
                initialValue = trainInfo
        }
        travelDetailsListener?.invoke(travelDetailsBottomSheet)
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
        this.bottomSectionContainer.visibility = if (!show) VISIBLE else GONE
        bookingCheckoutViewLayout.visibility = if (show) GONE else VISIBLE

        fillInPassengerDetails(passengersDetailLayout.getPassengerDetails())

        passengersListener.onPassengerPageVisibilityChanged(show)

        loadingButtonCallback.enableButton(if (show) arePassengerDetailsValid() else true)
    }

    override fun retrieveLoyaltyStatus() {
        presenter.createLoyaltyViewResponse()
    }

    override fun showLoadingButton(loading: Boolean) {
        showLoading(loading)
    }

    override fun showLoyaltyView(show: Boolean, loyaltyViewDataModel: LoyaltyViewDataModel?) {
        loyaltyView.visibility = if (show) VISIBLE else GONE
        loyaltyViewDataModel?.let {
            loyaltyView.set(it, callback = { result: Resource<LoyaltyStatus> ->
                when (result) {
                    is Resource.Success -> {
                        KarhooUISDK.analytics?.loyaltyStatusRequested(
                            quoteId = presenter.getCurrentQuote()?.id,
                            correlationId = result.correlationId,
                            loyaltyMode = loyaltyView.getCurrentMode().name,
                            loyaltyStatus = result.data,
                            loyaltyName = KarhooApi.userStore.paymentProvider?.loyalty?.name,
                            errorMessage = null,
                            errorSlug = null
                        )
                    }
                    is Resource.Failure -> {
                        KarhooUISDK.analytics?.loyaltyStatusRequested(
                            quoteId = presenter.getCurrentQuote()?.id,
                            correlationId = result.correlationId,
                            loyaltyMode = loyaltyView.getCurrentMode().name,
                            loyaltyStatus = null,
                            errorSlug = result.error.internalMessage,
                            loyaltyName = KarhooApi.userStore.paymentProvider?.loyalty?.name,
                            errorMessage = result.error.internalMessage
                        )
                    }
                }

            })
        }
        loyaltyView.set(LoyaltyMode.NONE)
    }

    override fun getDeviceLocale(): String {
        return resources.getString(R.string.kh_uisdk_locale)
    }

    override fun isTermsCheckBoxValid(): Boolean {
        if (KarhooUISDKConfigurationProvider.configuration.isExplicitTermsAndConditionsConsentRequired())
            return bookingRequestTermsWidget.khTermsAndConditionsCheckBox.isChecked

        return true
    }

    override fun consumeBackPressed(): Boolean = presenter.consumeBackPressed()

    override fun isPassengerDetailsViewVisible(): Boolean = passengersDetailLayout.visibility ==
            VISIBLE

    override fun arePassengerDetailsValid(): Boolean = passengersDetailLayout.areFieldsValid()

    override fun checkLoyaltyEligiblityAndStartPreAuth(): Boolean {
        return if (loyaltyView.visibility == VISIBLE) {
            loyaltyView.getLoyaltyPreAuthNonce { result, loyaltyStatus ->
                when (result) {
                    is Resource.Success -> {
                        presenter.setLoyaltyNonce(result.data.nonce)
                        startBooking()
                    }
                    is Resource.Failure -> {
                        if (result.error.code == CUSTOM_ERROR_PREFIX + KarhooError.ErrMissingBrowserInfo.code) {
                            //Start the booking even if the loyalty is in an error state
                            startBooking()
                            return@getLoyaltyPreAuthNonce
                        }

                        val reasonId = returnErrorStringOrLogoutIfRequired(result.error)

                        val config = KarhooAlertDialogConfig(
                            titleResId = R.string.kh_uisdk_error,
                            messageResId = reasonId,
                            karhooError = null,
                            positiveButton = KarhooAlertDialogAction(
                                R.string.kh_uisdk_ok
                            ) { d, _ ->
                                loadingButtonCallback.onLoadingComplete()
                                d.dismiss()
                            },
                            negativeButton = null
                        )
                        KarhooAlertDialogHelper(context).showAlertDialog(config)
                    }
                }

                logLoyaltyPreAuthEvent(result)
            }
            true
        } else {
            false
        }
    }

    private fun logLoyaltyPreAuthEvent(result: Resource<LoyaltyNonce>) {
        when (result) {
            is Resource.Success -> {
                KarhooUISDK.analytics?.loyaltyPreAuthSuccess(
                    quoteId = presenter.getCurrentQuote()?.id,
                    correlationId = result.correlationId,
                    loyaltyMode = loyaltyView.getCurrentMode().name
                )
            }
            is Resource.Failure -> {
                KarhooUISDK.analytics?.loyaltyPreAuthFailure(
                    quoteId = presenter.getCurrentQuote()?.id,
                    correlationId = result.correlationId,
                    loyaltyMode = loyaltyView.getCurrentMode().name,
                    errorSlug = result.error.internalMessage,
                    errorMessage = result.error.internalMessage
                )
            }
        }
    }

    private fun termsAndConditionsCheckBoxCheckedChanged() {
        loadingButtonCallback.setState(
            presenter.getBookingButtonState(
                arePassengerDetailsValid(),
                isTermsCheckBoxValid()
            )
        )
    }

    companion object {
        private const val CUSTOM_ERROR_PREFIX = "KSDK00 "
        private const val DATE_FORMAT = "EEEE, dd MMMM yyyy"
    }
}
