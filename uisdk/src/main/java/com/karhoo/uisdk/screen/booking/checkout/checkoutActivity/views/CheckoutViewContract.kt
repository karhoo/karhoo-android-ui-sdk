package com.karhoo.uisdk.screen.booking.checkout.checkoutActivity.views

import android.content.Intent
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.PoiType
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.QuoteVehicle
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.screen.booking.checkout.checkoutActivity.fragment.CheckoutFragmentContract
import com.karhoo.uisdk.screen.booking.checkout.payment.WebViewActions
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStatus
import com.karhoo.uisdk.screen.booking.quotes.extendedcapabilities.Capability
import org.joda.time.DateTime

interface CheckoutViewContract {
    interface View {

        fun bindEta(quote: Quote, card: String)

        fun bindPrebook(quote: Quote, card: String, date: DateTime)

        fun bindPriceAndEta(quote: Quote, card: String)

        fun bindQuoteAndTerms(vehicle: Quote, isPrebook: Boolean)

        fun displayFlightDetailsField(poiType: PoiType?)

        fun initialiseChangeCard(quote: Quote? = null)

        fun initialiseGuestPayment(quote: Quote?)

        fun initialisePaymentProvider(quote: Quote?)

        fun onError()

        fun onTripBookedSuccessfully(tripInfo: TripInfo)

        fun populateFlightDetailsField(flightNumber: String?)

        fun setCapacityAndCapabilities(capabilities: List<Capability>, vehicle: QuoteVehicle)

        fun showGuestBookingFields(details: PassengerDetails?)

        fun showAuthenticatedUserBookingFields()

        fun showPaymentFailureDialog(error: KarhooError?)

        fun showPaymentUI()

        fun showLoading(show: Boolean)

        fun showPrebookConfirmationDialog(quoteType: QuoteType?, tripInfo: TripInfo)

        fun showUpdatedPaymentDetails(savedPaymentInfo: SavedPaymentInfo?)

        fun startBooking()

        fun setListeners(loadingButtonCallback: CheckoutFragmentContract.LoadingButtonListener,
                         termsListener: CheckoutFragmentContract.TermsListener,
                         passengersListener: CheckoutFragmentContract.PassengersListener
                        )

        fun bindPassenger(passengerDetails: PassengerDetails?)

        fun bindPaymentMethod(paymentInfo: SavedPaymentInfo?)

        fun showPassengerDetails(show: Boolean)

        fun arePassengerDetailsValid(): Boolean
    }

    interface Presenter {

        fun clearData()

        fun handleChangeCard()

        fun handleError(stringId: Int, karhooError: KarhooError?)

        fun makeBooking()

        fun isPaymentSet(): Boolean

        fun passBackPaymentIdentifiers(identifier: String, tripId: String? = null,
                                       passengerDetails: PassengerDetails? = null,
                                       comments: String)

        fun setBookingFields(allFieldsValid: Boolean)

        fun showBookingRequest(quote: Quote, bookingStatus: BookingStatus?, outboundTripId: String? = null, bookingMetadata:
        HashMap<String, String>? = null)

        fun resetBooking()

        fun watchBookingStatus(bookingStatusStateViewModel: BookingStatusStateViewModel): Observer<in BookingStatus>

        fun watchBookingRequest(bookingRequestStateViewModel: BookingRequestStateViewModel)
                : Observer<BookingRequestStatus>

        fun onPaymentFailureDialogPositive()

        fun onPaymentFailureDialogCancelled()

        fun setBookingStatus(bookingStatus: BookingStatus?)
    }

    interface Actions : WebViewActions {
        fun finishedBooking()
    }

    interface BookingRequestViewWidget {
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
        fun showBookingRequest(quote: Quote, bookingStatus: BookingStatus?, outboundTripId: String? = null, bookingMetadata:
        HashMap<String, String>?)
    }

    sealed class Event {
        data class TermsAndConditionsRequested(val url: String) : Event()
        data class BookingSuccess(val tripInfo: TripInfo) : Event()
        data class BookingError(@StringRes val stringId: Int, val karhooError: KarhooError?) : Event()
    }

    sealed class Action {
        data class ShowTermsAndConditions(val url: String) : Action()
        object WaitForTripAllocation : Action()
        data class HandleBookingError(@StringRes val stringId: Int, val karhooError: KarhooError?) : Action()
    }

}
