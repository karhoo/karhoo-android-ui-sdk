package com.karhoo.uisdk.screen.rides

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.screen.booking.BookingActivity
import kotlinx.android.synthetic.main.uisdk_activity_rides.bookRideButton
import kotlinx.android.synthetic.main.uisdk_activity_rides.toolbar
import kotlinx.android.synthetic.main.uisdk_activity_rides.toolbarProgressBar
import kotlinx.android.synthetic.main.uisdk_activity_rides.viewPager

class RidesActivity : BaseActivity(), RidesLoading, ViewPager.OnPageChangeListener {

    private var adapter: LayoutArrayPagerAdapter? = null

    override val layout: Int = R.layout.uisdk_activity_rides

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
        val intent = BookingActivity.Builder.builder.build(this)
        startActivity(intent)
    }

    override fun handleExtras() {
        // Do nothing
    }

    override fun initialiseViews() {
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
