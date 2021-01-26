package com.karhoo.uisdk.screen.booking.booking.tripallocation

import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.network.observable.Observable
import com.karhoo.sdk.api.network.observable.Observer
import com.karhoo.sdk.api.network.request.TripCancellation
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.util.extension.isGuest

class TripAllocationPresenter(view: TripAllocationMVP.View,
                              private val tripsService: TripsService)
    : BasePresenter<TripAllocationMVP.View>(), TripAllocationMVP.Presenter {

    private var tripDetailsObserver: Observer<Resource<TripInfo>>? = null
    private var tripDetailsObservable: Observable<TripInfo>? = null
    private var trip: TripInfo? = null
    private var showAllocationAlert: Boolean = false

    init {
        attachView(view)
    }

    override fun cancelTrip() {
        showAllocationAlert = false
        val tripIdentifier = if (KarhooUISDKConfigurationProvider.isGuest()) trip?.followCode else trip?.tripId
        tripIdentifier?.let {
            tripsService
                    .cancel(TripCancellation(tripIdentifier = it))
                    .execute { result ->
                        when (result) {
                            is Resource.Failure -> handleFailedCancellation()
                        }
                    }
        }
    }

    override fun handleAllocationDelay(trip: TripInfo) {
        if(!KarhooUISDKConfigurationProvider.isGuest() && showAllocationAlert) {
            view?.showAllocationDelayAlert(trip)
        }
    }

    override fun waitForAllocation(trip: TripInfo) {
        showAllocationAlert = true
        this.trip = trip
        checkForAllocationOrCancellation(trip)
        val tripIdentifier = if (KarhooUISDKConfigurationProvider.isGuest()) trip.followCode else trip.tripId
        tripIdentifier?.let { observeTripInfo(it) }
    }

    private fun handleFailedCancellation() {
        val number = trip?.fleetInfo?.phoneNumber.orEmpty()
        val fleet = trip?.fleetInfo?.name.orEmpty()
        view?.showCallToCancelDialog(number, fleet)
    }

    private fun checkForAllocationOrCancellation(trip: TripInfo) {
        when (trip.tripState) {
            TripStatus.DRIVER_EN_ROUTE,
            TripStatus.ARRIVED,
            TripStatus.PASSENGER_ON_BOARD,
            TripStatus.COMPLETED -> tripAllocated(trip)
            TripStatus.CANCELLED_BY_USER -> tripCancelledByUser()
            TripStatus.CANCELLED_BY_DISPATCH,
            TripStatus.CANCELLED_BY_KARHOO -> tripCancelled(trip)
        }
    }

    private fun tripAllocated(trip: TripInfo) {
        showAllocationAlert = false
        unsubscribeFromUpdates()
        if (isGuest()) {
            trip.followCode?.let { view?.displayWebTracking(it) } ?: view?.goToTrip(trip)
        } else {
            view?.goToTrip(trip)
        }
    }

    private fun tripCancelled(trip: TripInfo) {
        showAllocationAlert = false
        unsubscribeFromUpdates()
        view?.displayBookingFailed(trip.fleetInfo?.name.orEmpty())
    }

    private fun tripCancelledByUser() {
        showAllocationAlert = false
        unsubscribeFromUpdates()
        view?.displayTripCancelledSuccess()
    }

    override fun unsubscribeFromUpdates() {
        tripDetailsObservable?.apply {
            tripDetailsObserver?.let {
                unsubscribe(it)
            }
        }
    }

    private fun observeTripInfo(tripIdentifier: String) {
        tripDetailsObserver = object : Observer<Resource<TripInfo>> {
            override fun onValueChanged(value: Resource<TripInfo>) {
                when (value) {
                    is Resource.Success -> {
                        if (KarhooUISDKConfigurationProvider.isGuest()) {
                            value.data.followCode = tripIdentifier
                        }
                        checkForAllocationOrCancellation(value.data)
                    }
                }
            }
        }

        tripDetailsObservable = tripsService.trackTrip(tripIdentifier).observable().apply {
            tripDetailsObserver?.let {
                subscribe(it)
            }
        }
    }
}
