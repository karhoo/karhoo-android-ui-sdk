package com.karhoo.uisdk.screen.booking

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.base.address.AddressCodes
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarMVP
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarViewContract
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity
import com.karhoo.uisdk.screen.booking.checkout.component.views.CheckoutViewContract
import com.karhoo.uisdk.screen.booking.checkout.loyalty.LoyaltyInfo
import com.karhoo.uisdk.screen.booking.checkout.prebookconfirmation.PrebookConfirmationView
import com.karhoo.uisdk.screen.booking.checkout.tripallocation.TripAllocationContract
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import com.karhoo.uisdk.screen.booking.domain.address.JourneyInfo
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStateViewModel
import com.karhoo.uisdk.screen.booking.map.BookingMapMVP
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_CANCELLED
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_INFO_REQUEST_NUMBER
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_SELECTED_QUOTE_KEY
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_SELECTED_QUOTE_VALIDITY_TIMESTAMP
import com.karhoo.uisdk.screen.rides.RidesActivity
import com.karhoo.uisdk.util.extension.isLocateMeEnabled
import com.karhoo.uisdk.util.extension.toSimpleLocationInfo
import kotlinx.android.synthetic.main.uisdk_activity_base.khWebView
import kotlinx.android.synthetic.main.uisdk_activity_booking_content.addressBarWidget
import kotlinx.android.synthetic.main.uisdk_activity_booking_content.bookingMapWidget
import kotlinx.android.synthetic.main.uisdk_activity_booking_content.toolbar
import kotlinx.android.synthetic.main.uisdk_activity_booking_content.tripAllocationWidget
import kotlinx.android.synthetic.main.uisdk_activity_booking_main.navigationDrawerWidget
import kotlinx.android.synthetic.main.uisdk_activity_booking_main.navigationWidget
import kotlinx.android.synthetic.main.uisdk_nav_header_main.navigationHeaderIcon
import kotlinx.android.synthetic.main.uisdk_view_booking_map.locateMeButton
import org.joda.time.DateTime

class BookingActivity : BaseActivity(), AddressBarMVP.Actions, BookingMapMVP.Actions,
                        TripAllocationContract.Actions {

    private val journeyDetailsStateViewModel: JourneyDetailsStateViewModel by lazy { ViewModelProvider(this).get(JourneyDetailsStateViewModel::class.java) }
    private val bookingRequestStateViewModel: BookingRequestStateViewModel by lazy { ViewModelProvider(this).get(BookingRequestStateViewModel::class.java) }

    private var quote: Quote? = null

    private var tripDetails: TripInfo? = null // field can be removed if we remove usage of the BaseActivity "lifecycle"
    private var outboundTripId: String? = null // field can be removed if we remove usage of the BaseActivity "lifecycle"
    private var journeyInfo: JourneyInfo? = null
    private var passengerDetails: PassengerDetails? = null
    private var loyaltyInfo: LoyaltyInfo? = null
    private var bookingComments: String? = ""
    private var bookingMetadata: HashMap<String, String>? = null

    private var isGuest = false

    override val layout: Int
        get() = R.layout.uisdk_activity_booking_main

    override fun onCreate(savedInstanceState: Bundle?) {
        window.allowEnterTransitionOverlap = true
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            if(it[JOURNEY_INFO] != null) {
                val journeyDetails = it[JOURNEY_INFO] as JourneyDetails
                journeyDetailsStateViewModel.process(
                    AddressBarViewContract.AddressBarEvent
                        .PrebookBookingEvent(
                            journeyDetails.pickup,
                            journeyDetails.destination,
                            journeyDetails.date
                        )
                )
            }
        }

        setSupportActionBar(toolbar)
        if (KarhooUISDK.menuHandler == null) {
            supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                it.setHomeAsUpIndicator(R.drawable.uisdk_ic_close)
                it.title = ""
            }
        } else {
            supportActionBar?.let { navigationDrawerWidget.setToggleToolbar(toolbar, it) }
        }

        bookingMapWidget.onCreate(savedInstanceState, this, journeyDetailsStateViewModel,
                                  tripDetails?.destination == null, journeyInfo != null)

        bookingMetadata = KarhooUISDKConfigurationProvider.configuration.bookingMetadata()

        KarhooUISDK.analytics?.bookingScreenOpened()
    }

    override fun onResume() {
        super.onResume()
        setWatchers()
        setNavHeaderImage()
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

    private fun setWatchers() {
        addressBarWidget.bindTripToView(tripDetails)
        tripDetails = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(JOURNEY_INFO, journeyDetailsStateViewModel.currentState)
        bookingMapWidget.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        bookingMapWidget.onLowMemory()
    }

    override fun handleExtras() {
        extras?.let { extras ->
            tripDetails = extras.get(Builder.EXTRA_TRIP_DETAILS) as TripInfo?
            tripDetails?.let {
                journeyDetailsStateViewModel.process(AddressBarViewContract.AddressBarEvent
                                                            .AsapBookingEvent(it.origin?.toSimpleLocationInfo(), it.destination?.toSimpleLocationInfo()))
            }
            outboundTripId = extras.getString(Builder.EXTRA_OUTBOUND_TRIP_ID, null)
            val initialLocation = extras.getParcelable<Location>(Builder.EXTRA_INITIAL_LOCATION)
            initialLocation?.let { bookingMapWidget.initialLocation = LatLng(it.latitude, it.longitude) }
            journeyInfo = extras.getParcelable(Builder.EXTRA_JOURNEY_INFO)
            passengerDetails = extras.getParcelable(Builder.EXTRA_PASSENGER_DETAILS)
            bookingComments = extras.getString(Builder.EXTRA_COMMENTS)
            loyaltyInfo = extras.getParcelable(Builder.EXTRA_LOYALTY_INFO)
            val injectedBookingMetadata = extras.getSerializable(Builder.EXTRA_META) as? HashMap<String, String>
            injectedBookingMetadata?.let {
                bookingMetadata?.putAll(it)
            }
        }
    }

    override fun initialiseViews() {
        isGuest = KarhooUISDKConfigurationProvider.isGuest()

        addressBarWidget.watchJourneyDetailsState(this@BookingActivity, journeyDetailsStateViewModel)
        tripAllocationWidget.watchBookingRequestStatus(this@BookingActivity,
                                                       bookingRequestStateViewModel)
    }

    override fun bindViews() {

        if (isGuest) {
            navigationWidget.menu.removeItem(R.id.action_rides)
            navigationWidget.menu.removeItem(R.id.action_profile)
        }

        addressBarWidget.setJourneyInfo(journeyInfo)

        locateMeButton.setOnClickListener {
            if (isLocateMeEnabled(this)) {
                bookingMapWidget.locateUser()
            } else {
                ActivityCompat.requestPermissions(this,
                                                  arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                                                          Manifest.permission.ACCESS_COARSE_LOCATION),
                                                  MY_PERMISSIONS_REQUEST_LOCATION)
            }
        }

        lifecycle.apply {
            addObserver(bookingMapWidget)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    bookingMapWidget.locationPermissionGranted()
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest
                                    .permission.ACCESS_FINE_LOCATION)) {
                        showLocationLock()
                    }
                }
                return
            }
        }
    }

    override fun initialiseViewListeners() {
        bookingMapWidget.actions = this
        tripAllocationWidget.actions = this

        navigationWidget.setNavigationItemSelectedListener(navigationDrawerWidget)

        journeyDetailsStateViewModel.viewActions().observe(this, bindToAddressBarOutputs())
        bookingRequestStateViewModel.viewActions().observe(this, bindToBookingRequestOutputs())
    }

    private fun bindToAddressBarOutputs(): Observer<in AddressBarViewContract.AddressBarActions> {
        return Observer { actions ->
            when (actions) {
                is AddressBarViewContract.AddressBarActions.ShowAddressActivity ->
                    startActivityForResult(actions.intent, actions.addressCode)
            }
        }
    }

    private fun bindToBookingRequestOutputs(): Observer<in CheckoutViewContract.Action> {
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

    override fun selectAddress(intent: Intent, addressCode: Int) {
        startActivityForResult(intent, addressCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode == Activity.RESULT_OK && requestCode == REQ_CODE_BOOKING_REQUEST_ACTIVITY -> {
                if (data?.hasExtra(CheckoutActivity.BOOKING_CHECKOUT_PREBOOK_TRIP_INFO_KEY) == true) {
                    showPrebookConfirmationDialog(data)
                } else {
                    waitForTripAllocation()
                    tripAllocationWidget.onActivityResult(requestCode, resultCode, data)
                }
            }
            resultCode == RESULT_OK -> {
                when (requestCode) {
                    AddressCodes.PICKUP -> addressBarWidget.onActivityResult(requestCode, resultCode, data)
                    AddressCodes.DESTINATION -> {
                        addressBarWidget.onActivityResult(requestCode, resultCode, data)
                        startQuoteListActivity(restorePreviousData = false)
                    }
                }
            }
            resultCode == QuotesActivity.QUOTES_RESULT_OK -> {
                val pickup = data?.getParcelableExtra<LocationInfo>(QuotesActivity.QUOTES_PICKUP_ADDRESS)
                val destination = data?.getParcelableExtra<LocationInfo>(QuotesActivity.QUOTES_DROPOFF_ADDRESS)
                val date = data?.getSerializableExtra(QuotesActivity.QUOTES_SELECTED_DATE) as? DateTime

                pickup?.let {
                    addressBarWidget.setPickup(pickup, -1)
                }

                destination?.let {
                    addressBarWidget.setDestination(destination, -1)
                }

                date?.let {
                    addressBarWidget.setPrebookTime(date)
                }

                val passengerNumber = data?.getIntExtra(QuotesActivity.PASSENGER_NUMBER, 1)
                val luggage = data?.getIntExtra(QuotesActivity.LUGGAGE, 0)
                if(bookingMetadata == null)
                    bookingMetadata = HashMap()
                bookingMetadata?.put(QuotesActivity.PASSENGER_NUMBER, passengerNumber.toString())
                bookingMetadata?.put(QuotesActivity.LUGGAGE, luggage.toString())

                startCheckoutActivity(data)
            }
            resultCode == CheckoutActivity.BOOKING_CHECKOUT_CANCELLED || resultCode == CheckoutActivity.BOOKING_CHECKOUT_EXPIRED -> {
                startQuoteListActivity(
                    restorePreviousData = resultCode == CheckoutActivity.BOOKING_CHECKOUT_CANCELLED,
                    validityTimestamp = data?.getLongExtra(QUOTES_SELECTED_QUOTE_VALIDITY_TIMESTAMP, 0)
                )
            }
            resultCode == QUOTES_CANCELLED -> {
                val journeyDetails = data?.getParcelableExtra(Builder.EXTRA_JOURNEY_INFO) as JourneyDetails?
                journeyDetailsStateViewModel.process(
                    AddressBarViewContract.AddressBarEvent
                        .PrebookBookingEvent(
                            journeyDetails?.pickup,
                            journeyDetails?.destination,
                            journeyDetails?.date
                        )
                )
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        // if Webview is visible we hide it
        if (khWebView?.visibility == View.VISIBLE) {
            khWebView?.hide()
            return
        }
        // if destination set we clear it, close the quotes list and return
        if (journeyDetailsStateViewModel.currentState.destination != null) {
            journeyDetailsStateViewModel.process(AddressBarViewContract.AddressBarEvent
                                                        .DestinationAddressEvent(null))
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
        addressBarWidget.visibility = View.INVISIBLE
        toolbar.visibility = View.INVISIBLE
        navigationDrawerWidget.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        bookingMapWidget.centreMapToPickupPin()
    }

    private fun showPrebookConfirmationDialog(data: Intent?) {
        val quoteType = data?.getSerializableExtra(CheckoutActivity
                                                           .BOOKING_CHECKOUT_PREBOOK_QUOTE_TYPE_KEY) as QuoteType
        val tripInfo = data.getParcelableExtra<TripInfo>(CheckoutActivity
                                                                 .BOOKING_CHECKOUT_PREBOOK_TRIP_INFO_KEY)

        if (tripInfo != null) {
            journeyDetailsStateViewModel.process(AddressBarViewContract.AddressBarEvent.ResetJourneyDetailsEvent)

            val prebookConfirmationView = PrebookConfirmationView(this).apply {
                bind(quoteType, tripInfo)
            }
            prebookConfirmationView.actions = object : CheckoutViewContract.PrebookViewActions {
                override fun finishedBooking() {
                    journeyDetailsStateViewModel.process(AddressBarViewContract.AddressBarEvent.ResetJourneyDetailsEvent)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!isGuest) {
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
                startActivity(RidesActivity.Builder.builder.build(this))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBookingCancelledOrFinished() {
        addressBarWidget.visibility = View.VISIBLE
        toolbar.visibility = View.VISIBLE
        navigationDrawerWidget.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        journeyDetailsStateViewModel.process(AddressBarViewContract.AddressBarEvent.ResetJourneyDetailsEvent)
    }

    private fun startCheckoutActivity(data: Intent?) {
        this.quote = data?.getParcelableExtra(QUOTES_SELECTED_QUOTE_KEY)

        this.quote?.let { quote ->
            val builder = CheckoutActivity.Builder()
                .quote(quote)
                .outboundTripId(outboundTripId)
                .bookingMetadata(bookingMetadata)
                .journeyDetails(
                    JourneyDetails(
                        data?.getParcelableExtra(QuotesActivity.QUOTES_PICKUP_ADDRESS) ?: journeyDetailsStateViewModel.currentState.pickup,
                        data?.getParcelableExtra(QuotesActivity.QUOTES_DROPOFF_ADDRESS) ?: journeyDetailsStateViewModel.currentState.destination,
                        data?.getSerializableExtra(QuotesActivity.QUOTES_SELECTED_DATE) as? DateTime ?: journeyDetailsStateViewModel.currentState.date
                    )
                )

            passengerDetails?.let {
                builder.passengerDetails(it)
            }

            bookingComments?.let {
                builder.comments(it)
            }

            loyaltyInfo?.let {
                builder.loyaltyInfo(it)
            }

            val validityTimeStamp = data?.getLongExtra(
                QuotesActivity.QUOTES_SELECTED_QUOTE_VALIDITY_TIMESTAMP, 0
            )

            validityTimeStamp?.let {
                builder.validityDeadlineTimestamp(
                    validityTimeStamp
                )
            }

            startActivityForResult(builder.build(this), REQ_CODE_BOOKING_REQUEST_ACTIVITY)
        }
    }
    private fun startQuoteListActivity(restorePreviousData: Boolean, validityTimestamp: Long? = null) {
        val builder = QuotesActivity.Builder().restorePreviousData(restorePreviousData).bookingInfo(journeyDetailsStateViewModel.viewStates().value)
        validityTimestamp?.let {
            builder.validityTimestamp(validityTimestamp)
        }
        startActivityForResult(builder.build(this@BookingActivity), QUOTES_INFO_REQUEST_NUMBER)
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
         * By passing journey info into the Booking activity it will automatically prefill the origin
         * destination and date of the desired trip. This will only use the details available inside
         * the [JourneyInfo] object.
         */
        fun journeyInfo(journeyInfo: JourneyInfo): Builder {
            extrasBundle.putParcelable(EXTRA_JOURNEY_INFO, journeyInfo)
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
        fun buildForOnActivityResultCallback(context: Context): Intent = Intent(context, KarhooUISDK.Routing.booking).apply {
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
        private const val REQ_CODE_BRAINTREE = 301
        private const val REQ_CODE_BOOKING_REQUEST_ACTIVITY = 304
        private const val REQ_CODE_BRAINTREE_GUEST = 302
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 1001
        private const val NAVIGATION_ICON_DELAY = 100L
        private const val JOURNEY_INFO = "JOURNEY_INFO"
    }

}
