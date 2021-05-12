package com.karhoo.uisdk.screen.booking.address.addressbar

import android.content.Intent
import androidx.lifecycle.Observer
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.address.JourneyInfo
import org.joda.time.DateTime

interface AddressBarMVP {

    interface View {

        fun setPickupAddress(displayAddress: String)

        fun setDropoffAddress(displayAddress: String)

        fun setDefaultPickupText()

        fun showFlipButton()

        fun hideFlipButton()

        fun resetDateField()

        fun displayPrebookTime(time: DateTime)

    }

    interface Presenter {

        fun subscribeToBookingStatus(bookingStatusStateViewModel: BookingStatusStateViewModel): Observer<BookingStatus>

        fun destinationSet(destinationLocationInfo: LocationInfo, addressPositionInList: Int)

        fun pickupSet(pickupLocationInfo: LocationInfo, addressPositionInList: Int)

        fun setBothPickupDropoff(tripInfo: TripInfo?)

        fun clearDestinationClicked()

        fun dropOffAddressClicked()

        fun pickUpAddressClicked()

        fun flipAddressesClicked()

        fun prefillForJourney(journeyInfo: JourneyInfo)

    }

    interface Actions {

        fun selectAddress(intent: Intent, addressCode: Int)

    }
}
