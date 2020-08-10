package com.karhoo.uisdk.screen.booking.booking.bookingrequest

import androidx.lifecycle.Observer
import com.braintreepayments.api.models.PaymentMethodNonce
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.PoiType
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.QuoteV2
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.VehicleAttributes
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.screen.booking.booking.PaymentActions
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

        fun enableBooking()

        fun onError()

        fun onTripBookedSuccessfully(tripInfo: TripInfo)

        fun displayFlightDetailsField(poiType: PoiType?)

        fun populateFlightDetailsField(flightNumber: String?)

        fun setCapacity(vehicleAttributes: VehicleAttributes)

        fun showGuestBookingFields()

        fun showAuthenticatedUserBookingFields()

        fun showPaymentDialog(braintreeSDKToken: String)

        fun showPaymentUI(braintreeSDKToken: String)

        fun showPrebookConfirmationDialog(quoteType: QuoteType?, tripInfo: TripInfo)

        fun showUpdatedCardDetails(savedPaymentInfo: SavedPaymentInfo?)

        fun threeDSecureNonce(braintreeSDKToken: String, nonce: String, amount: String)

    }

    interface Presenter {

        fun clearData()

        fun setBookingEnablement(hasValidPaxDetails: Boolean)

        fun setBookingFields(allFieldsValid: Boolean)

        fun handleError(stringId: Int)

        fun hideBookingRequest()

        fun makeBooking()

        fun passBackThreeDSecuredNonce(threeDSNonce: String, passengerDetails: PassengerDetails?
        = null, comments: String)

        fun setToken(braintreeSDKToken: String)

        fun showBookingRequest(quote: QuoteV2, outboundTripId: String? = null)

        fun resetBooking()

        fun updateCardDetails(braintreeSDKNonce: String?)

        fun watchBookingStatus(bookingStatusStateViewModel: BookingStatusStateViewModel): Observer<in BookingStatus>

        fun watchBookingRequest(bookingRequestStateViewModel: BookingRequestStateViewModel)
                : Observer<BookingRequestStatus>
    }

    interface Actions : PaymentActions

}