package com.karhoo.uisdk.screen.rides.upcoming

import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.network.request.TripSearch
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.util.returnErrorStringOrLogoutIfRequired

class UpcomingRidesPresenter(view: UpcomingRidesMVP.View,
                             private val tripsService: TripsService)
    : BasePresenter<UpcomingRidesMVP.View>(), UpcomingRidesMVP.Presenter {

    init {
        attachView(view)
    }

    override fun getUpcomingRides() {
        tripsService.search(TripSearch(tripType = TRIP_TYPE,
                                       tripState = VALID_UPCOMING_STATES.toList()))
                .execute { result ->
                    when (result) {
                        is Resource.Success -> handleSuccessfulTripHistory(result.data)
                        is Resource.Failure -> view?.showError(returnErrorStringOrLogoutIfRequired(result.error), result.error)
                    }
                }
    }

    private fun handleSuccessfulTripHistory(tripList: List<TripInfo>) {
        val upcomingTrips = tripList
                .filter { VALID_UPCOMING_STATES.contains(it.tripState) }
                .sortedWith(UpcomingRidesSort())

        if (upcomingTrips.isEmpty()) {
            view?.showEmptyState()
        } else {
            view?.showUpcomingRides(upcomingTrips)
        }
    }

    companion object {
        private val VALID_UPCOMING_STATES = arrayOf(
                TripStatus.REQUESTED,
                TripStatus.CONFIRMED,
                TripStatus.DRIVER_EN_ROUTE,
                TripStatus.ARRIVED,
                TripStatus.PASSENGER_ON_BOARD)
        private const val TRIP_TYPE = "BOTH"
    }

}
