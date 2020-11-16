package com.karhoo.uisdk.screen.booking.booking.bookingrequest

import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.FlightDetails
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Poi
import com.karhoo.sdk.api.model.Price
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.TripInfo
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

class BookingRequestPresenter(view: BookingRequestMVP.View,
                              private val analytics: Analytics?,
                              private val preferenceStore: PreferenceStore,
                              private val tripsService: TripsService,
                              private val userStore: UserStore)
    : BasePresenter<BookingRequestMVP.View>(), BookingRequestMVP.Presenter, LifecycleObserver {

    private var bookingStatusStateViewModel: BookingStatusStateViewModel? = null
    private var bookingRequestStateViewModel: BookingRequestStateViewModel? = null
    private var destination: LocationInfo? = null
    private var flightDetails: FlightDetails? = null
    private var origin: LocationInfo? = null
    private var outboundTripId: String? = null
    private var quote: Quote? = null
    private var scheduledDate: DateTime? = null

    init {
        attachView(view)
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
        bookTrip()
    }

    private fun bookTrip() {
        if (KarhooUISDKConfigurationProvider.isGuest()) {
            analytics?.bookingRequested(currentTripInfo(), outboundTripId)
            view?.initialiseGuestPayment(quote?.price)
        } else {
            view?.initialisePaymentProvider(quote?.price)
        }
    }

    override fun hideBookingRequest() {
        view?.animateOut()
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
            hideBookingRequest()
            view?.onTripBookedSuccessfully(tripInfo)
            bookingRequestStateViewModel?.process(BookingRequestViewContract.BookingRequestEvent
                                                          .BookingSuccess(tripInfo))
        }
    }

    private fun onTripBookFailure(error: KarhooError) {
        when (error) {
            KarhooError.CouldNotBookPaymentPreAuthFailed -> view?.showPaymentFailureDialog()
            KarhooError.InvalidRequestPayload -> handleError(R.string.booking_details_error)
            else -> handleError(returnErrorStringOrLogoutIfRequired(error))
        }
    }

    override fun clearData() {
        if (KarhooUISDKConfigurationProvider.isGuest()) {
            userStore.removeCurrentUser()
        }
    }

    override fun handleChangeCard() {
        view?.initialiseChangeCard(quote?.price)
    }

    override fun setBookingFields(allFieldsValid: Boolean) {
        if (KarhooUISDKConfigurationProvider.isGuest()) {
            view?.showGuestBookingFields()
            setBookingEnablement(allFieldsValid)
        } else {
            view?.showAuthenticatedUserBookingFields()
            setBookingEnablement(true)
        }
    }

    override fun setBookingEnablement(hasValidPaxDetails: Boolean) {
        if (!KarhooUISDKConfigurationProvider.isGuest() || (hasValidPaxDetails && userStore.savedPaymentInfo != null)) {
            view?.enableBooking()
        } else {
            view?.disableBooking()
        }
    }

    override fun passBackThreeDSecuredNonce(threeDSNonce: String, passengerDetails:
    PassengerDetails?, comments: String) {
        val passengerDetails = if (KarhooUISDKConfigurationProvider.isGuest()) passengerDetails else
            getPassengerDetails()
        passengerDetails?.let {
            tripsService.book(TripBooking(
                    nonce = threeDSNonce,
                    quoteId = quote?.id?.orEmpty(),
                    passengers = Passengers(
                            additionalPassengers = 0,
                            passengerDetails = listOf(passengerDetails)),
                    flightNumber = flightDetails?.flightNumber,
                    comments = comments))
                    .execute { result ->
                        when (result) {
                            is Resource.Success -> onTripBookSuccess(result.data)
                            is Resource.Failure -> onTripBookFailure(result.error)
                        }
                    }
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

    override fun showBookingRequest(quote: Quote, outboundTripId: String?) {
        refreshPaymentDetails()
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
            view?.setCapacity(quote.vehicleAttributes)
            view?.animateIn()
        } else if (origin == null) {
            handleError(R.string.origin_book_error)
        } else if (destination == null) {
            handleError(R.string.destination_book_error)
        }
    }

    override fun handleError(@StringRes stringId: Int) {
        view?.onError()
        bookingRequestStateViewModel?.process(BookingRequestViewContract
                                                      .BookingRequestEvent
                                                      .BookingError(stringId))
    }

    private fun handleBookingType(quote: Quote) {
        if (scheduledDate != null) {
            scheduledDate?.let {
                view?.bindPrebook(quote, "", it)
            }
        } else if (destination != null && quote.price.highPrice > 0) {
            view?.bindPriceAndEta(quote, "")
        } else {
            view?.bindEta(quote, "")
        }
    }

    override fun onPaymentFailureDialogPositive() {
        view?.hideLoading()
        handleChangeCard()
    }

    override fun onPaymentFailureDialogCancelled() {
        view?.hideLoading()
        hideBookingRequest()
    }

    override fun onTermsAndConditionsRequested(url: String?) {
        url?.let {
        bookingRequestStateViewModel?.process(BookingRequestViewContract
                                                      .BookingRequestEvent
                                                      .TermsAndConditionsRequested(it))
        }
    }
}
