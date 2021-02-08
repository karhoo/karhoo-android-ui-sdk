package com.karhoo.uisdk.screen.booking.booking.cancellation

import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.BookingFeePrice
import com.karhoo.sdk.api.network.request.TripCancellation
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.util.CurrencyUtils

class BookingCancellationPresenter(view: BookingCancellationMVP.View,
                                   private val tripsService: TripsService = KarhooApi.tripService)
    : BasePresenter<BookingCancellationMVP.View>(), BookingCancellationMVP.Presenter {

    init {
        attachView(view)
    }

    override fun getCancellationFee(tripId: String) {
        tripsService.cancellationFee(tripId).execute { result ->
            when (result) {
                is Resource.Success -> showCancellationFee(result.data.fee)
                is Resource.Failure -> view?.showCancellationFeeError()
            }
        }
    }

    private fun showCancellationFee(bookingFeePrice: BookingFeePrice?) {
        bookingFeePrice?.let {
            view?.showCancellationFee(CurrencyUtils.getFormattedPrice(it.currency, it.value))
        } ?: view?.showCancellationFee("")
    }

    override fun handleCancellationRequest(tripId: String) {
        tripsService.cancel(tripCancellation = TripCancellation(tripId)).execute { result ->
            when (result) {
                is Resource.Success -> view?.showCancellationSuccess()
                is Resource.Failure -> view?.showCancellationError()
            }
        }
    }

}