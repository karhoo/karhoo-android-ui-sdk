package com.karhoo.uisdk.screen.booking.domain.address

import android.app.Application
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.uisdk.base.address.AddressCodes
import com.karhoo.uisdk.base.address.AddressType
import com.karhoo.uisdk.base.state.BaseStateViewModel
import com.karhoo.uisdk.screen.address.AddressActivity
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarViewContract
import org.joda.time.DateTime

class JourneyDetailsStateViewModel(application: Application) : BaseStateViewModel<JourneyDetails,
        AddressBarViewContract.AddressBarActions, AddressBarViewContract.AddressBarEvent>(application) {

    init {
        viewState = JourneyDetails(null, null, null)
    }

    // update the state by using a set of predefined contracts. Some of the event can trigger an
    // action to be performed (e.g. output of the widget)
    override fun process(viewEvent: AddressBarViewContract.AddressBarEvent) {
        super.process(viewEvent)
        when (viewEvent) {
            is AddressBarViewContract.AddressBarEvent.PickUpAddressEvent -> updatePickup(viewEvent.address)
            is AddressBarViewContract.AddressBarEvent.DestinationAddressEvent -> updateDestination(viewEvent.address)
            is AddressBarViewContract.AddressBarEvent.AsapBookingEvent -> updatePickupDestinationDate(viewEvent.pickup, viewEvent.destination, null)
            is AddressBarViewContract.AddressBarEvent.PrebookBookingEvent -> updatePickupDestinationDate(viewEvent.pickup, viewEvent.destination, viewEvent.date)
            is AddressBarViewContract.AddressBarEvent.BookingDateEvent -> updateDate(viewEvent.dateTime)
            is AddressBarViewContract.AddressBarEvent.ResetJourneyDetailsEvent -> resetJourneyDetails()
            is AddressBarViewContract.AddressBarEvent.FlipAddressesEvent -> flipAddresses()
            is AddressBarViewContract.AddressBarEvent.AddressClickedEvent -> showAddressActivity(viewEvent.type, viewEvent.position)
        }
    }

    private fun updatePickupDestinationDate(pickup: LocationInfo?, destination: LocationInfo?, date: DateTime?) {
        viewState = JourneyDetails(pickup, destination, date)
    }

    private fun showAddressActivity(addressType: AddressType, position: Position?) {
        val addressBuilder = AddressActivity.Builder.builder
                .addressType(addressType)
        position?.let {
            addressBuilder.locationBias(it.latitude, it.longitude)
        }

        val intent = addressBuilder.build(getApplication())

        viewAction = when (addressType) {
            AddressType.PICKUP -> AddressBarViewContract.AddressBarActions.ShowAddressActivity(intent, AddressCodes.PICKUP)
            AddressType.DESTINATION -> AddressBarViewContract.AddressBarActions.ShowAddressActivity(intent, AddressCodes.DESTINATION)
        }
    }

    private fun updatePickup(pickup: LocationInfo?) {
        viewState = JourneyDetails(pickup, viewState.destination, viewState.date)
    }

    private fun updateDestination(destination: LocationInfo?) {
        viewState = JourneyDetails(viewState.pickup, destination, viewState.date)
    }

    private fun updateDate(date: DateTime?) {
        viewState = JourneyDetails(viewState.pickup, viewState.destination, date)
    }

    private fun flipAddresses() {
        viewState = JourneyDetails(viewState.destination, viewState.pickup, viewState.date)
    }

    private fun resetJourneyDetails() {
        viewState = JourneyDetails(null, null, null)
    }
}
