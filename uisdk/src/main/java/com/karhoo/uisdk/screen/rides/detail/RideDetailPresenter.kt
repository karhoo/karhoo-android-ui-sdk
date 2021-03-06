package com.karhoo.uisdk.screen.rides.detail

import android.content.Context
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.Price
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.sdk.api.network.observable.Observable
import com.karhoo.sdk.api.network.observable.Observer
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.fare.FareService
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.base.ScheduledDateViewBinder
import com.karhoo.uisdk.screen.rides.feedback.FeedbackCompletedTripsStore
import com.karhoo.uisdk.util.extension.classToLocalisedString
import com.karhoo.uisdk.util.extension.getCancellationText
import com.karhoo.uisdk.util.extension.hasValidCancellationDependingOnTripStatus
import com.karhoo.uisdk.util.formatted
import java.util.Currency

private val LIVE_STATES = arrayOf(
        TripStatus.REQUESTED,
        TripStatus.CONFIRMED,
        TripStatus.DRIVER_EN_ROUTE,
        TripStatus.ARRIVED,
        TripStatus.PASSENGER_ON_BOARD,
        TripStatus.INCOMPLETE)

@Suppress("LongParameterList")
class RideDetailPresenter(view: RideDetailMVP.View,
                          private var trip: TripInfo,
                          private val tripsService: TripsService,
                          private val scheduledDateBinder: ScheduledDateViewBinder,
                          private val feedbackCompletedTripsStore: FeedbackCompletedTripsStore,
                          private val fareService: FareService = KarhooApi.fareService)
    : BasePresenter<RideDetailMVP.View>(), RideDetailMVP.Presenter {

    private var tripDetailsObserver: Observer<Resource<TripInfo>>? = null
    private var tripDetailsObservable: Observable<TripInfo>? = null
    private var tripInfoObservers = mutableSetOf<RideDetailMVP.Presenter.OnTripInfoChangedListener?>()

    init {
        attachView(view)
    }

    private fun bindAll() {
        bindState()
        bindPrice()
        bindButtons()
        bindVehicle()
        bindFlightDetails()
        bindDate()
        bindComments()
    }

    override fun bindFlightDetails() {
        if (trip.flightNumber.isNullOrEmpty()) {
            view?.hideFlightDetails()
        } else {
            view?.displayFlightDetails(trip.flightNumber.orEmpty(), "")
        }
    }

    override fun bindComments() {
        with(trip.comments.orEmpty()) {
            if (this.isEmpty()) {
                view?.hideComments()
            } else {
                view?.displayComments(this)
            }
        }
    }

    override fun bindState() {
        when (trip.tripState) {
            TripStatus.REQUESTED -> view?.displayState(R.drawable.uisdk_blank, R.string.kh_uisdk_ride_state_requested, R.color.off_black)
            TripStatus.CONFIRMED -> view?.displayState(R.drawable.uisdk_blank, R.string.kh_uisdk_ride_state_confirmed, R.color.off_black)
            TripStatus.DRIVER_EN_ROUTE -> view?.displayState(R.drawable.uisdk_blank, R.string.kh_uisdk_ride_state_der, R.color.off_black)
            TripStatus.ARRIVED -> view?.displayState(R.drawable.uisdk_blank, R.string.kh_uisdk_ride_state_arrived, R.color.off_black)
            TripStatus.PASSENGER_ON_BOARD -> view?.displayState(R.drawable.uisdk_blank, R.string.kh_uisdk_ride_state_pob, R.color.off_black)
            TripStatus.COMPLETED -> view?.displayState(R.drawable.uisdk_ic_trip_completed, R.string.kh_uisdk_ride_state_completed, R.color.off_black)
            TripStatus.INCOMPLETE -> view?.displayState(R.drawable.uisdk_blank, R.string.kh_uisdk_pending, R.color.off_black)
            TripStatus.CANCELLED_BY_USER, TripStatus.CANCELLED_BY_DISPATCH, TripStatus.NO_DRIVERS, TripStatus.CANCELLED_BY_KARHOO ->
                view?.displayState(R.drawable.uisdk_ic_trip_cancelled, R.string.kh_uisdk_ride_state_cancelled, R.color.off_black)
        }
    }

    override fun bindPrice() {
        getQuoteFromTrip()?.let {
            validatePrice(it)
        } ?: run {
            view?.displayPricePending()
        }
    }

    private fun validatePrice(price: Price): Unit? {
        return if (isValidPrice(price)) {
            updatePrice(price)
        } else {
            view?.displayPricePending()
        }
    }

    private fun updatePrice(price: Price) {
        val displayPrice = getPriceFromTrip(price)
        if (price.quoteType == QuoteType.METERED || price.quoteType == QuoteType.ESTIMATED) {
            view?.displayBasePrice(displayPrice)
        } else {
            view?.displayPrice(displayPrice)
        }
    }

    private fun getQuoteFromTrip(): Price? {
        return if (LIVE_STATES.contains(trip.tripState)) {
            trip.quote
        } else {
            fetchFare(trip.tripId)
            return null
        }
    }

    private fun fetchFare(tripId: String) {
        fareService.fareDetails(tripId).execute {
            when (it) {
                is Resource.Success -> validatePrice(Price(
                        quoteType = trip.quote?.quoteType ?: QuoteType.FIXED,
                        total = it.data.breakdown.total,
                        currency = it.data.breakdown.currency))
                is Resource.Failure -> view?.displayPricePending()
            }
        }
    }

    private fun isValidPrice(price: Price): Boolean {
        return !price.currency.isNullOrEmpty()
    }

    private fun getPriceFromTrip(price: Price): String {
        val currency = Currency.getInstance(price.currency)
        return currency.formatted(price.total)
    }

    override fun bindButtons() {
        if (LIVE_STATES.contains(trip.tripState)) {
            view?.apply {
                hideRebookButton()
                hideReportIssueButton()
                displayContactOptions()
            }
        } else {
            view?.apply {
                hideContactOptions()
                displayReportIssueButton()
                if (!trip.origin?.placeId.isNullOrEmpty()
                        && !trip.destination?.placeId.isNullOrEmpty()) {
                    displayRebookButton()
                }
            }
        }
    }

    override fun bindVehicle() {
        if (!trip.vehicle?.vehicleLicencePlate.isNullOrBlank()) {
            view?.displayVehicle("${trip.vehicle?.classToLocalisedString()}${trip.vehicle?.vehicleLicencePlate}")
        }
    }

    override fun baseFarePressed() {
        view?.displayBaseFareDialog()
    }

    private fun observeTripInfo(tripIdentifier: String) {
        tripDetailsObserver = object : Observer<Resource<TripInfo>> {
            override fun onValueChanged(value: Resource<TripInfo>) {
                when (value) {
                    is Resource.Success -> handleTripUpdate(value.data)
                }
            }
        }

        if (LIVE_STATES.contains(trip.tripState)) {
            tripDetailsObservable = tripsService.trackTrip(tripIdentifier).observable().apply {
                tripDetailsObserver?.let {
                    subscribe(it, TRIP_INFO_UPDATE_PERIOD)
                }
            }
        }
    }

    private fun handleTripUpdate(tripInfo: TripInfo) {
        this.trip = tripInfo
        bindAll()
        notifyObservers()
    }

    override fun bindDate() {
        view?.let { scheduledDateBinder.bind(it, trip) }
    }

    override fun onResume() {
        val tripIdentifier = if (KarhooUISDKConfigurationProvider.isGuest()) trip.followCode else trip.tripId
        tripIdentifier?.let {
            observeTripInfo(it)
        }
        if (feedbackCompletedTripsStore.contains(trip.tripId)) {
            view?.showFeedbackSubmitted()
        }
    }

    override fun onPause() {
        unsubscribeObservers()
    }

    override fun checkCancellationSLA(context: Context, trip: TripInfo, serviceCancellation: ServiceCancellation?) {
        trip.tripState?.let {
            if(serviceCancellation?.hasValidCancellationDependingOnTripStatus(it) == false) {
                return
            }

            var isPrebook = false

            trip.dateScheduled?.let { dateScheduled ->
                isPrebook = trip.dateScheduled != null && trip.dateBooked != null &&
                        !dateScheduled.equals(trip.dateBooked)
            }

            val cancellationText = serviceCancellation?.getCancellationText(context, isPrebook)

            if (cancellationText.isNullOrEmpty()) {
                view?.showCancellationText(false)
            } else {
                view?.setCancellationText(cancellationText)
                view?.showCancellationText(true)
            }
        }
    }

    override fun addTripInfoObserver(tripInfoListener: RideDetailMVP.Presenter.OnTripInfoChangedListener?) {
        tripInfoObservers.add(tripInfoListener)
        tripInfoListener?.onTripInfoChanged(trip)
    }

    private fun notifyObservers() {
        tripInfoObservers.map {
            it?.onTripInfoChanged(trip) ?: run {
                tripInfoObservers.remove(it)
            }
        }
    }

    private fun unsubscribeObservers() {
        tripDetailsObservable?.apply {
            tripDetailsObserver?.let {
                unsubscribe(it)
            }
        }
    }

    companion object {
        const val TRIP_INFO_UPDATE_PERIOD = 15000L
    }
}
