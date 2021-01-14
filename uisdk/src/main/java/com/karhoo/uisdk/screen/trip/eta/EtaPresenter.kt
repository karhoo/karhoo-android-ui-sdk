package com.karhoo.uisdk.screen.trip.eta

import com.karhoo.sdk.api.model.DriverTrackingInfo
import com.karhoo.sdk.api.model.TripState
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.network.observable.Observable
import com.karhoo.sdk.api.network.observable.Observer
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.drivertracking.DriverTrackingService
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.uisdk.base.BasePresenter

class EtaPresenter(view: EtaMVP.View, private val driverTrackingService: DriverTrackingService,
                   private val tripsService: TripsService)
    : BasePresenter<EtaMVP.View>(), EtaMVP.Presenter {

    private var driverPositionObserver: Observer<Resource<DriverTrackingInfo>>? = null
    private var tripStateObserver: Observer<Resource<TripState>>? = null
    private var driverTrackingInfoObservable: Observable<DriverTrackingInfo>? = null
    private var tripStateObservable: Observable<TripState>? = null
    private var tripState: TripStatus? = null

    init {
        attachView(view)
    }

    override fun monitorEta(tripIdentifier: String) {
        if (tripIdentifier.isNotBlank()) {
            observeTripStatus(tripIdentifier)
            observeDriverPosition(tripIdentifier)
        }
    }

    private fun observeDriverPosition(tripIdentifier: String) {
        driverPositionObserver = object : Observer<Resource<DriverTrackingInfo>> {
            override fun onValueChanged(value: Resource<DriverTrackingInfo>) {
                when (value) {
                    is Resource.Success -> updateEta(value.data, tripIdentifier)
                    is Resource.Failure -> view?.hideEta()
                }
            }
        }
        driverTrackingInfoObservable = driverTrackingService.trackDriver(tripIdentifier).observable().apply {
            driverPositionObserver?.let {
                subscribe(it, REPEAT_INTERVAL)
            }
        }
    }

    private fun updateEta(driverTrackingInfo: DriverTrackingInfo, tripId: String) {
        if (tripState == TripStatus.DRIVER_EN_ROUTE) {
            val originEta = driverTrackingInfo.originEta
            view?.showEta(originEta)
        }
    }

    private fun observeTripStatus(tripIdentifier: String) {
        tripStateObserver = object : Observer<Resource<TripState>> {
            override fun onValueChanged(value: Resource<TripState>) {
                when (value) {
                    is Resource.Success -> handleTripState(value.data)
                    is Resource.Failure -> view?.hideEta()
                }
            }
        }

        tripStateObservable = tripsService.status(tripIdentifier).observable().apply {
            tripStateObserver?.let {
                subscribe(it, REPEAT_INTERVAL)
            }
        }
    }

    private fun handleTripState(tripState: TripState) {
        this.tripState = tripState.tripState
        if (tripState.tripState != TripStatus.DRIVER_EN_ROUTE) {
            view?.hideEta()
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
