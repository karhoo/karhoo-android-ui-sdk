package com.karhoo.uisdk.screen.trip.bookingstatus

import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.network.observable.Observable
import com.karhoo.sdk.api.network.observable.Observer
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.uisdk.R
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.BasePresenter

internal class BookingStatusPresenter(view: BookingStatusMVP.View, private val tripsService: TripsService,
                                      private val analytics: Analytics?)
    : BasePresenter<BookingStatusMVP.View>(), BookingStatusMVP.Presenter {

    private var tripInfo: TripInfo? = null
    private var tripState: TripStatus? = null

    private var tripInfoObservers = mutableSetOf<BookingStatusMVP.Presenter.OnTripInfoChangedListener?>()

    private var tripUpdateObserver: Observer<Resource<TripInfo>>? = null
    private var tripDetailsObservable: Observable<TripInfo>? = null

    init {
        attachView(view)
    }

    override fun monitorTrip(tripIdentifier: String) {
        if (tripIdentifier.isNotBlank()) {
            tripUpdateObserver = object : Observer<Resource<TripInfo>> {
                override fun onValueChanged(value: Resource<TripInfo>) {
                    when (value) {
                        is Resource.Success -> handleTripUpdated(value.data)
                        is Resource.Failure -> view?.showTemporaryError(value.error.userFriendlyMessage)
                    }
                }
            }

            tripDetailsObservable = tripsService.trackTrip(tripIdentifier).observable().apply {
                tripUpdateObserver?.let {
                    subscribe(it)
                }
            }
        }
    }

    override fun addTripInfoObserver(tripInfoListener: BookingStatusMVP.Presenter.OnTripInfoChangedListener?) {
        tripInfoObservers.add(tripInfoListener)
        tripInfoListener?.onTripInfoChanged(tripInfo)
    }

    private fun handleTripUpdated(tripInfo: TripInfo) {
        handleChangeOfState(tripInfo)
        updateBookingStatus(tripInfo)
        this.tripInfo = tripInfo
        notifyObservers()
    }

    override fun updateBookingStatus(tripDetails: TripInfo) {
        if (tripDetails.tripState != tripState) {
            tripState = tripDetails.tripState
            when (tripDetails.tripState) {
                TripStatus.REQUESTED -> {
                    view?.setCancelEnabled(true)
                }
                TripStatus.CONFIRMED -> {
                    view?.setCancelEnabled(true)
                }
                TripStatus.DRIVER_EN_ROUTE -> {
                    view?.updateStatus(R.string.driver_en_route, tripDetails.fleetInfo?.name.orEmpty())
                    view?.setCancelEnabled(true)
                }
                TripStatus.ARRIVED -> {
                    view?.updateStatus(R.string.arrived, tripDetails.fleetInfo?.name.orEmpty())
                    view?.setCancelEnabled(true)
                    val tripIdentifier = tripInfo?.tripId.orEmpty()
                    if (tripIdentifier.isNotBlank()) {
                        unsubscribeObservers()
                        tripDetailsObservable = tripsService.trackTrip(tripIdentifier).observable().apply {
                            tripUpdateObserver?.let {
                                subscribe(it, IN_PROGRESS_TRIP_INFO_UPDATE_PERIOD)
                            }
                        }
                    }
                }
                TripStatus.PASSENGER_ON_BOARD -> {
                    view?.updateStatus(R.string.pass_on_board, tripDetails.fleetInfo?.name.orEmpty())
                    view?.setCancelEnabled(false)
                }
                TripStatus.COMPLETED -> {
                    view?.setCancelEnabled(false)
                    view?.tripComplete(tripDetails)
                }
                TripStatus.CANCELLED_BY_USER -> {
                    view?.setCancelEnabled(false)
                    view?.tripCanceled(tripDetails)
                }
                TripStatus.CANCELLED_BY_DISPATCH -> {
                    view?.setCancelEnabled(false)
                    view?.tripCanceled(tripDetails)
                }
                else -> {
                }
            }
            if (TripStatus.tripEnded(tripState)) {
                unsubscribeObservers()
            }
        }
    }

    private fun handleChangeOfState(updatedTripInfo: TripInfo?) {
        if (!(updatedTripInfo == tripInfo || tripState == updatedTripInfo?.tripState)) {
            tripInfo = updatedTripInfo
            analytics?.tripStateChanged(tripInfo)
            if (updatedTripInfo?.tripState == TripStatus.CANCELLED_BY_DISPATCH) {
                view?.showCancellationDialog(updatedTripInfo)
            }
        }
    }

    private fun notifyObservers() {
        tripInfoObservers.map {
            it?.onTripInfoChanged(tripInfo) ?: run {
                tripInfoObservers.remove(it)
            }
        }
    }

    private fun unsubscribeObservers() {
        tripDetailsObservable?.apply {
            tripUpdateObserver?.let {
                unsubscribe(it)
            }
        }
    }

    companion object {
        const val IN_PROGRESS_TRIP_INFO_UPDATE_PERIOD = 30000L
    }
}
