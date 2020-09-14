package com.karhoo.uisdk.screen.booking.booking.bookingrequest

import androidx.lifecycle.Observer
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.PoiType
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.QuoteV2
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.VehicleAttributes
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentActions
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStatus
import org.joda.time.DateTime

interface GuestBookingMVP {
    interface View {

        fun animateIn()

        fun animateOut()

        fun bindEta(quote: QuoteV2, card: String)

        fun bindPrebook(quote: QuoteV2, card: String, date: DateTime)

        fun bindPriceAndEta(quote: QuoteV2, card: String)

        fun disableBooking()

        fun displayFlightDetailsField(poiType: PoiType?)

        fun enableBooking()

        fun initialiseChangeCard(price: QuotePrice?)

        fun initialiseGuestPayment(price: QuotePrice?)

        fun initialisePaymentProvider(price: QuotePrice?)

        fun onError()

        fun onTripBookedSuccessfully(tripInfo: TripInfo)

        fun populateFlightDetailsField(flightNumber: String?)

        fun setCapacity(vehicleAttributes: VehicleAttributes)

        fun showGuestBookingFields()

        fun showAuthenticatedUserBookingFields()

        fun showPaymentFailureDialog()

        fun showPaymentUI()

        fun showPrebookConfirmationDialog(quoteType: QuoteType?, tripInfo: TripInfo)

        fun showUpdatedPaymentDetails(savedPaymentInfo: SavedPaymentInfo?, quotePrice: QuotePrice?)

    }

    interface Presenter {

        fun clearData()

        fun handleChangeCard()

        fun handleError(stringId: Int)

        fun hideBookingRequest()

        fun makeBooking()

        fun passBackThreeDSecuredNonce(threeDSNonce: String, passengerDetails: PassengerDetails?
        = null, comments: String)

        fun setBookingEnablement(hasValidPaxDetails: Boolean)

        fun setBookingFields(allFieldsValid: Boolean)

        fun showBookingRequest(quote: QuoteV2, outboundTripId: String? = null)

        fun resetBooking()

        fun updateCardDetails(braintreeSDKNonce: String?)

        fun watchBookingStatus(bookingStatusStateViewModel: BookingStatusStateViewModel): Observer<in BookingStatus>

        fun watchBookingRequest(bookingRequestStateViewModel: BookingRequestStateViewModel)
                : Observer<BookingRequestStatus>
    }

    interface Actions : PaymentActions

}