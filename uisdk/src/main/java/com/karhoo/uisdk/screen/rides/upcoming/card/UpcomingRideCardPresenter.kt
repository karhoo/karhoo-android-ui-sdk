package com.karhoo.uisdk.screen.rides.upcoming.card

import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.uisdk.R
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.base.ScheduledDateViewBinder

class UpcomingRideCardPresenter(view: UpcomingRideCardMVP.View,
                                private val trip: TripInfo,
                                private val scheduledDateViewBinder: ScheduledDateViewBinder,
                                private val analytics: Analytics?)
    : BasePresenter<UpcomingRideCardMVP.View>(), UpcomingRideCardMVP.Presenter {

    init {
        attachView(view)

        when (trip.tripState) {
            TripStatus.REQUESTED,
            TripStatus.CONFIRMED ->
                view.hideTrackDriverButton()
            TripStatus.DRIVER_EN_ROUTE,
            TripStatus.ARRIVED,
            TripStatus.PASSENGER_ON_BOARD ->
                view.displayTrackDriverButton()
        }
        setCallText()
    }

    private fun setCallText() {
        if (checkDriverNumberIsAvailable()) {
            view?.callText(R.string.contact_driver)
        } else {
            view?.callText(R.string.contact_fleet)
        }
    }

    override fun call() {
        if (checkDriverNumberIsAvailable()) {
            view?.callDriver(trip.vehicle?.driver?.phoneNumber.orEmpty())
        } else {
            view?.callDriver(trip.fleetInfo?.phoneNumber.orEmpty())
        }
    }

    private fun checkDriverNumberIsAvailable() = trip.vehicle != null
            && trip.vehicle?.driver != null
            && !trip.vehicle?.driver?.phoneNumber.isNullOrEmpty()

    override fun track() {
        analytics?.trackRide()
        view?.trackTrip(trip)
    }

    override fun bindDate() {
        view?.let { scheduledDateViewBinder.bind(it, trip) }
    }

    override fun selectDetails() {
        view?.goToDetails(trip)
    }

}
