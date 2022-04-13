package com.karhoo.uisdk.screen.rides.past

import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.network.request.TripSearch
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.util.returnErrorStringOrLogoutIfRequired

internal val VALID_PAST_STATES = arrayOf(TripStatus.COMPLETED,
                                         TripStatus.NO_DRIVERS,
                                         TripStatus.CANCELLED_BY_USER,
                                         TripStatus.CANCELLED_BY_DISPATCH,
                                         TripStatus.CANCELLED_BY_KARHOO,
                                         TripStatus.INCOMPLETE)

private const val TRIP_TYPE = "BOTH"
private const val PAGE_SIZE = 10

class PastRidesPresenter(view: PastRidesMVP.View,
                         private val tripsService: TripsService)
    : BasePresenter<PastRidesMVP.View>(), PastRidesMVP.Presenter {

    init {
        attachView(view)
    }

    override fun getPastRides() {
        tripsService.search(TripSearch(tripType = TRIP_TYPE,
                                       tripState = VALID_PAST_STATES.toList(), paginationRowCount = PAGE_SIZE))
                .execute { result ->
                    when (result) {
                        is Resource.Success -> handleSuccessfulTripHistory(result.data)
                        is Resource.Failure -> view?.showError(returnErrorStringOrLogoutIfRequired(result.error), result.error)
                    }
                }
    }

    private fun handleSuccessfulTripHistory(tripList: List<TripInfo>) {
        val pastRides = tripList
                .filter { VALID_PAST_STATES.contains(it.tripState) }
                .sortedByDescending { it.dateScheduled }

        if (pastRides.isEmpty()) {
            view?.showEmptyState()
        } else {
            view?.showPastRides(pastRides)
        }
    }

}
