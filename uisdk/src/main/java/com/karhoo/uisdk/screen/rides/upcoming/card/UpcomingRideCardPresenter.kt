package com.karhoo.uisdk.screen.rides.upcoming.card

import android.content.Context
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.base.ScheduledDateViewBinder
import com.karhoo.uisdk.util.extension.getCancellationText
import com.karhoo.uisdk.util.extension.hasValidCancellationDependingOnTripStatus

class UpcomingRideCardPresenter(view: UpcomingRideCardMVP.View,
                                private val trip: TripInfo,
                                private val scheduledDateViewBinder: ScheduledDateViewBinder,
                                private val analytics: Analytics?,
                                private val context: Context)
    : BasePresenter<UpcomingRideCardMVP.View>(), UpcomingRideCardMVP.Presenter {

    init {
        attachView(view)

        when (trip.tripState) {
            TripStatus.REQUESTED,
            TripStatus.CONFIRMED -> {
                view.hideTrackDriverButton()
            }
            TripStatus.DRIVER_EN_ROUTE,
            TripStatus.ARRIVED,
            TripStatus.PASSENGER_ON_BOARD ->
                view.displayTrackDriverButton()
        }

            checkCancellationSLAMinutes(trip, trip.serviceAgreements?.freeCancellation, context)
    }

    override fun call() {
        view?.callFleet(trip.fleetInfo?.phoneNumber.orEmpty())
    }

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

    private fun checkCancellationSLAMinutes(trip: TripInfo, serviceCancellation: ServiceCancellation?, context: Context) {
        trip.tripState?.let {
            if (serviceCancellation?.hasValidCancellationDependingOnTripStatus(it) == false) {
                view?.showCancellationText(false)
                return
            }

            var isPrebook = false

            trip.dateScheduled?.let { dateScheduled ->
                isPrebook = trip.dateScheduled != null && trip.dateBooked != null &&
                        !dateScheduled.equals(trip.dateBooked)
            }

            val text = serviceCancellation?.getCancellationText(context, isPrebook)

            if (text.isNullOrEmpty()) {
                view?.showCancellationText(false)
            } else {
                view?.setCancellationText(text)
                view?.showCancellationText(true)
            }
        }
    }
}
