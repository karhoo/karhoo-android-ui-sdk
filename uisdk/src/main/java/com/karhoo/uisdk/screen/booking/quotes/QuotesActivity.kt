package com.karhoo.uisdk.screen.booking.quotes

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.base.address.AddressCodes
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity
import com.karhoo.uisdk.screen.booking.checkout.payment.WebViewActions
import com.karhoo.uisdk.screen.booking.domain.address.BookingInfo
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
        fun bookingInfo(bookingInfo: BookingInfo): Builder {
            extrasBundle.putParcelable(CheckoutActivity.BOOKING_CHECKOUT_STATUS_KEY, bookingInfo)
            return this
        }

    }


    override fun showWebView(url: String?) {
        //to be completed
    }

    companion object {
        const val QUOTES_BOOKING_INFO_KEY = "QUOTES_BOOKING_INFO_KEY"
        const val QUOTES_BOOKING_INFO_REQUEST_NUMBER = 20
    }

}
