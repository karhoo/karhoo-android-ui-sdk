package com.karhoo.uisdk.screen.trip.map

import android.location.Location
import com.google.android.gms.common.api.ResolvableApiException
import com.karhoo.sdk.api.model.DriverTrackingInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.network.observable.Observable
import com.karhoo.sdk.api.network.observable.Observer
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.drivertracking.DriverTrackingService
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.uisdk.R
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.booking.domain.userlocation.LocationProvider
import com.karhoo.uisdk.screen.booking.domain.userlocation.PositionListener

class TripMapPresenter(view: TripMapMVP.View,
                       private val driverTrackingService: DriverTrackingService,
                       private val tripsService: TripsService,
                       private val analytics: Analytics?,
                       private val locationProvider: LocationProvider)
    : BasePresenter<TripMapMVP.View>(), TripMapMVP.Presenter {

    private var tripDetailsObserver: Observer<Resource<TripInfo>>? = null
    private var tripDetailsObservable: Observable<TripInfo>? = null
    private var driverPositionObserver: Observer<Resource<DriverTrackingInfo>>? = null
    private var driverTrackingInfoObservable: Observable<DriverTrackingInfo>? = null
    private var tripState: TripStatus? = null
    private var driverLatLng: Position? = null
    private var origin: Position? = null
    private var destination: Position? = null
    private var zoomingEnabled = true

    init {
        attachView(view)
    }

    override fun onResume() {
        locationProvider.listenForLocations(object : PositionListener {
            override fun onResolutionRequired(resolvableApiException: ResolvableApiException) {
                view?.resolveApiException(resolvableApiException)
            }

            override fun onPositionUpdated(location: Location) {

            }

            override fun onLocationServicesDisabled() {
                //if disabled we just don't send analytics updates
            }
        })
    }

    override fun onPause() {
        locationProvider.stopListeningForLocations()
    }

    override fun setOrigin(origin: Position) {
        this.origin = origin
    }

    override fun setDestination(destination: Position) {
        this.destination = destination
    }

    override fun mapIsReady() {
        this.origin?.let { origin ->
            this.destination?.let { destination ->
                view?.apply {
                    addPinToMap(origin, true, R.string.kh_uisdk_address_pick_up)
                    addPinToMap(destination, false, R.string.kh_uisdk_address_drop_off)
                    zoomMapToIncludeLatLngs(1, origin, destination)
                    setUserLocationVisibility(tripState)
                }
            }
        }
    }

    override fun mapDragged() {
        zoomingEnabled = false
    }

    override fun locateMe() {
        zoomingEnabled = true
        zoomMapIfEnabled()
    }

    override fun trackDriverPosition(tripIdentifier: String) {
        if (tripIdentifier.isNotBlank()) {
            observeTripInfo(tripIdentifier)
            observeDriverPosition(tripIdentifier)
        }
    }

    private fun observeDriverPosition(tripIdentifier: String) {
        driverPositionObserver = object : Observer<Resource<DriverTrackingInfo>> {
            override fun onValueChanged(value: Resource<DriverTrackingInfo>) {
                when (value) {
                    is Resource.Success -> updateDriverPosition(value.data)
                }
            }
        }
        driverTrackingInfoObservable = driverTrackingService.trackDriver(tripIdentifier).observable()
        driverPositionObserver?.let {
            driverTrackingInfoObservable?.subscribe(it, TRIP_INFO_UPDATE_PERIOD)
        }
    }

    private fun updateDriverPosition(driverTrackingInfo: DriverTrackingInfo) {
        if (driverLatLng != driverTrackingInfo.position) {
            driverTrackingInfo.position?.let {
                driverLatLng = it
                view?.animateDriverPositionToLatLng(CAR_ANIM_DURATION, it.latitude, it.longitude)
                zoomMapIfEnabled()
            }
        }
    }

    private fun zoomMapIfEnabled() {
        if (zoomingEnabled) {
            if (tripState != null && driverLatLng != null) {
                when (tripState) {
                    TripStatus.DRIVER_EN_ROUTE,
                    TripStatus.ARRIVED -> view?.zoomMapToIncludeLatLngs(CAR_ANIM_DURATION, driverLatLng, origin)
                    TripStatus.PASSENGER_ON_BOARD -> view?.zoomMapToIncludeLatLngs(CAR_ANIM_DURATION, driverLatLng, destination)
                    else -> view?.zoomMapToIncludeLatLngs(CAR_ANIM_DURATION, origin, destination)
                }
            } else if(tripState == null && driverLatLng != null){
                view?.zoomMapToIncludeLatLngs(CAR_ANIM_DURATION, driverLatLng, origin)
            } else {
                view?.zoomMapToIncludeLatLngs(CAR_ANIM_DURATION, origin, destination)
            }
        }
    }

    private fun observeTripInfo(tripId: String) {
        tripDetailsObserver = object : Observer<Resource<TripInfo>> {
            override fun onValueChanged(value: Resource<TripInfo>) {
                when (value) {
                    is Resource.Success -> handleTripUpdated(value.data)
                }
            }
        }

        tripDetailsObservable = tripsService.trackTrip(tripId).observable().apply {
            tripDetailsObserver?.let {
                subscribe(it, TRIP_INFO_UPDATE_PERIOD)
            }
        }
    }

    private fun handleTripUpdated(tripInfo: TripInfo) {
        if (tripState != tripInfo.tripState) {
            tripState = tripInfo.tripState
            setUserLocationVisibility(tripInfo.tripState)
        }
        if (TripStatus.tripEnded(tripState)) {
            unsubscribeObservers()
        }
    }

    private fun setUserLocationVisibility(tripState: TripStatus?) {
        view?.userLocationVisible = (tripState != TripStatus.PASSENGER_ON_BOARD)
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
        tripDetailsObserver?.let {
            tripDetailsObservable?.unsubscribe(it)
        }
    }

    companion object {
        const val CAR_ANIM_DURATION = 3000
        const val TRIP_INFO_UPDATE_PERIOD = 30000L
    }
}
