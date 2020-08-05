package com.karhoo.uisdk.screen.trip.deta

import com.karhoo.sdk.api.model.DriverTrackingInfo
import com.karhoo.sdk.api.model.TripState
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.network.observable.Observable
import com.karhoo.sdk.api.network.observable.Observer
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.drivertracking.DriverTrackingService
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.BasePresenter
import java.util.Date
import java.util.TimeZone

class DetaPresenter(view: DetaMVP.View, private val driverTrackingService: DriverTrackingService,
                    private val tripsService: TripsService,
                    private val analytics: Analytics?)
    : BasePresenter<DetaMVP.View>(), DetaMVP.Presenter {

    private var driverPositionObserver: Observer<Resource<DriverTrackingInfo>>? = null
    private var tripStateObserver: Observer<Resource<TripState>>? = null
    private var driverTrackingInfoObservable: Observable<DriverTrackingInfo>? = null
    private var tripStateObservable: Observable<TripState>? = null
    private var tripState: TripStatus? = null
    private var offsetMilliseconds: Int = 0

    init {
        attachView(view)
    }

    override fun monitorDeta(tripIdentifier: String, timeZone: String) {
        if (tripIdentifier.isNotBlank()) {
            this.offsetMilliseconds = TimeZone.getTimeZone(timeZone).getOffset(Date().time)
            observeTripStatus(tripIdentifier)
            observeDriverPosition(tripIdentifier)
        }
    }

    private fun observeDriverPosition(tripIdentifier: String) {
        driverPositionObserver = object : Observer<Resource<DriverTrackingInfo>> {
            override fun onValueChanged(value: Resource<DriverTrackingInfo>) {
                when (value) {
                    is Resource.Success -> updateDeta(value.data, tripIdentifier)
                    is Resource.Failure -> view?.hideDeta()
                }
            }
        }
        driverTrackingInfoObservable = driverTrackingService.trackDriver(tripIdentifier).observable().apply {
            driverPositionObserver?.let {
                subscribe(it, REPEAT_INTERVAL)
            }
        }
    }

    private fun updateDeta(driverTrackingInfo: DriverTrackingInfo, tripId: String) {
        if (tripState == TripStatus.PASSENGER_ON_BOARD) {
            var destinationEta = driverTrackingInfo.destinationEta
            if (destinationEta < 1) {
                destinationEta = 1
            }
            view?.showDeta(destinationEta, offsetMilliseconds)
            analytics?.detaDisplayed(destinationEta, tripId)
        }
    }

    private fun observeTripStatus(tripIdentifier: String) {
        tripStateObserver = object : Observer<Resource<TripState>> {
            override fun onValueChanged(value: Resource<TripState>) {
                when (value) {
                    is Resource.Success -> handleTripState(value.data)
                }
            }
        }

        tripStateObservable = tripsService.status(tripIdentifier).observable().apply {
            tripStateObserver?.let { subscribe(it, REPEAT_INTERVAL) }
        }
    }

    private fun handleTripState(tripState: TripState) {
        this.tripState = tripState.tripState
        if (tripState.tripState != TripStatus.PASSENGER_ON_BOARD) {
            view?.hideDeta()
        }
    }

    override fun onStop() {
        unsubscribeObservers()
    }

    override fun onDestroy() {
        unsubscribeObservers()
    }

    private fun unsubscribeObservers() {
        driverPositionObserver?.let {
            driverTrackingInfoObservable?.unsubscribe(it)
        }
        tripStateObserver?.let {
            tripStateObservable?.unsubscribe(it)
        }
    }

    companion object {

        const val REPEAT_INTERVAL = 5000L

    }
}