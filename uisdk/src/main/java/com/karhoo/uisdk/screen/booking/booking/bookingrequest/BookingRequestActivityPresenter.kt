package com.karhoo.uisdk.screen.booking.booking.bookingrequest

import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.sdk.api.model.FlightDetails
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Poi
import com.karhoo.sdk.api.model.Price
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.network.request.Luggage
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.sdk.api.network.request.Passengers
import com.karhoo.sdk.api.network.request.TripBooking
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarViewContract
import com.karhoo.uisdk.screen.booking.booking.payment.ProviderType
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStatus
import com.karhoo.uisdk.service.preference.PreferenceStore
import com.karhoo.uisdk.util.extension.orZero
import com.karhoo.uisdk.util.extension.toTripLocationDetails
import com.karhoo.uisdk.util.returnErrorStringOrLogoutIfRequired
import org.joda.time.DateTime
import java.util.Date

class BookingRequestActivityPresenter(view: BookingRequestContract.View,
                                      private val analytics: Analytics?,
                                      private val preferenceStore: PreferenceStore,
                                      private val tripsService: TripsService,
                                      private val userStore: UserStore)
    : BasePresenter<BookingRequestContract.View>(), BookingRequestContract.Presenter, LifecycleObserver {

    private var bookingStatusStateViewModel: BookingStatusStateViewModel? = null
    private var bookingRequestStateViewModel: BookingRequestStateViewModel? = null
    private var destination: LocationInfo? = null
    private var flightDetails: FlightDetails? = null
    private var origin: LocationInfo? = null
    private var outboundTripId: String? = null
    private var quote: Quote? = null
    private var scheduledDate: DateTime? = null
    private var bookingMetadata: HashMap<String, String>? = null

    init {
        attachView(view)
    }

    override fun setBookingStatus(bookingStatus: BookingStatus?) {
        bookingStatus?.let {
            scheduledDate = it.date
            destination = it.destination
            origin = it.pickup
        }
    }
    override fun watchBookingStatus(bookingStatusStateViewModel: BookingStatusStateViewModel): Observer<in BookingStatus> {
        this.bookingStatusStateViewModel = bookingStatusStateViewModel
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
        analytics?.bookingRequested(currentTripInfo(), outboundTripId)
    }

    override fun isPaymentSet(): Boolean {
        return userStore.savedPaymentInfo != null
    }

    private fun currentTripInfo(): TripInfo {
        return TripInfo(
                origin = origin?.toTripLocationDetails(),
                destination = destination?.toTripLocationDetails(),
                dateScheduled = Date(scheduledDate?.millis.orZero()),
                quote = Price(total = quote?.price?.highPrice.orZero()))
    }

    private fun onTripBookSuccess(tripInfo: TripInfo) {
        preferenceStore.lastTrip = tripInfo
        val date = scheduledDate
        if (date != null) {
            view?.showPrebookConfirmationDialog(quote?.quoteType, tripInfo)
        } else {
            view?.onTripBookedSuccessfully(tripInfo)
            bookingRequestStateViewModel?.process(BookingRequestViewContract.BookingRequestEvent
                                                          .BookingSuccess(tripInfo))
        }
    }

    private fun onTripBookFailure(error: KarhooError) {
        when (error) {
            KarhooError.CouldNotBookPaymentPreAuthFailed -> view?.showPaymentFailureDialog(error)
            KarhooError.InvalidRequestPayload -> handleError(R.string.kh_uisdk_booking_details_error, error)
            else -> handleError(returnErrorStringOrLogoutIfRequired(error), error)
        }
    }

    override fun clearData() {
        if (KarhooUISDKConfigurationProvider.isGuest()) {
            userStore.removeCurrentUser()
        }
        if (ProviderType.ADYEN.name.equals(userStore.paymentProvider?.id, ignoreCase = true)) {
            userStore.clearSavedPaymentInfo()
        }
    }

    override fun handleChangeCard() {
        view?.initialiseChangeCard(quote)
    }

    override fun setBookingFields(allFieldsValid: Boolean) {

        when (KarhooUISDKConfigurationProvider.configuration.authenticationMethod()) {
            is AuthenticationMethod.Guest -> {
                view?.showGuestBookingFields(null)
            }
            is AuthenticationMethod.TokenExchange -> {
                view?.showGuestBookingFields(details = getPassengerDetails())
            }
            else -> {
                view?.showAuthenticatedUserBookingFields()
            }
        }
    }

    override fun passBackPaymentIdentifiers(identifier: String, tripId: String?, passengerDetails: PassengerDetails?, comments: String) {
        val passenger = if (KarhooUISDKConfigurationProvider.configuration
                        .authenticationMethod() is AuthenticationMethod.KarhooUser) getPassengerDetails() else passengerDetails

        passenger?.let {
            val metadata = getBookingMetadataMap(identifier, tripId)

            tripsService.book(TripBooking(
                    comments = comments,
                    flightNumber = flightDetails?.flightNumber,
                    meta = metadata,
                    nonce = identifier,
                    quoteId = quote?.id.orEmpty(),
                    passengers = Passengers(
                            additionalPassengers = 0,
                            passengerDetails = listOf(passenger),
                            luggage = Luggage(total = 0))))
                    .execute { result ->
                        when (result) {
                            is Resource.Success -> onTripBookSuccess(result.data)
                            is Resource.Failure -> onTripBookFailure(result.error)
                        }
                    }
        }
    }

    private fun getBookingMetadataMap(identifier: String, tripId: String?): HashMap<String, String>? {
        return bookingMetadata?.let { bookingData ->
            tripId?.let { bookingData[TRIP_ID] = identifier }
            bookingData
        } ?: run {
            tripId?.let { hashMapOf(TRIP_ID to identifier) }
        }
    }

    private fun getPassengerDetails(): PassengerDetails {
        val user = userStore.currentUser
        return PassengerDetails(
                firstName = user.firstName,
                lastName = user.lastName,
                phoneNumber = user.phoneNumber,
                email = user.email,
                locale = user.locale)
    }

    override fun resetBooking() {
        bookingStatusStateViewModel?.process(AddressBarViewContract.AddressBarEvent.ResetBookingStatusEvent)
    }

    private fun refreshPaymentDetails() {
        view?.showUpdatedPaymentDetails(userStore.savedPaymentInfo)
    }

    override fun showBookingRequest(quote: Quote, outboundTripId: String?, bookingMetadata:
    HashMap<String, String>?) {
        refreshPaymentDetails()
        this.bookingMetadata = bookingMetadata
        if (origin != null && destination != null) {
            this.quote = quote
            this.outboundTripId = outboundTripId
            handleBookingType(quote)
            when (origin?.poiType) {
                Poi.ENRICHED -> {
                    view?.displayFlightDetailsField(origin?.details?.type)
                }
                else -> view?.displayFlightDetailsField(null)
            }
            view?.setCapacity(quote.vehicle)
        } else if (origin == null) {
            handleError(R.string.kh_uisdk_origin_book_error, null)
        } else if (destination == null) {
            handleError(R.string.kh_uisdk_destination_book_error, null)
        }
    }

    override fun handleError(@StringRes stringId: Int, karhooError: KarhooError?) {
        view?.onError()
        view?.enableCancelButton()
        bookingRequestStateViewModel?.process(BookingRequestViewContract
                                                      .BookingRequestEvent
                                                      .BookingError(stringId, karhooError))
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

    companion object {
        const val TRIP_ID = "trip_id"
    }
}
