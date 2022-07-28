package com.karhoo.uisdk.screen.booking.quotes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.base.address.AddressCodes
import com.karhoo.uisdk.screen.booking.checkout.payment.WebViewActions
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.quotes.fragment.QuotesFragment
import kotlinx.android.synthetic.main.uisdk_booking_checkout_activity.*

class QuotesActivity : BaseActivity(), WebViewActions {
    override val layout: Int
        get() = R.layout.uisdk_quotes_activity

    private lateinit var fragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(checkoutToolbar)
        checkoutToolbar.setNavigationOnClickListener {
            setResult(QUOTES_CANCELLED)

            onBackPressed()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val ft = supportFragmentManager.beginTransaction()

        fragment = QuotesFragment.newInstance(extras)

        ft.add(R.id.quotesActivityFragmentContainer, fragment, fragment::class.java.name)
            .commit()
    }

    override fun handleExtras() {
        //to be completed
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                AddressCodes.PICKUP,
                AddressCodes.DESTINATION -> {
                    fragment.onActivityResult(requestCode, resultCode, data)
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Intent Builder
     */
    class Builder {

        private val extrasBundle: Bundle = Bundle()

        /**
         * By passing booking status into the Booking activity it will automatically prefill the origin
         * destination and date of the desired trip. This will only use the details available inside
         * the BookingInfo object.
         */
        fun bookingInfo(journeyDetails: JourneyDetails?): Builder {
            extrasBundle.putParcelable(QUOTES_BOOKING_INFO_KEY, journeyDetails)
            return this
        }

        fun restorePreviousData(restore: Boolean): Builder {
            extrasBundle.putBoolean(QUOTES_RESTORE_PREVIOUS_DATA_KEY, restore)
            return this
        }

        fun validityTimestamp(ts: Long): Builder {
            extrasBundle.putLong(QUOTES_SELECTED_QUOTE_VALIDITY_TIMESTAMP, ts)
            return this
        }

        /**
         * Returns a launchable Intent to the configured quotes activity with the given
         * builder parameters in the extras bundle
         */
        fun build(context: Context): Intent = Intent(context, KarhooUISDK.Routing.quotes).apply {
            putExtras(extrasBundle)
        }
    }

    override fun showWebView(url: String?) {
        //to be completed
    }

    companion object {
        const val QUOTES_BOOKING_INFO_KEY = "QUOTES_BOOKING_INFO_KEY"
        const val QUOTES_SELECTED_QUOTE_KEY = "QUOTES_SELECTED_QUOTE_KEY"
        const val QUOTES_SELECTED_QUOTE_VALIDITY_TIMESTAMP = "QUOTES_SELECTED_QUOTE_VALIDITY_TIMESTAMP"
        const val QUOTES_PICKUP_ADDRESS = "QUOTES_PICKUP_ADDRESS"
        const val QUOTES_DROPOFF_ADDRESS = "QUOTES_DROPOFF_ADDRESS"
        const val QUOTES_SELECTED_DATE = "QUOTES_SELECTED_DATE"
        const val QUOTES_RESULT_OK = 21
        const val QUOTES_INFO_REQUEST_NUMBER = 20
        const val QUOTES_CANCELLED = 22
        const val PASSENGER_NUMBER = "PASSENGER_NUMBER"
        const val LUGGAGE = "LUGGAGE"
        const val QUOTES_RESTORE_PREVIOUS_DATA_KEY = "QUOTES_SELECTED_DATE"
    }

}
