package com.karhoo.uisdk.screen.booking.checkout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.screen.booking.checkout.component.fragment.CheckoutFragment
import com.karhoo.uisdk.screen.booking.checkout.loyalty.LoyaltyInfo
import com.karhoo.uisdk.screen.booking.checkout.payment.WebViewActions
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import kotlinx.android.synthetic.main.uisdk_activity_base.khWebView
import kotlinx.android.synthetic.main.uisdk_booking_checkout_activity.checkoutToolbar
import java.util.HashMap

class CheckoutActivity : BaseActivity(), WebViewActions {
    override val layout: Int
        get() = R.layout.uisdk_booking_checkout_activity

    private lateinit var fragment: CheckoutFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(checkoutToolbar)
        checkoutToolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        if(savedInstanceState != null){
            fragment = supportFragmentManager.getFragment(savedInstanceState, CHECKOUT_FRAGMENT) as CheckoutFragment
            supportFragmentManager.beginTransaction().replace(R.id.checkoutActivityFragmentContainer, fragment, fragment::class.java.name)
                .commit()
        }
        else{
            extras?.let { extras ->
                val quote = extras.getParcelable<Quote>(BOOKING_CHECKOUT_QUOTE_KEY)

                quote?.let {
                    removeIfCheckoutFragmentExists()
                    val ft = supportFragmentManager.beginTransaction()

                    fragment = CheckoutFragment.newInstance(extras)

                    ft.add(R.id.checkoutActivityFragmentContainer, fragment, fragment::class.java.name)
                        .commit()
                } ?: run {
                    finishWithError(BOOKING_CHECKOUT_ERROR_NO_QUOTE)
                }
            } ?: run {
                finishWithError(BOOKING_CHECKOUT_ERROR_NO_QUOTE)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        supportFragmentManager.putFragment(outState, CHECKOUT_FRAGMENT, fragment)
    }

    fun removeIfCheckoutFragmentExists(){
        for(item in supportFragmentManager.fragments){
            if(item is CheckoutFragment){
                supportFragmentManager.beginTransaction().remove(item).commit();
            }
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
        data.putExtra(BOOKING_CHECKOUT_ERROR_KEY, error)
        setResult(BOOKING_CHECKOUT_ERROR, data)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        fragment.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun showWebView(url: String?) {
        url?.let { khWebView?.show(it) }
    }

    override fun onBackPressed() {
        if(khWebView.visibility == View.VISIBLE) {
            khWebView.hide()
        } else {
            fragment.onBackPressed()
        }
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
            extrasBundle.putParcelable(BOOKING_CHECKOUT_QUOTE_KEY, quote)
            return this
        }

        /**
         * The [outboundTripId] is expected when the trip is booked from a 'rebook' button in another activity,
         * It's used for analytics purposes only
         */
        fun outboundTripId(outboundTripId: String?): Builder {
            extrasBundle.putString(BOOKING_CHECKOUT_OUTBOUND_TRIP_ID_KEY, outboundTripId)
            return this
        }

        /**
         * If an [metadata] is passed in the activity, it will be used as part of the
         * Booking API meta data
         */
        fun bookingMetadata(metadata: HashMap<String, String>?): Builder {
            extrasBundle.putSerializable(BOOKING_CHECKOUT_METADATA_KEY, metadata)
            return this
        }

        /**
         * By passing journey details into the Booking activity it will automatically prefill the origin
         * destination and date of the desired trip. This will only use the details available inside
         * the JourneyDetails object.
         */
        fun journeyDetails(journeyDetails: JourneyDetails): Builder {
            extrasBundle.putParcelable(BOOKING_CHECKOUT_STATUS_KEY, journeyDetails)
            return this
        }

        /**
         * If a passenger is added, then it will be used to prefill the passenger details in the booking request component
         */
        fun passengerDetails(passenger: PassengerDetails): Builder {
            extrasBundle.putParcelable(BOOKING_CHECKOUT_PASSENGER_KEY, passenger)
            return this
        }

        /**
         * By passing comments into the Checkout Component it will automatically prefill the
         * comments of the desired trip.
         */
        fun comments(comments: String): Builder {
            extrasBundle.putString(BOOKING_CHECKOUT_COMMENTS_KEY, comments)
            return this
        }

        /**
         * By passing the loyalty info, the Checkout Component will adjust the behaviour of the
         * loyalty sub-component
         */
        fun loyaltyInfo(loyaltyInfo: LoyaltyInfo): Builder {
            extrasBundle.putParcelable(BOOKING_CHECKOUT_LOYALTY_KEY, loyaltyInfo)
            return this
        }

        /**
         * Sets the validity timestamp of the quote
         * When validity of the quote is expired, a popup will be shown to the user to notify him
         */
        fun validityDeadlineTimestamp(timestamp: Long): Builder {
            extrasBundle.putLong(BOOKING_CHECKOUT_VALIDITY_KEY, timestamp)
            return this
        }

        /**
         * Returns a launchable Intent to the configured booking activity with the given
         * builder parameters in the extras bundle
         */
        fun build(context: Context): Intent = Intent(context, KarhooUISDK.Routing.checkout).apply {
            putExtras(extrasBundle)
        }

    }

    companion object {
        const val BOOKING_CHECKOUT_QUOTE_KEY = "BOOKING_CHECKOUT_INPUT_QUOTE_KEY"
        const val BOOKING_CHECKOUT_OUTBOUND_TRIP_ID_KEY = "BOOKING_CHECKOUT_OUTBOUND_TRIP_ID_KEY"
        const val BOOKING_CHECKOUT_METADATA_KEY = "BOOKING_CHECKOUT_METADATA_KEY"
        const val BOOKING_CHECKOUT_STATUS_KEY = "BOOKING_STATUS_KEY"
        const val BOOKING_CHECKOUT_TRIP_INFO_KEY = "TRIP_INFO_KEY"
        const val BOOKING_CHECKOUT_PREBOOK_TRIP_INFO_KEY = "PREBOOK_TRIP_INFO_KEY"
        const val BOOKING_CHECKOUT_PREBOOK_QUOTE_TYPE_KEY = "PREBOOK_QUOTE_TYPE_KEY"
        const val BOOKING_CHECKOUT_ERROR_DATA = "BOOKING_CHECKOUT_ERROR_DATA"
        const val BOOKING_CHECKOUT_PASSENGER_KEY = "PASSENGER_KEY"
        const val BOOKING_CHECKOUT_COMMENTS_KEY = "BOOKING_CHECKOUT_COMMENTS_KEY"
        const val BOOKING_CHECKOUT_LOYALTY_KEY = "BOOKING_CHECKOUT_LOYALTY_KEY"
        const val BOOKING_CHECKOUT_VALIDITY_KEY = "BOOKING_CHECKOUT_VALIDITY_KEY"
        const val BOOKING_CHECKOUT_CANCELLED = 11
        const val BOOKING_CHECKOUT_EXPIRED = 12

        /** Errors outputted by the Booking Request Activity**/
        const val BOOKING_CHECKOUT_ERROR = 10
        const val BOOKING_CHECKOUT_ERROR_KEY = "BOOKING_CHECKOUT_ERROR_KEY"
        const val BOOKING_CHECKOUT_ERROR_NO_QUOTE = "BOOKING_CHECKOUT_ERROR_NO_QUOTE_KEY"
        private const val CHECKOUT_FRAGMENT = "CHECKOUT_FRAGMENT"
    }
}
