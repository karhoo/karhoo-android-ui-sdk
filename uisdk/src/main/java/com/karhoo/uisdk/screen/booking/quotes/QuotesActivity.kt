package com.karhoo.uisdk.screen.booking.quotes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.base.address.AddressCodes
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.screen.booking.checkout.payment.WebViewActions
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.quotes.fragment.QuotesFragment

class QuotesActivity : BaseActivity(), WebViewActions {
    override val layout: Int
        get() = R.layout.uisdk_quotes_activity

    private lateinit var fragment: Fragment
    lateinit var checkoutToolbar: androidx.appcompat.widget.Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkoutToolbar = findViewById(R.id.checkoutToolbar)
        setSupportActionBar(checkoutToolbar)
        checkoutToolbar.setNavigationOnClickListener {
            val data = Intent().apply {
                putExtra(BookingActivity.Builder.EXTRA_JOURNEY_INFO, (fragment as QuotesFragment).getJourneyDetails())
            }
            setResult(QUOTES_CANCELLED, data)

            onBackPressed()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val ft = supportFragmentManager.beginTransaction()

        fragment = if(savedInstanceState != null && supportFragmentManager.getFragment(savedInstanceState, QUOTES_FRAGMENT) != null){
            supportFragmentManager.getFragment(savedInstanceState, QUOTES_FRAGMENT)!!;
        } else
            QuotesFragment.newInstance(extras)

        ft.add(R.id.quotesActivityFragmentContainer, fragment, fragment::class.java.name)
            .commit()

        (fragment as QuotesFragment).nrOfResults.observe(this, watchQuoteNumber())
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        supportFragmentManager.putFragment(outState, QUOTES_FRAGMENT, fragment);
    }

    private fun watchQuoteNumber() = Observer<Int> { quotes ->
        quotes?.let {
            checkoutToolbar.title = it.toString() + " " + getString(R.string.kh_uisdk_results)
        }
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
        const val ADDITIONAL_PASSENGERS = "ADDITIONAL_PASSENGERS"
        const val LUGGAGE = "LUGGAGE"
        const val QUOTES_RESTORE_PREVIOUS_DATA_KEY = "QUOTES_SELECTED_DATE"
        const val QUOTES_FRAGMENT = "QUOTES_FRAGMENT"
    }

}
