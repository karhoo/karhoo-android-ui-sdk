package com.karhoo.uisdk.screen.booking.checkout.component.views

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
import com.karhoo.uisdk.screen.booking.checkout.component.fragment.BookButtonState
import com.karhoo.uisdk.screen.booking.checkout.component.fragment.CheckoutFragmentContract
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

        fun onError(error: KarhooError?)

        fun onTripBookedSuccessfully(tripInfo: TripInfo)

        fun populateFlightDetailsField(flightNumber: String?)

        fun setCapacityAndCapabilities(capabilities: List<Capability>, vehicle: QuoteVehicle)

        fun fillInPassengerDetails(details: PassengerDetails?)

        fun showPaymentFailureDialog(error: KarhooError?)

        fun showPaymentUI()

        fun showLoading(show: Boolean)

        fun showPrebookConfirmationDialog(quoteType: QuoteType?, tripInfo: TripInfo)

        fun showUpdatedPaymentDetails(savedPaymentInfo: SavedPaymentInfo?)

        fun startBooking()

        fun setListeners(loadingButtonCallback: CheckoutFragmentContract.LoadingButtonListener,
                         webViewListener: CheckoutFragmentContract.WebViewListener,
                         passengersListener: CheckoutFragmentContract.PassengersListener,
                         bookingListener: CheckoutFragmentContract.BookingListener)

        fun bindPassenger(passengerDetails: PassengerDetails?)

        fun showPassengerDetails(show: Boolean)

        fun arePassengerDetailsValid(): Boolean

        fun isPaymentMethodValid(): Boolean

        fun clickedPassengerSaveButton()

        fun isPassengerDetailsViewVisible(): Boolean

        fun consumeBackPressed(): Boolean
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

        fun showBookingRequest(quote: Quote, bookingStatus: BookingStatus?, outboundTripId: String? = null, bookingMetadata:
        HashMap<String, String>? = null)

        fun resetBooking()

        fun getPassengerDetails()

        fun watchBookingStatus(bookingStatusStateViewModel: BookingStatusStateViewModel): Observer<in BookingStatus>

        fun watchBookingRequest(bookingRequestStateViewModel: BookingRequestStateViewModel)
                : Observer<BookingRequestStatus>

        fun onPaymentFailureDialogPositive()

        fun onPaymentFailureDialogCancelled()

        fun setBookingStatus(bookingStatus: BookingStatus?)

        fun consumeBackPressed(): Boolean

        fun getBookingButtonState(arePassengerDetailsValid: Boolean, isPaymentValid: Boolean):
                BookButtonState
    }

    interface PrebookViewActions {
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
