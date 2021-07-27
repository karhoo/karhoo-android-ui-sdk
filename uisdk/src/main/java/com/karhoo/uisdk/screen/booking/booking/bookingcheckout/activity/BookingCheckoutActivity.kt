package com.karhoo.uisdk.screen.booking.booking.bookingcheckout.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.screen.booking.booking.bookingcheckout.fragment.BookingCheckoutFragment
import com.karhoo.uisdk.screen.booking.booking.bookingcheckout.views.BookingCheckoutViewContract
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentActions
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import kotlinx.android.synthetic.main.uisdk_activity_base.khWebView
import java.util.*

class BookingCheckoutActivity : BaseActivity(), PaymentActions {
    override val layout: Int
        get() = R.layout.uisdk_booking_checkout_activity

    private var isGuest: Boolean = false

    private var holdOpenForPaymentFlow = false

    private lateinit var presenter: BookingCheckoutViewContract.Presenter
    private lateinit var fragment: BookingCheckoutFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        extras?.let { extras ->
            val quote = extras.getParcelable<Quote>(BOOKING_REQUEST_QUOTE_KEY)

            if (quote != null) {
                val ft = supportFragmentManager.beginTransaction()

                fragment = BookingCheckoutFragment.newInstance(
                        extras
                )

                ft.add(R.id.checkoutActivityFragmentContainer, fragment,
                        fragment::class.java.name
                ).commit()
            } else {
                finishWithError(BOOKING_REQUEST_ERROR_NO_QUOTE)
            }
        } ?: run {
            finishWithError(BOOKING_REQUEST_ERROR_NO_QUOTE)
        }
    }

    override fun handleExtras() {
        // do nothing
    }

    /**
     * Method used for finishing up the booking request activity with an error
     * The activity which launches the BookingRequestActivity should handle the error result
     * under BOOKING_REQUEST_ERROR (result code 10) and act accordingly
     * @param error reason for closing the BookingRequestActivity
     */
    private fun finishWithError(error: String) {
        val data = Intent()
        data.putExtra(BookingCheckoutActivity.BOOKING_REQUEST_ERROR_KEY, error)

        setResult(BookingCheckoutActivity.BOOKING_REQUEST_ERROR, data)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun showWebView(url: String?) {
        url?.let { khWebView?.show(it) }
    }

    /**
     * Intent Builder
     */
    class Builder {

        private val extrasBundle: Bundle = Bundle()

        /**
         * The activity will take the quote object and
         * use this to prepopulate eta, pricing, vechicle details fields
         * @param quote mandatory param for starting up the Booking Request Activity
         */
        fun quote(quote: Quote): Builder {
            extrasBundle.putParcelable(BOOKING_REQUEST_QUOTE_KEY, quote)
            return this
        }

        /**
         * The [outboundTripId] is expected when the trip is booked from a 'rebook' button in another activity,
         * It's used for analytics purposes only
         */
        fun outboundTripId(outboundTripId: String?): Builder {
            extrasBundle.putString(BOOKING_REQUEST_OUTBOUND_TRIP_ID_KEY, outboundTripId)
            return this
        }

        /**
         * If an [metadata] is passed in the activity, it will be used as part of the
         * Booking API meta data
         */
        fun bookingMetadata(metadata: HashMap<String, String>?): Builder {
            extrasBundle.putSerializable(BOOKING_REQUEST_METADATA_KEY, metadata)
            return this
        }

        /**
         * By passing booking status into the Booking activity it will automatically prefill the origin
         * destination and date of the desired trip. This will only use the details available inside
         * the BookingStatus object.
         */
        fun bookingStatus(bookingStatus: BookingStatus): Builder {
            extrasBundle.putParcelable(BOOKING_REQUEST_STATUS_KEY, bookingStatus)
            return this
        }

        /**
         * If a passenger is added, then it will be used to prefill the passenger details in the booking request component
         */
        fun passengerDetails(passenger: PassengerDetails): Builder {
            extrasBundle.putParcelable(BOOKING_REQUEST_PASSENGER_KEY, passenger)
            return this
        }

        /**
         * Returns a launchable Intent to the configured booking activity with the given
         * builder parameters in the extras bundle
         */
        fun build(context: Context): Intent = Intent(context, KarhooUISDK.Routing.bookingRequest).apply {
            putExtras(extrasBundle)
        }
    }

    companion object {
        const val BOOKING_REQUEST_QUOTE_KEY = "BOOKING_REQUEST_INPUT_QUOTE"
        const val BOOKING_REQUEST_OUTBOUND_TRIP_ID_KEY = "BOOKING_REQUEST_OUTBOUND_TRIP_ID_KEY"
        const val BOOKING_REQUEST_METADATA_KEY = "BOOKING_REQUEST_METADATA_KEY"
        const val BOOKING_REQUEST_STATUS_KEY = "BOOKING_STATUS_KEY"
        const val BOOKING_REQUEST_TRIP_INFO_KEY = "TRIP_INFO_KEY"
        const val BOOKING_REQUEST_PASSENGER_KEY = "PASSENGER_KEY"

        /** Errors outputted by the Booking Request Activity**/
        const val BOOKING_REQUEST_ERROR = 10
        const val BOOKING_REQUEST_ERROR_KEY = "BOOKING_REQUEST_ERROR_KEY"
        const val BOOKING_REQUEST_ERROR_NO_QUOTE = "BOOKING_REQUEST_ERROR_NO_QUOTE"
    }
}
