package com.karhoo.uisdk.screen.trip.bookingstatus.tripinfo

import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.trip.bookingstatus.BookingStatusMVP

internal class TripInfoPresenter(view: TripInfoMVP.View)
    : BasePresenter<TripInfoMVP.View>(), TripInfoMVP.Presenter, BookingStatusMVP.Presenter.OnTripInfoChangedListener {

    init {
        attachView(view)
    }

    override fun observeTripStatus(bookingStatusPresenter: BookingStatusMVP.Presenter) {
        bookingStatusPresenter.addTripInfoObserver(this)
    }

    override fun onTripInfoChanged(tripInfo: TripInfo?) {
        tripInfo?.let {
            view?.bindViews(it.vehicle?.driver?.firstName.orEmpty(),
                            it.vehicle?.description.orEmpty(),
                            it.vehicle?.vehicleLicencePlate.orEmpty(),
                            it.vehicle?.driver?.licenceNumber.orEmpty(),
                            it.vehicle?.driver?.photoUrl.orEmpty())
            if (canShowDriverDetails(tripInfo)) {
                view?.showDriverDetails()
                view?.showTripInfo()
            }
            enableDetails(it.tripState)
        }
    }

    private fun enableDetails(tripState: TripStatus?) {
        tripState?.let {
            when (it) {
                TripStatus.COMPLETED -> view?.hideDetailsOptions()
                else -> view?.showDetailsOptions()
            }
        }
    }

    private fun canShowDriverDetails(tripDetails: TripInfo): Boolean {
        return (tripDetails.vehicle != null)
    }
}
