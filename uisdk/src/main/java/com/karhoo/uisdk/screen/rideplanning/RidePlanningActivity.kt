package com.karhoo.uisdk.screen.rideplanning

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarMVP
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarViewContract
import com.karhoo.uisdk.screen.booking.checkout.component.views.CheckoutViewContract
import com.karhoo.uisdk.screen.booking.checkout.loyalty.LoyaltyInfo
import com.karhoo.uisdk.screen.booking.checkout.tripallocation.TripAllocationContract
import com.karhoo.uisdk.screen.booking.domain.address.JourneyInfo
import com.karhoo.uisdk.screen.booking.map.BookingMapMVP
import com.karhoo.uisdk.screen.rides.RidesActivity
import kotlinx.android.synthetic.main.uisdk_activity_base.*
import kotlinx.android.synthetic.main.uisdk_activity_booking_main.*
import kotlinx.android.synthetic.main.uisdk_activity_booking_main.navigationDrawerWidget
import kotlinx.android.synthetic.main.uisdk_activity_booking_main.navigationWidget
import kotlinx.android.synthetic.main.uisdk_activity_ride_planning.*
import kotlinx.android.synthetic.main.uisdk_nav_header_main.*
import kotlinx.android.synthetic.main.uisdk_view_booking_map.*
import kotlinx.android.synthetic.main.uisdk_view_booking_mode.*

class RidePlanningActivity : BaseActivity(), AddressBarMVP.Actions, BookingMapMVP.Actions,
    TripAllocationContract.Actions {

    override val layout: Int
        get() = R.layout.uisdk_activity_ride_planning

    private lateinit var view: RidePlanningContract.View

    override fun onCreate(savedInstanceState: Bundle?) {
        if (KarhooUISDKConfigurationProvider.configuration.forceDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        window.allowEnterTransitionOverlap = true

        super.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)

        supportActionBar?.setHomeActionContentDescription(getString(R.string.kh_uisdk_close_the_screen))

        if (KarhooUISDK.menuHandler == null) {
            supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                it.setHomeAsUpIndicator(R.drawable.uisdk_ic_close)
                it.title = ""
            }
        } else {
            supportActionBar?.let { navigationDrawerWidget.setToggleToolbar(toolbar, it) }
        }
    }

    override fun onResume() {
        super.onResume()

        setNavHeaderImage()
    }

    override fun initialiseViews() {
        view = findViewById(R.id.ride_planning_view)
        view.setup(RidePlanningPresenter(), intent.extras, this)
        view.checkoutObserver = createCheckoutObserver()
        view.handleNavigationVisiblityCallback = { visibility, lockMode ->
            toolbar.visibility = visibility
            navigationDrawerWidget.setDrawerLockMode(lockMode)
        }
    }

    private fun setNavHeaderImage() {
        Handler().postDelayed({
            KarhooUISDKConfigurationProvider.configuration.logo()?.let {
                navigationHeaderIcon?.setImageDrawable(it)
            } ?: run {
                navigationHeaderIcon?.setImageDrawable(getDrawable(R.drawable.uisdk_karhoo_wordmark))
            }
        }, NAVIGATION_ICON_DELAY)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        view.saveInstanceState(outState)
    }

    override fun handleExtras() {
        view.parseExtrasBundle(extras)
    }

    override fun bindViews() {
        if (KarhooUISDKConfigurationProvider.isGuest()) {
            navigationWidget.menu.removeItem(R.id.action_rides)
            navigationWidget.menu.removeItem(R.id.action_profile)
        }

        view.bindViews(context = this)
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if(!view.onReceivedPermissionsResult(this, requestCode, permissions, grantResults)) {
            showLocationLock()
        }
    }

    override fun initialiseViewListeners() {
        navigationWidget.setNavigationItemSelectedListener(navigationDrawerWidget)

        view.initializeListeners(this)
    }

    override fun selectAddress(intent: Intent, addressCode: Int) {
        startActivityForResult(intent, addressCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        view.parseDataFromActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        // if Webview is visible we hide it
        if (khWebView?.visibility == View.VISIBLE) {
            khWebView?.hide()
            return
        }
        // if destination set we clear it, close the quotes list and return
        if (RidePlanningStorage.journeyDetailsStateViewModel.currentState.destination != null) {
            RidePlanningStorage.journeyDetailsStateViewModel.process(
                AddressBarViewContract.AddressBarEvent
                    .DestinationAddressEvent(null)
            )
            return
        }
        if (navigationDrawerWidget.closeIfOpen()) {
            super.onBackPressed()
        }
    }

    private fun showWebView(url: String) {
        khWebView?.show(url)
    }

    private fun waitForTripAllocation() {
        toolbar.visibility = View.INVISIBLE
        navigationDrawerWidget.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!KarhooUISDKConfigurationProvider.isGuest()) {
            menuInflater.inflate(R.menu.uisdk_booking_menu, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.booking_action_rides -> {
                startActivity(
                    RidesActivity.Builder.builder
                        .passengerDetails(RidePlanningStorage.passengerDetails)
                        .build(this)
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBookingCancelledOrFinished() {
        view.onBookingCancelledOrFinished()

        toolbar.visibility = View.VISIBLE
        navigationDrawerWidget.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        RidePlanningStorage.journeyDetailsStateViewModel.process(AddressBarViewContract.AddressBarEvent.ResetJourneyDetailsEvent)
    }

    private fun createCheckoutObserver(): Observer<in CheckoutViewContract.Action> {
        return Observer { actions ->
            when (actions) {
                is CheckoutViewContract.Action.ShowTermsAndConditions ->
                    showWebView(actions.url)
                is CheckoutViewContract.Action.WaitForTripAllocation ->
                    waitForTripAllocation()
                is CheckoutViewContract.Action.HandleBookingError ->
                    showErrorDialog(actions.stringId, actions.karhooError)
            }
        }
    }

    /**
     * Intent Builder
     */
    class Builder private constructor() {

        private val extrasBundle: Bundle = Bundle()

        /**
         * The activity will take the origin and destination, if available from [tripDetails],
         * use this to prepopulate the addressview and begin fetching quotes
         */
        fun tripDetails(tripDetails: TripInfo): Builder {
            extrasBundle.putParcelable(EXTRA_TRIP_DETAILS, tripDetails)
            return this
        }

        /**
         * By passing passenger details into the Booking activity it will automatically prefill the
         * passenger details of the desired trip. This will only use the details available inside
         * the [PassengerDetails] object.
         */
        fun passengerDetails(passengerDetails: PassengerDetails): Builder {
            extrasBundle.putParcelable(EXTRA_PASSENGER_DETAILS, passengerDetails)
            return this
        }

        /**
         * By passing comments into the Booking activity it will automatically prefill the
         * comments of the desired trip.
         */
        fun comments(comments: String): Builder {
            extrasBundle.putString(EXTRA_COMMENTS, comments)
            return this
        }

        /**
         * The [outboundTripId] is expected when the trip is booked from a 'rebook' button in another activity,
         * It's used for analytics purposes only
         */
        fun outboundTripId(outboundTripId: String): Builder {
            extrasBundle.putString(EXTRA_OUTBOUND_TRIP_ID, outboundTripId)
            return this
        }

        /**
         * If an [initialLocation] is passed in the activity will zoom straight to this
         * without having to wait for the device to return a location
         */
        fun initialLocation(initialLocation: Location?): Builder {
            initialLocation?.let {
                extrasBundle.putParcelable(EXTRA_INITIAL_LOCATION, it)
            }
            return this
        }

        /**
         * By passing journey info into the Booking activity it will automatically prefill the origin
         * destination and date of the desired trip. This will only use the details available inside
         * the [JourneyInfo] object.
         */
        fun journeyInfo(journeyInfo: JourneyInfo): Builder {
            extrasBundle.putParcelable(EXTRA_JOURNEY_INFO, journeyInfo)
            return this
        }

        /**
         * If an [metadata] is passed in the activity, it will be used as part of the
         * Booking API meta data
         */
        fun bookingMetadata(metadata: HashMap<String, String>?): Builder {
            metadata?.let {
                extrasBundle.putSerializable(EXTRA_META, it)
            }
            return this
        }

        fun loyaltyInfo(loyaltyInfo: LoyaltyInfo?): Builder {
            loyaltyInfo?.let {
                extrasBundle.putParcelable(EXTRA_LOYALTY_INFO, loyaltyInfo)
            }

            return this
        }

        /**
         * Returns a launchable Intent to the configured booking activity with the given
         * builder parameters in the extras bundle
         */
        fun build(context: Context): Intent = Intent(context, KarhooUISDK.Routing.booking).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtras(extrasBundle)
        }

        /**
         * Returns a launchable Intent to the configure booking activity with the given
         * builder parameters in the extras bundle, when the intent is being started
         * with startActivityForResult()
         */
        fun buildForOnActivityResultCallback(context: Context): Intent =
            Intent(context, KarhooUISDK.Routing.booking).apply {
                putExtras(extrasBundle)
            }

        companion object {

            const val EXTRA_TRIP_DETAILS = "trip::details"
            const val EXTRA_OUTBOUND_TRIP_ID = "outboundTripId"
            const val EXTRA_INITIAL_LOCATION = "extraInitialLocation"
            const val EXTRA_JOURNEY_INFO = "journey::info"
            const val EXTRA_PASSENGER_DETAILS = "booking::passenger"
            const val EXTRA_COMMENTS = "booking::comments"
            const val EXTRA_META = "booking::meta"
            const val EXTRA_LOYALTY_INFO = "extraLoyaltyInfo"

            val builder: Builder
                get() = Builder()
        }
    }

    companion object {
        const val REQ_CODE_BRAINTREE = 301
        private const val REQ_CODE_BRAINTREE_GUEST = 302
        private const val NAVIGATION_ICON_DELAY = 100L
    }

}
