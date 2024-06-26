package com.karhoo.uisdk.screen.booking.checkout.component.views

import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteVehicle
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.PoiType
import com.karhoo.sdk.api.network.request.Luggage
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.sdk.api.network.request.Passengers
import com.karhoo.sdk.api.network.request.TripBooking
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.booking.checkout.component.fragment.BookButtonState
import com.karhoo.uisdk.screen.booking.checkout.loyalty.LoyaltyViewDataModel
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStatus
import com.karhoo.uisdk.screen.booking.quotes.extendedcapabilities.Capability
import com.karhoo.uisdk.screen.booking.quotes.extendedcapabilities.CapabilityAdapter
import com.karhoo.uisdk.service.preference.PreferenceStore
import com.karhoo.uisdk.util.returnErrorStringOrLogoutIfRequired
import org.joda.time.DateTime
import java.util.Date

internal class CheckoutViewPresenter(
    view: CheckoutViewContract.View,
    private val analytics: Analytics?,
    private val preferenceStore: PreferenceStore,
    private val tripsService: TripsService,
    private val userStore: UserStore
) : BasePresenter<CheckoutViewContract.View>(), CheckoutViewContract.Presenter, LifecycleObserver {

    var journeyDetailsStateViewModel: JourneyDetailsStateViewModel? = null
    private var bookingRequestStateViewModel: BookingRequestStateViewModel? = null
    private var destination: LocationInfo? = null
    private var origin: LocationInfo? = null
    private var outboundTripId: String? = null
    private var quote: Quote? = null
    private var scheduledDate: DateTime? = null
    private var loyaltyNonce: String? = null
    private var bookingMetadata: HashMap<String, String>? = null

    init {
        attachView(view)
    }

    override fun getJourneyDetails(): JourneyDetails? {
        return JourneyDetails(
            date = scheduledDate,
            destination = destination,
            pickup = origin
        )
    }

    override fun setJourneyDetails(journeyDetails: JourneyDetails?) {
        journeyDetails?.let {
            scheduledDate = it.date
            destination = it.destination
            origin = it.pickup
        }
    }

    override fun getBookingButtonState(
        arePassengerDetailsValid: Boolean,
        isTermsCheckBoxValid: Boolean
    ): BookButtonState {
        return if(arePassengerDetailsValid && isTermsCheckBoxValid){
            BookButtonState.PAY
        } else {
            BookButtonState.NEXT
        }
    }

    override fun watchJourneyDetails(journeyDetailsStateViewModel: JourneyDetailsStateViewModel): Observer<in JourneyDetails> {
        this.journeyDetailsStateViewModel = journeyDetailsStateViewModel
        return Observer { currentStatus ->
            currentStatus?.let {
                scheduledDate = it.date
                destination = it.destination
                origin = it.pickup
            }
        }
    }

    override fun watchBookingRequest(bookingRequestStateViewModel: BookingRequestStateViewModel): Observer<BookingRequestStatus> {
        this.bookingRequestStateViewModel = bookingRequestStateViewModel
        return Observer { bookingRequestStatus ->
            bookingRequestStatus?.let {
                it.tripInfo
            }
        }
    }

    override fun makeBooking() {
        if (KarhooUISDKConfigurationProvider.isGuest()) {
            view?.initialiseGuestPayment(quote)
        } else {
            view?.initialisePaymentProvider(quote)
        }

        quote?.id?.let {
            analytics?.bookingRequested(quoteId = it)
        }
    }

    override fun getCurrentQuote(): Quote? {
        return quote
    }

    override fun isPaymentSet(): Boolean {
        return userStore.savedPaymentInfo != null
    }

    private fun onTripBookSuccess(tripInfo: TripInfo) {
        preferenceStore.lastTrip = tripInfo
        val date = scheduledDate
        if (date != null) {
            KarhooUISDK.analytics?.tripPrebookConfirmation(tripInfo)
            view?.showPrebookConfirmationDialog(
                quote?.quoteType,
                tripInfo,
                getJourneyDetails(),
                quote
            )
        } else {
            KarhooUISDK.analytics?.paymentSucceed()
            view?.onTripBookedSuccessfully(tripInfo)
            bookingRequestStateViewModel?.process(CheckoutViewContract.Event.BookingSuccess(tripInfo))
        }
        clearData()
    }

    private fun onTripBookFailure(error: KarhooError) {
        when (error) {
            KarhooError.CouldNotBookPaymentPreAuthFailed -> view?.showPaymentFailureDialog(
                null,
                error
            )
            KarhooError.InvalidRequestPayload -> handleError(
                R.string.kh_uisdk_booking_details_error,
                error
            )
            else -> handleError(returnErrorStringOrLogoutIfRequired(error), error)
        }
    }

    override fun clearData() {
        if (KarhooUISDKConfigurationProvider.isGuest()) {
            userStore.removeCurrentUser()
        }
        if ((KarhooUISDKConfigurationProvider.configuration.authenticationMethod() is AuthenticationMethod.TokenExchange &&
                    KarhooUISDKConfigurationProvider.configuration.paymentManager.shouldClearStoredPaymentMethod) ||
            KarhooUISDKConfigurationProvider.isGuest()
        ) {
            userStore.clearSavedPaymentInfo()
        }
    }

    override fun handleChangeCard() {
        view?.initialiseChangeCard(quote)
    }

    override fun retrievePassengerDetailsForShowing(passengerDetails: PassengerDetails?) {
        passengerDetails?.let {
            view?.fillInPassengerDetails(details = passengerDetails)
        } ?: run {
            when (KarhooUISDKConfigurationProvider.configuration.authenticationMethod()) {
                is AuthenticationMethod.Guest -> {
                    view?.fillInPassengerDetails(null)
                }
                else -> {
                    view?.fillInPassengerDetails(details = getPassengerDetailsFromUserStore())
                }
            }
        }
    }

    override fun consumeBackPressed(): Boolean {
        return if (view?.isPassengerDetailsViewVisible() == true) {
            view?.showPassengerDetailsLayout(false)
            view?.revertPassengerDetails()
            true
        } else {
            false
        }
    }

    override fun passBackPaymentIdentifiers(
        identifier: String,
        tripId: String?,
        passengerDetails: PassengerDetails?,
        comments: String,
        flightInfo: String,
        trainInfo: String?
    ) {
        val passenger = passengerDetails ?: getPassengerDetailsFromUserStore()

        passenger.locale.let {
            if (it.isNullOrEmpty() || !it.contains("_")) {
                passenger.locale = view?.getDeviceLocale()
            }
        }

        val flight = if (flightInfo.isNotEmpty()) {
            flightInfo
        } else {
            null
        }

        val metadata = getBookingMetadataMap(identifier, tripId)
        val additionalPassengers = metadata?.get(ADDITIONAL_PASSENGERS)?.toInt() ?: kotlin.run { 0 }
        val luggage = metadata?.get(LUGGAGE)?.toInt() ?: kotlin.run { 0 }

        tripsService.book(
            TripBooking(
                comments = comments,
                flightNumber = flight,
                trainNumber = trainInfo,
                meta = metadata,
                nonce = identifier,
                quoteId = quote?.id.orEmpty(),
                loyaltyNonce = loyaltyNonce,
                passengers = Passengers(
                    additionalPassengers = additionalPassengers,
                    passengerDetails = listOf(passenger),
                    luggage = Luggage(total = luggage)
                )
            )
        ).execute { result ->
            when (result) {
                is Resource.Success -> {
                    onTripBookSuccess(result.data)
                }
                is Resource.Failure -> {
                    onTripBookFailure(result.error)
                }
            }

            logBookingEvent(result)
        }
    }

    private fun getBookingMetadataMap(
        identifier: String,
        tripId: String?
    ): HashMap<String, String>? {
        return bookingMetadata?.let { bookingData ->
            tripId?.let { bookingData[TRIP_ID] = identifier }
            bookingData
        } ?: run {
            tripId?.let { hashMapOf(TRIP_ID to identifier) }
        }
    }

    private fun getPassengerDetailsFromUserStore(): PassengerDetails {
        val user = userStore.currentUser
        return PassengerDetails(
            firstName = user.firstName,
            lastName = user.lastName,
            phoneNumber = user.phoneNumber,
            email = user.email,
            locale = user.locale
        )
    }

    private fun refreshPaymentDetails() {
        //todo check if commenting out 241-244 will not break anything
        if ((KarhooUISDKConfigurationProvider.configuration.authenticationMethod() is AuthenticationMethod.TokenExchange &&
                    KarhooUISDKConfigurationProvider.configuration.paymentManager.showSavedPaymentInfo) || KarhooUISDKConfigurationProvider.isGuest()
        ) {
            view?.showUpdatedPaymentDetails(null)
        } else {
            view?.showUpdatedPaymentDetails(userStore.savedPaymentInfo)
        }
    }

    override fun showBookingRequest(
        quote: Quote, journeyDetails: JourneyDetails?, outboundTripId: String?, bookingMetadata:
        HashMap<String, String>?, passengerDetails: PassengerDetails?
    ) {
        retrievePassengerDetailsForShowing(passengerDetails)
        setJourneyDetails(journeyDetails)
        refreshPaymentDetails()
        this.bookingMetadata = bookingMetadata
        if (origin != null && destination != null) {
            this.quote = quote
            KarhooUISDK.analytics?.checkoutOpened(quote = quote)
            this.outboundTripId = outboundTripId
            handleBookingType(quote)
            view?.setCapacityAndCapabilities(
                createCapabilityByType(
                    quote.fleet.capabilities,
                    quote.vehicle
                ), quote.vehicle
            )
        } else if (origin == null) {
            handleError(R.string.kh_uisdk_origin_book_error, null)
        } else if (destination == null) {
            handleError(R.string.kh_uisdk_destination_book_error, null)
        }
    }

    override fun handleError(@StringRes stringId: Int, karhooError: KarhooError?) {
        view?.onError(karhooError)
        bookingRequestStateViewModel?.process(
            CheckoutViewContract.Event.BookingError(
                stringId,
                karhooError
            )
        )
        clearData()
    }

    private fun handleBookingType(quote: Quote) {
        if (scheduledDate != null) {
            scheduledDate?.let {
                view?.bindPrebook(quote, "", it)
                view?.bindQuoteAndTerms(quote, isPrebook = true)
            }
        } else if (destination != null && quote.price.highPrice > 0) {
            view?.bindPriceAndEta(quote, "")
            view?.bindQuoteAndTerms(quote, isPrebook = false)
        } else {
            view?.bindEta(quote, "")
            view?.bindQuoteAndTerms(quote, isPrebook = false)
        }
    }

    override fun onPaymentFailureDialogPositive() {
        view?.showLoading(false)
        handleChangeCard()
    }

    override fun onPaymentFailureDialogCancelled() {
        view?.showLoading(false)
    }

    private fun createCapabilityByType(
        capabilityTypes: List<String>?,
        vehicle: QuoteVehicle
    ): List<Capability> {
        val capabilitiesList = arrayListOf<Capability>()

        capabilitiesList.add(
            Capability(
                CapabilityAdapter.PASSENGERS_MAX,
                vehicle.passengerCapacity
            )
        )
        capabilitiesList.add(Capability(CapabilityAdapter.BAGGAGE_MAX, vehicle.luggageCapacity))

        if (capabilityTypes != null) {
            for (capability in capabilityTypes) {
                when (capability) {
                    CapabilityAdapter.GPS_TRACKING,
                    CapabilityAdapter.TRAIN_TRACKING,
                    CapabilityAdapter.FLIGHT_TRACKING -> {
                        capabilitiesList.add(Capability(capability))
                    }
                }
            }
        }

        return capabilitiesList
    }

    override fun createLoyaltyViewResponse() {
        val loyaltyId = userStore.paymentProvider?.loyalty?.id
        if (!loyaltyId.isNullOrEmpty()) {
            view?.showLoyaltyView(
                show = true,
                LoyaltyViewDataModel(
                    loyaltyId = loyaltyId,
                    tripAmount = quote?.price?.highPrice?.toDouble() ?: 0.0,
                    currency = quote?.price?.currencyCode ?: ""
                )
            )
        } else {
            view?.showLoyaltyView(show = false)
        }
    }

    private fun logBookingEvent(result: Resource<TripInfo>) {
        when (result) {
            is Resource.Success -> {
                analytics?.bookingSuccess(
                    tripId = result.data.tripId,
                    quoteId = quote?.id,
                    correlationId = result.correlationId
                )
            }
            is Resource.Failure -> {
                KarhooUISDKConfigurationProvider.configuration.paymentManager.paymentProviderView?.javaClass?.simpleName?.let {
                    analytics?.bookingFailure(
                        errorMessage = result.error.internalMessage,
                        quoteId = quote?.id,
                        correlationId = result.correlationId,
                        lastFourDigits = userStore.savedPaymentInfo?.lastFour ?: "",
                        date = Date(),
                        amount = quote?.price?.highPrice ?: 0,
                        currency = quote?.price?.currencyCode ?: "",
                        paymentMethodUsed = it
                    )
                }
            }
        }
    }

    override fun identifyTravelDetails(isPrebook: Boolean) {
        val capabilities = getCurrentQuote()?.fleet?.capabilities
        if (isPrebook) {
            if (capabilities?.contains(CapabilityAdapter.FLIGHT_TRACKING) == true && (getJourneyDetails()?.pickup?.details?.type == PoiType.AIRPORT)) {
                view?.bindTravelDetails(PoiType.AIRPORT, null)
            }
            if (capabilities?.contains(CapabilityAdapter.TRAIN_TRACKING) == true && (getJourneyDetails()?.pickup?.details?.type == PoiType.TRAIN_STATION)) {
                view?.bindTravelDetails(PoiType.TRAIN_STATION, null)
            }
        } else {
            view?.bindTravelDetails(null, null)
        }
    }

    override fun setLoyaltyNonce(nonce: String) {
        this.loyaltyNonce = nonce
    }

    companion object {
        const val TRIP_ID = "trip_id"
        const val PASSENGER_NUMBER = "PASSENGER_NUMBER"
        const val ADDITIONAL_PASSENGERS = "ADDITIONAL_PASSENGERS"
        const val LUGGAGE = "LUGGAGE"
    }
}
