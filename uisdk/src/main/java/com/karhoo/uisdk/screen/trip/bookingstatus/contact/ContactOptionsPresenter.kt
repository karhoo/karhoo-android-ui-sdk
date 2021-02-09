package com.karhoo.uisdk.screen.trip.bookingstatus.contact

import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.BookingFeePrice
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.network.request.TripCancellation
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.rides.detail.RideDetailMVP
import com.karhoo.uisdk.screen.trip.bookingstatus.BookingStatusMVP
import com.karhoo.uisdk.util.CurrencyUtils
import com.karhoo.uisdk.util.returnErrorStringOrLogoutIfRequired

internal class ContactOptionsPresenter(view: ContactOptionsMVP.View,
                                       private val tripsService: TripsService,
                                       private val analytics: Analytics?)
    : BasePresenter<ContactOptionsMVP.View>(), ContactOptionsMVP.Presenter, BookingStatusMVP.Presenter.OnTripInfoChangedListener,
      RideDetailMVP.Presenter.OnTripInfoChangedListener {

    private var trip: TripInfo? = null

    init {
        attachView(view)
    }

    override fun cancelTrip() {
        analytics?.userCancelTrip(trip)
        trip?.let {
            view?.showLoadingDialog(true)
            tripsService
                    .cancel(TripCancellation(tripIdentifier = if (KarhooUISDKConfigurationProvider.isGuest()) trip?.followCode.orEmpty() else trip?.tripId.orEmpty()))
                    .execute { result ->
                        when (result) {
                            is Resource.Success -> handleSuccessfulCancellation()
                            is Resource.Failure -> handleErrorWhileCancelling(result.error, it)
                        }
                    }
        }
    }

    private fun handleSuccessfulCancellation() {
        view?.apply {
            showLoadingDialog(false)
            view?.showTripCancelledDialog()
        }
    }

    private fun handleErrorWhileCancelling(karhooError: KarhooError, tripInfo: TripInfo) {
        view?.showLoadingDialog(false)
        if (tripInfo.fleetInfo == null) {
            view?.showError(returnErrorStringOrLogoutIfRequired(karhooError), karhooError)
        } else {
            view?.showCallToCancelDialog(
                    tripInfo.fleetInfo?.phoneNumber.orEmpty(),
                    tripInfo.fleetInfo?.name.orEmpty(), karhooError)
        }
    }

    override fun cancelPressed() {
        //        view?.showCancelConfirmationDialog()
        getCancellationFee()
    }

    override fun onTripInfoChanged(tripInfo: TripInfo?) {
        trip = tripInfo
        trip?.let { enableValidContactOptions(it) }
    }

    private fun enableValidContactOptions(currentTrip: TripInfo) {
        if (!currentTrip.vehicle?.driver?.phoneNumber.isNullOrBlank()
                && currentTrip.tripState != TripStatus.PASSENGER_ON_BOARD
                && currentTrip.tripState != TripStatus.COMPLETED) {
            view?.apply {
                enableCallDriver()
                disableCallFleet()
            }
        } else if (!currentTrip.fleetInfo?.phoneNumber.isNullOrBlank()) {
            view?.apply {
                enableCallFleet()
                disableCallDriver()
            }
        } else {
            view?.apply {
                disableCallFleet()
                disableCallDriver()
            }
        }
    }

    override fun contactFleet() {
        analytics?.userCalledFleet(trip)
        trip?.fleetInfo?.phoneNumber?.let { view?.makeCall(it) }
    }

    override fun contactDriver() {
        analytics?.userCalledDriver(trip)
        trip?.vehicle?.driver?.phoneNumber?.let { view?.makeCall(it) }
    }

    override fun getCancellationFee() {
        val tripIdentifier = if (KarhooUISDKConfigurationProvider.isGuest()) trip?.followCode else trip?.tripId
        tripIdentifier?.let {
            tripsService.cancellationFee(tripIdentifier).execute { result ->
                when (result) {
                    is Resource.Success -> showCancellationFee(result.data.fee, tripIdentifier)
                    is Resource.Failure -> view?.showCancellationFeeError()
                }
            }
        }
    }

    private fun showCancellationFee(bookingFeePrice: BookingFeePrice?, tripId: String) {
        bookingFeePrice?.let {
            view?.showCancellationFee(CurrencyUtils.getFormattedPrice(it.currency, it.value), tripId)
        } ?: view?.showCancellationFee("", tripId)
    }
}
