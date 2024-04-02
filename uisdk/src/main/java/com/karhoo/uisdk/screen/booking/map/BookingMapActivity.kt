package com.karhoo.uisdk.screen.booking.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
//import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.base.address.AddressCodes
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarMVP
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarView
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarViewContract
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import com.karhoo.uisdk.screen.booking.domain.address.JourneyInfo
import com.karhoo.uisdk.screen.booking.domain.quotes.KarhooAvailability
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity
import com.karhoo.uisdk.screen.rides.RidesActivity
import com.karhoo.uisdk.util.extension.isLocateMeEnabled
import com.karhoo.uisdk.util.extension.toSimpleLocationInfo
import org.joda.time.DateTime

class BookingMapActivity : BaseActivity(), AddressBarMVP.Actions, BookingMapMVP.Actions {

    override val layout: Int
        get() = R.layout.uisdk_activity_booking_map

    private val journeyDetailsStateViewModel: JourneyDetailsStateViewModel by lazy {
        ViewModelProvider(
            this
        ).get(JourneyDetailsStateViewModel::class.java)
    }

    private var tripDetails: TripInfo? =
        null // field can be removed if we remove usage of the BaseActivity "lifecycle"
    private var journeyInfo: JourneyInfo? = null
//    private var bookingMetadata: HashMap<String, String>? = null

    private var isGuest = false

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
//    private lateinit var navigationDrawerWidget: BookingDrawerView
    private lateinit var bookingMapWidget: BookingMapView
    private lateinit var bookingModeWidget: BookingModeView
    private var navigationHeaderIcon: ImageView? = null
    private lateinit var addressBarWidget: AddressBarView
//    private lateinit var tripAllocationWidget: TripAllocationView
//    private lateinit var navigationWidget: NavigationView
    private lateinit var locateMeButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.uisdk_activity_booking_map)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        if (KarhooUISDKConfigurationProvider.configuration.forceDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        window.allowEnterTransitionOverlap = true
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            if (it[JOURNEY_DETAILS] != null) {
                val journeyDetails = it[JOURNEY_DETAILS] as JourneyDetails
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

        supportActionBar?.setHomeActionContentDescription(getString(R.string.kh_uisdk_close_the_screen))

        if (KarhooUISDK.menuHandler == null) {
            supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                it.setHomeAsUpIndicator(R.drawable.uisdk_ic_close)
                it.title = ""
            }
        } else {
//            supportActionBar?.let { navigationDrawerWidget.setToggleToolbar(toolbar, it) }
        }

        bookingMapWidget.onCreate(
            savedInstanceState, this, journeyDetailsStateViewModel,
            tripDetails?.destination == null, journeyInfo != null
        )

//        bookingMetadata = KarhooUISDKConfigurationProvider.configuration.bookingMetadata()

        KarhooUISDK.analytics?.bookingScreenOpened()
        bookingModeWidget.callbackToStartQuoteList = { isPrebook ->
            val data = Intent()
            if(!isPrebook){
                journeyDetailsStateViewModel.currentState.date = null
            }
            data.putExtra(BookingMapActivity.JOURNEY_DETAILS, journeyDetailsStateViewModel.currentState)
            setResult(RESULT_OK, data)
            finish()
        }
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
//        tripDetails?.let {
//            if (it.origin != null && it.destination != null) {
//                startQuoteListActivity(false)
//            }
//        }
        tripDetails = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(JOURNEY_DETAILS, journeyDetailsStateViewModel.currentState)
        bookingMapWidget.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        bookingMapWidget.onLowMemory()
    }

    override fun handleExtras() {
        extras?.let { extras ->
            tripDetails = extras.get(BookingActivity.Builder.EXTRA_TRIP_DETAILS) as TripInfo?
            tripDetails?.let {
                journeyDetailsStateViewModel.process(
                    AddressBarViewContract.AddressBarEvent
                        .AsapBookingEvent(
                            it.origin?.toSimpleLocationInfo(),
                            it.destination?.toSimpleLocationInfo()
                        )
                )
            }
//            outboundTripId = extras.getString(BookingActivity.Builder.EXTRA_OUTBOUND_TRIP_ID, null)
//            val initialLocation = extras.getParcelable<Location>(BookingActivity.Builder.EXTRA_INITIAL_LOCATION)
//            initialLocation?.let {
//                bookingMapWidget.initialLocation = LatLng(it.latitude, it.longitude)
//            }
            journeyInfo = extras.getParcelable(BookingActivity.Builder.EXTRA_JOURNEY_INFO)
//            passengerDetails = extras.getParcelable(BookingActivity.Builder.EXTRA_PASSENGER_DETAILS)
//            bookingComments = extras.getString(BookingActivity.Builder.EXTRA_COMMENTS)
//            loyaltyInfo = extras.getParcelable(BookingActivity.Builder.EXTRA_LOYALTY_INFO)
//            val injectedBookingMetadata =
//                extras.getSerializable(BookingActivity.Builder.EXTRA_META) as? HashMap<String, String>
//            injectedBookingMetadata?.let {
//                bookingMetadata?.putAll(it)
//            }
        }
    }

    override fun initialiseViews() {
        toolbar = findViewById(R.id.toolbar)
//        navigationDrawerWidget = findViewById(R.id.navigationDrawerWidget)
        bookingMapWidget = findViewById(R.id.bookingMapWidget)
        bookingModeWidget = findViewById(R.id.bookingModeWidget)
        navigationHeaderIcon = findViewById(R.id.navigationHeaderIcon)
        addressBarWidget = findViewById(R.id.addressBarWidget)
//        tripAllocationWidget = findViewById(R.id.tripAllocationWidget)
//        navigationWidget = findViewById(R.id.navigationWidget)
        locateMeButton = findViewById(R.id.locateMeButton)

        isGuest = KarhooUISDKConfigurationProvider.isGuest()

        addressBarWidget.watchJourneyDetailsState(
            this@BookingMapActivity,
            journeyDetailsStateViewModel
        )
        bookingModeWidget.watchJourneyDetailsState(
            this@BookingMapActivity,
            journeyDetailsStateViewModel
        )
//        tripAllocationWidget.watchBookingRequestStatus(
//            this@BookingActivity,
//            bookingRequestStateViewModel
//        )
    }

    override fun bindViews() {

//        if (isGuest) {
//            navigationWidget.menu.removeItem(R.id.action_rides)
//            navigationWidget.menu.removeItem(R.id.action_profile)
//        }

        addressBarWidget.setJourneyInfo(journeyInfo)

        locateMeButton.setOnClickListener {
            if (isLocateMeEnabled(this)) {
                bookingMapWidget.locateUser()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
        }

        lifecycle.apply {
            addObserver(bookingMapWidget)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    bookingMapWidget.locationPermissionGranted()
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this, Manifest
                                .permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        showLocationLock()
                    }
                }
                return
            }
        }
    }

    override fun initialiseViewListeners() {
        bookingMapWidget.actions = this
//        tripAllocationWidget.actions = this

//        navigationWidget.setNavigationItemSelectedListener(navigationDrawerWidget)

        journeyDetailsStateViewModel.viewActions().observe(this, bindToAddressBarOutputs())
//        bookingRequestStateViewModel.viewActions().observe(this, bindToBookingRequestOutputs())
    }

    private var hasPickupCoverage: Boolean = false
    private var hasDestinationCoverage: Boolean = false

    private fun bindToAddressBarOutputs(): Observer<in AddressBarViewContract.AddressBarActions> {
        return Observer { actions ->
            when (actions) {
                is AddressBarViewContract.AddressBarActions.ShowAddressActivity -> {
                    startActivityForResult(actions.intent, actions.addressCode)
                }

                is AddressBarViewContract.AddressBarActions.AddressChanged -> {
                    if (actions.address == null) {
                        validateCoverage()
                        return@Observer
                    }

                    if (actions.addressCode == AddressCodes.PICKUP) {
                        KarhooAvailability.checkCoverage(actions.address) { hasCoverage ->
                            hasPickupCoverage = hasCoverage

                            validateCoverage()
                        }
                    }

                    if (actions.addressCode == AddressCodes.DESTINATION) {
                        KarhooAvailability.checkCoverage(actions.address) { hasCoverage ->
                            hasDestinationCoverage = hasCoverage

                            validateCoverage()
                        }
                    }
                }
            }
        }
    }

    private fun validateCoverage() {
        if (journeyDetailsStateViewModel.currentState.pickup != null &&
            journeyDetailsStateViewModel.currentState.destination != null
        ) {

            val hasCoverage = hasPickupCoverage || hasDestinationCoverage

            bookingModeWidget.enableNowButton(hasCoverage)
            bookingModeWidget.showNoCoverageText(hasCoverage)
            bookingModeWidget.enableScheduleButton(true)
            bookingMapWidget.updateMapViewForQuotesListVisibilityExpanded(bookingModeWidget.height)

            bookingModeWidget.show(true)
        } else {
            bookingMapWidget.updateMapViewForQuotesListVisibilityCollapsed()

            bookingModeWidget.show(false)
        }
    }

    override fun selectAddress(intent: Intent, addressCode: Int) {
        startActivityForResult(intent, addressCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
//            resultCode == Activity.RESULT_OK && requestCode == REQ_CODE_BOOKING_REQUEST_ACTIVITY -> {
//                if (data?.hasExtra(CheckoutActivity.BOOKING_CHECKOUT_PREBOOK_SKIP_RIDE_DETAILS_KEY) == true) {
//                    journeyDetailsStateViewModel.process(AddressBarViewContract.AddressBarEvent.ResetJourneyDetailsEvent)
//                } else if (data?.hasExtra(CheckoutActivity.BOOKING_CHECKOUT_PREBOOK_TRIP_INFO_KEY) == true) {
//                    journeyDetailsStateViewModel.process(AddressBarViewContract.AddressBarEvent.ResetJourneyDetailsEvent)
//                    bookingMapWidget.clearMarkers()
//                    bookingModeWidget.show(show = false)
//                    if(isLocateMeEnabled(this)){
//                        bookingMapWidget.locateUser()
//                    }
//                    val tripInfo =
//                        data.getParcelableExtra<TripInfo>(CheckoutActivity.BOOKING_CHECKOUT_PREBOOK_TRIP_INFO_KEY)
//
//                    tripInfo?.let {
//                        ContextCompat.startActivity(this, RideDetailActivity.Builder.newBuilder().trip(tripInfo).build(this), null)
//                    }
//                } else {
//                    waitForTripAllocation()
//                    tripAllocationWidget.onActivityResult(requestCode, resultCode, data)
//                }
//            }
            resultCode == RESULT_OK -> {
                when (requestCode) {
                    AddressCodes.PICKUP -> addressBarWidget.onActivityResult(requestCode, resultCode, data)
                    AddressCodes.DESTINATION -> addressBarWidget.onActivityResult(requestCode, resultCode, data)
                }
                bookingMapWidget.setTopPadding(addressBarWidget.height)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun extractJourneyDetails(data: Intent?): JourneyDetails {
        return JourneyDetails(
            data?.getParcelableExtra(QuotesActivity.QUOTES_PICKUP_ADDRESS)
                ?: journeyDetailsStateViewModel.currentState.pickup,
            data?.getParcelableExtra(QuotesActivity.QUOTES_DROPOFF_ADDRESS)
                ?: journeyDetailsStateViewModel.currentState.destination,
            data?.getSerializableExtra(QuotesActivity.QUOTES_SELECTED_DATE) as? DateTime?
        )
    }

    override fun onBackPressed() {
        // if Webview is visible we hide it
        if (khWebView?.visibility == View.VISIBLE) {
            khWebView?.hide()
            return
        }
        // if destination set we clear it, close the quotes list and return
        if (journeyDetailsStateViewModel.currentState.destination != null) {
            journeyDetailsStateViewModel.process(
                AddressBarViewContract.AddressBarEvent
                    .DestinationAddressEvent(null)
            )
            return
        }
//        if (navigationDrawerWidget.closeIfOpen()) {
//            super.onBackPressed()
//        }
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
                startActivity(
                    RidesActivity.Builder.builder
                        .build(this)
                )
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
         * By passing journey info into the Booking activity it will automatically prefill the origin
         * destination and date of the desired trip. This will only use the details available inside
         * the [JourneyInfo] object.
         */
        fun journeyInfo(journeyInfo: JourneyInfo?): Builder {
            journeyInfo?.let {
                extrasBundle.putParcelable(EXTRA_JOURNEY_INFO, journeyInfo)
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

        /**
         * Returns a launchable Intent to the configured booking activity with the given
         * builder parameters in the extras bundle
         */
        fun build(context: Context): Intent = Intent(context, KarhooUISDK.Routing.bookingMap).apply {
            putExtras(extrasBundle)
        }

        /**
         * Returns a launchable Intent to the configure booking activity with the given
         * builder parameters in the extras bundle, when the intent is being started
         * with startActivityForResult()
         */
        fun buildForOnActivityResultCallback(context: Context): Intent =
            Intent(context, KarhooUISDK.Routing.bookingMap).apply {
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
        const val REQ_CODE_BOOKING_MAP_ACTIVITY = 304
        private const val REQ_CODE_BRAINTREE_GUEST = 302
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 1001
        private const val NAVIGATION_ICON_DELAY = 100L
        const val JOURNEY_DETAILS = "JOURNEY_DETAILS"
    }
}
