package com.karhoo.uisdk.screen.booking.booking.bookingrequest

import androidx.lifecycle.Observer
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.PoiType
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.QuoteVehicle
import com.karhoo.sdk.api.model.TripInfo
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

        fun enableCancelButton()

        fun initialiseChangeCard(quote: Quote? = null)

        fun initialiseGuestPayment(quote: Quote?)

        fun initialisePaymentProvider(quote: Quote?)

        fun onError()

        fun onTripBookedSuccessfully(tripInfo: TripInfo)

        fun populateFlightDetailsField(flightNumber: String?)

        fun setCapacity(vehicle: QuoteVehicle)

        fun showGuestBookingFields(details: PassengerDetails = PassengerDetails())

        fun showAuthenticatedUserBookingFields()

        fun showPaymentFailureDialog(error: KarhooError?)

        fun showPaymentUI()

        fun showPrebookConfirmationDialog(quoteType: QuoteType?, tripInfo: TripInfo)

        fun showUpdatedPaymentDetails(savedPaymentInfo: SavedPaymentInfo?)

        fun hideLoading()

        fun updateBookingButtonForGuest()

    }

    interface Presenter {

        fun clearData()

        fun handleChangeCard()

        fun handleError(stringId: Int, karhooError: KarhooError?)

        fun hideBookingRequest()

        fun makeBooking()

        fun passBackPaymentIdentifiers(identifier: String, tripId: String? = null,
                                       passengerDetails: PassengerDetails? = null, comments: String)

        fun setBookingEnablement(hasValidPaxDetails: Boolean)

        fun setBookingFields(allFieldsValid: Boolean)

        fun showBookingRequest(quote: Quote, outboundTripId: String? = null, bookingMeta:
        HashMap<String, String>? = null)

        fun resetBooking()

        fun watchBookingStatus(bookingStatusStateViewModel: BookingStatusStateViewModel): Observer<in BookingStatus>

        fun watchBookingRequest(bookingRequestStateViewModel: BookingRequestStateViewModel)
                : Observer<BookingRequestStatus>

        fun onPaymentFailureDialogPositive()

        fun onPaymentFailureDialogCancelled()

        fun onTermsAndConditionsRequested(url: String?)
    }

    interface Actions : PaymentActions {
        fun finishedBooking()
    }

}
