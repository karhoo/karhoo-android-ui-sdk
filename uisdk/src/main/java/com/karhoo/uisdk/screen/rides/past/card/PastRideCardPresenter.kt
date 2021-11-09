package com.karhoo.uisdk.screen.rides.past.card

import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.fare.FareService
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.base.ScheduledDateViewBinder
import com.karhoo.uisdk.util.formatted
import java.util.Currency

class PastRideCardPresenter(
        view: PastRideCardMVP.View,
        private val scheduledDateViewBinder: ScheduledDateViewBinder,
        private val trip: TripInfo,
        private val fareService: FareService = KarhooApi.fareService)
    : BasePresenter<PastRideCardMVP.View>(), PastRideCardMVP.Presenter {

    init {
        attachView(view)
    }

    override fun selectDetails() {
        view?.goToDetails(trip)
    }

    override fun bindState() {
        when (trip.tripState) {
            TripStatus.COMPLETED ->
                view?.displayState(R.drawable.uisdk_ic_trip_completed, R.string.kh_uisdk_ride_state_completed, R.color.kh_uisdk_off_black)
            TripStatus.CANCELLED_BY_USER,
            TripStatus.CANCELLED_BY_DISPATCH,
            TripStatus.NO_DRIVERS,
            TripStatus.CANCELLED_BY_KARHOO ->
                view?.displayState(R.drawable.uisdk_ic_trip_cancelled, R.string.kh_uisdk_ride_state_cancelled, R.color.kh_uisdk_off_black)
        }
    }

    override fun bindPrice() {
        fareService.fareDetails(tripId = trip.tripId).execute { fare ->
            when (fare) {
                is Resource.Success -> {
                    if (fare.data.breakdown.currency.isBlank()) {
                        view?.displayPricePending()
                    } else if (fare.data.breakdown.total != 0) {
                        fare.data.breakdown.let {
                            val currency = Currency.getInstance(it.currency)
                            val price = currency.formatted(it.total)
                            view?.displayPrice(price)
                        }
                    }
                }
                is Resource.Failure -> view?.displayPricePending()
            }
        }
    }

    override fun bindDate() {
        view?.let { scheduledDateViewBinder.bind(it, trip) }
    }
}
