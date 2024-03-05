package com.karhoo.uisdk.screen.rides

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity

class RidesActivity : BaseActivity(), RidesLoading, ViewPager.OnPageChangeListener {

    private var adapter: LayoutArrayPagerAdapter? = null

    override val layout: Int = R.layout.uisdk_activity_rides

    private lateinit var bookRideButton: Button
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarProgressBar: ProgressBar
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        toolbar.setNavigationOnClickListener { finish() }
        bookRideButton.setOnClickListener { goToBooking() }
    }

    override fun onResume() {
        super.onResume()
        adapter?.apply {
            setLoader(this@RidesActivity)
            refresh()
        }
    }

    private fun goToBooking() {
        val passenger = extras?.getParcelable<PassengerDetails?>(CheckoutActivity.BOOKING_CHECKOUT_PASSENGER_KEY)

        val intent = BookingActivity.Builder.builder
        passenger?.let {
            intent.passengerDetails(it)
        }
        startActivity(intent.build(this))
    }

    override fun handleExtras() {
        // Do nothing
    }

    override fun initialiseViews() {
        bookRideButton = findViewById(R.id.bookRideButton)
        toolbar = findViewById(R.id.toolbar)
        toolbarProgressBar = findViewById(R.id.toolbarProgressBar)
        viewPager = findViewById(R.id.viewPager)

        val pages = arrayOf(LayoutArrayPagerAdapter.Page(getString(R.string.kh_uisdk_title_page_upcoming), R.layout.uisdk_page_rides_upcoming),
                            LayoutArrayPagerAdapter.Page(getString(R.string.kh_uisdk_title_page_past), R.layout.uisdk_page_rides_past))
        adapter = LayoutArrayPagerAdapter(pages)
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(this)
        sendAnalytics(viewPager.currentItem)
    }

    override fun showLoading() {
        toolbarProgressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        toolbarProgressBar.visibility = View.INVISIBLE
    }

    /**
     * Intent Builder
     */
    class Builder private constructor() {

        private val extras: Bundle = Bundle()

        /**
         * Returns a launchable Intent to the configured rides activity with the given
         * builder parameters in the extras bundle
         */
        fun build(context: Context): Intent {
            val intent = Intent(context, KarhooUISDK.Routing.rides)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtras(extras)
            return intent
        }

        fun passengerDetails(passenger: PassengerDetails?): Builder {
            passenger?.let {
                extras.putParcelable(CheckoutActivity.BOOKING_CHECKOUT_PASSENGER_KEY, passenger)
            }
            return this
        }

        companion object {
            val builder: Builder
                get() = Builder()
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        // do nothing
    }

    override fun onPageSelected(position: Int) {
        sendAnalytics(position)
    }

    override fun onPageScrollStateChanged(state: Int) {
        // do nothing
    }

    private fun sendAnalytics(position: Int){
        when(position){
            0 -> KarhooUISDK.analytics?.upcomingTripsOpened()
            1 -> KarhooUISDK.analytics?.pastTripsOpened()
        }
    }

}
