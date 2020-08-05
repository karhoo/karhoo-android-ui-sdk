package com.karhoo.uisdk.screen.booking.address.addressbar

import androidx.lifecycle.Observer
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.address.AddressService
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.base.address.AddressType
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.address.JourneyInfo
import com.karhoo.uisdk.util.extension.toSimpleLocationInfo
import org.joda.time.DateTime

internal class AddressBarPresenter(view: AddressBarMVP.View,
                                   private val analytics: Analytics?,
                                   private val addressService: AddressService = KarhooApi.addressService)
    : BasePresenter<AddressBarMVP.View>(), AddressBarMVP.Presenter {

    private var bookingStatusStateViewModel: BookingStatusStateViewModel? = null

    init {
        attachView(view)
    }

    override fun pickUpAddressClicked() {
        val latLong: Position? = bookingStatusStateViewModel?.currentState?.pickup?.position
        bookingStatusStateViewModel?.process(AddressBarViewContract.AddressBarEvent
                                                     .AddressClickedEvent(AddressType.PICKUP, latLong))
    }

    override fun dropOffAddressClicked() {
        analytics?.destinationPressed()
        val latLong: Position? = bookingStatusStateViewModel?.currentState?.destination?.position

        bookingStatusStateViewModel?.process(AddressBarViewContract.AddressBarEvent
                                                     .AddressClickedEvent(AddressType.DESTINATION, latLong))
    }

    override fun flipAddressesClicked() {
        bookingStatusStateViewModel?.process(AddressBarViewContract.AddressBarEvent.FlipAddressesEvent)
    }

    override fun pickupSet(pickupLocationInfo: LocationInfo, addressPositionInList: Int) {
        analytics?.pickupAddressSelected(pickupLocationInfo, addressPositionInList)
        view?.setPickupAddress(pickupLocationInfo.displayAddress)
        bookingStatusStateViewModel?.process(AddressBarViewContract.AddressBarEvent
                                                     .PickUpAddressEvent(pickupLocationInfo))
    }

    override fun destinationSet(destinationLocationInfo: LocationInfo, addressPositionInList: Int) {
        analytics?.destinationAddressSelected(destinationLocationInfo, addressPositionInList)
        view?.setDropoffAddress(destinationLocationInfo.displayAddress)
        view?.showFlipButton()
        bookingStatusStateViewModel?.process(AddressBarViewContract.AddressBarEvent
                                                     .DestinationAddressEvent(destinationLocationInfo))
    }

    private fun dateSet(date: DateTime) {
        bookingStatusStateViewModel?.process(AddressBarViewContract.AddressBarEvent.BookingDateEvent(date))
        if (!scheduledDateInvalid()) {
            view?.displayPrebookTime(date)
        }
    }

    override fun setBothPickupDropoff(tripInfo: TripInfo?) {
        tripInfo?.let { trip ->
            view?.setPickupAddress(trip.origin?.displayAddress.orEmpty())
            view?.setDropoffAddress(tripInfo.destination?.displayAddress.orEmpty())
            view?.showFlipButton()
            bookingStatusStateViewModel?.process(AddressBarViewContract.AddressBarEvent
                                                         .PrebookBookingEvent(trip.origin?.toSimpleLocationInfo()
                                                                              , trip.destination?.toSimpleLocationInfo()
                                                                              , bookingStatusStateViewModel?.currentState?.date))
        }
    }

    override fun clearDestinationClicked() {
        clearDestinationInView()
        bookingStatusStateViewModel?.process(AddressBarViewContract.AddressBarEvent
                                                     .DestinationAddressEvent(null))
    }

    private fun clearDestinationInView() {
        view?.setDropoffAddress("")
        if (scheduledDateInvalid()) {
            view?.resetDateField()
        }
        view?.hideFlipButton()
    }

    private fun scheduledDateInvalid(): Boolean {
        bookingStatusStateViewModel?.currentState?.date?.let {
            val currentRemaining = it.millis - System.currentTimeMillis()
            return if (currentRemaining < ONE_HOUR_MILLIS) {
                bookingStatusStateViewModel?.process(AddressBarViewContract.AddressBarEvent
                                                             .BookingDateEvent(null))
                true
            } else {
                false
            }
        } ?: run {
            return false
        }
    }

    override fun prefillForJourney(journeyInfo: JourneyInfo) {
        journeyInfo.origin?.let {
            addressService.reverseGeocode(it).execute { result ->
                reverseGeocodeDestination(journeyInfo.destination)
                when (result) {
                    is Resource.Success -> pickupSet(pickupLocationInfo = result.data, addressPositionInList = 0)
                }
            }
        } ?: run {
            reverseGeocodeDestination(journeyInfo.destination)
        }
        journeyInfo.date?.let {
            dateSet(it)
        }
    }

    private fun reverseGeocodeDestination(destination: Position?) {
        destination?.let {
            addressService.reverseGeocode(it).execute { result ->
                when (result) {
                    is Resource.Success -> destinationSet(destinationLocationInfo = result.data, addressPositionInList
                    = 0)
                }
            }
        }
    }

    override fun subscribeToBookingStatus(bookingStatusStateViewModel: BookingStatusStateViewModel): Observer<BookingStatus> {
        setCurrentBookingStatus(bookingStatusStateViewModel)
        return Observer { bookingStatus ->
            bookingStatus?.let {
                it.pickup?.let { pickup ->
                    view?.setPickupAddress(pickup.displayAddress)
                } ?: run {
                    view?.setDefaultPickupText()
                }

                it.destination?.let { dropoff ->
                    view?.setDropoffAddress(dropoff.displayAddress)
                } ?: run {
                    clearDestinationInView()
                }

            }
        }
    }

    private fun setCurrentBookingStatus(bookingStatusStateViewModel: BookingStatusStateViewModel) {
        this.bookingStatusStateViewModel = bookingStatusStateViewModel
    }

    companion object {

        const val ONE_HOUR_MILLIS = 60000L * 60L

    }
}
