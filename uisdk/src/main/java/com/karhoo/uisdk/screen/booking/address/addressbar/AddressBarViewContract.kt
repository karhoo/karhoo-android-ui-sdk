package com.karhoo.uisdk.screen.booking.address.addressbar

import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.base.address.AddressType
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import com.karhoo.uisdk.screen.booking.domain.address.JourneyInfo
import org.joda.time.DateTime

interface AddressBarViewContract {
    interface Widget {

        fun setPickup(address: LocationInfo, addressPositionInList: Int)

        fun setDestination(address: LocationInfo, addressPositionInList: Int)

        fun setJourneyInfo(journeyInfo: JourneyInfo?)

        fun bindTripToView(tripDetails: TripInfo?)

        fun watchBookingStatusState(lifecycleOwner: LifecycleOwner, journeyDetailsStateViewModel: JourneyDetailsStateViewModel)

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    }

    // A contract to have predefined events to update the state of the widget.
    sealed class AddressBarEvent {

        data class PickUpAddressEvent(val address: LocationInfo?) : AddressBarEvent()

        data class DestinationAddressEvent(val address: LocationInfo?) : AddressBarEvent()

        data class AsapBookingEvent(val pickup: LocationInfo?,
                                    val destination: LocationInfo?) : AddressBarEvent()

        data class PrebookBookingEvent(val pickup: LocationInfo?,
                                       val destination: LocationInfo?,
                                       val date: DateTime?) : AddressBarEvent()

        data class BookingDateEvent(val dateTime: DateTime?) : AddressBarEvent()

        object ResetBookingStatusEvent : AddressBarEvent()

        object FlipAddressesEvent : AddressBarEvent()

        data class AddressClickedEvent(val type: AddressType,
                                       val position: Position?) : AddressBarEvent()

    }

    sealed class AddressBarActions {

        data class ShowAddressActivity(val intent: Intent, val addressCode: Int) : AddressBarActions()

    }

}
