package com.karhoo.uisdk.screen.booking.booking.bookingrequest

import androidx.lifecycle.Observer
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.PoiType
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.VehicleAttributes
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentActions
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStatus
import org.joda.time.DateTime

interface BookingRequestMVP {
    interface View {

        fun animateIn()

        fun animateOut()

        fun bindEta(quote: Quote, card: String)

        fun bindPrebook(quote: Quote, card: String, date: DateTime)

        fun bindPriceAndEta(quote: Quote, card: String)

        fun disableBooking()

        fun displayFlightDetailsField(poiType: PoiType?)

        fun enableBooking()

        fun initialiseChangeCard(price: QuotePrice? = null)

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

        fun showUpdatedPaymentDetails(savedPaymentInfo: SavedPaymentInfo?)

        fun hideLoading()

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

        fun showBookingRequest(quote: Quote, outboundTripId: String? = null)

        fun resetBooking()

        fun watchBookingStatus(bookingStatusStateViewModel: BookingStatusStateViewModel): Observer<in BookingStatus>

        fun watchBookingRequest(bookingRequestStateViewModel: BookingRequestStateViewModel)
                : Observer<BookingRequestStatus>

        fun onPaymentFailureDialogPositive()

        fun onPaymentFailureDialogCancelled()
    }

    interface Actions : PaymentActions

}
