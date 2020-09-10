package com.karhoo.uisdk.screen.booking

import android.Manifest
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
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.QuoteV2
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.base.address.AddressCodes
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarMVP
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarViewContract
import com.karhoo.uisdk.screen.booking.booking.bookingrequest.BookingRequestViewContract
import com.karhoo.uisdk.screen.booking.booking.bookingrequest.BookingRequestMVP
import com.karhoo.uisdk.screen.booking.booking.supplier.BookingSupplierViewContract
import com.karhoo.uisdk.screen.booking.booking.supplier.BookingSupplierViewModel
import com.karhoo.uisdk.screen.booking.booking.tripallocation.TripAllocationMVP
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.address.JourneyInfo
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStateViewModel
import com.karhoo.uisdk.screen.booking.domain.supplier.AvailabilityProvider
import com.karhoo.uisdk.screen.booking.domain.supplier.KarhooAvailability
import com.karhoo.uisdk.screen.booking.domain.supplier.LiveFleetsViewModel
import com.karhoo.uisdk.screen.booking.map.BookingMapMVP
import com.karhoo.uisdk.screen.booking.supplier.category.CategoriesViewModel
import com.karhoo.uisdk.screen.rides.RidesActivity
import com.karhoo.uisdk.util.extension.isLocateMeEnabled
import com.karhoo.uisdk.util.extension.toSimpleLocationInfo
import kotlinx.android.synthetic.main.uisdk_activity_base.khWebView
import kotlinx.android.synthetic.main.uisdk_activity_booking_content.addressBarWidget
import kotlinx.android.synthetic.main.uisdk_activity_booking_content.bookingMapWidget
import kotlinx.android.synthetic.main.uisdk_activity_booking_content.bookingRequestWidget
import kotlinx.android.synthetic.main.uisdk_activity_booking_content.supplierListWidget
import kotlinx.android.synthetic.main.uisdk_activity_booking_content.toolbar
import kotlinx.android.synthetic.main.uisdk_activity_booking_content.tripAllocationWidget
import kotlinx.android.synthetic.main.uisdk_activity_booking_main.navigationDrawerWidget
import kotlinx.android.synthetic.main.uisdk_activity_booking_main.navigationWidget
import kotlinx.android.synthetic.main.uisdk_booking_request.bookingRequestCommentsWidget
import kotlinx.android.synthetic.main.uisdk_booking_request.bookingRequestPassengerDetailsWidget
import kotlinx.android.synthetic.main.uisdk_nav_header_main.navigationHeaderIcon
import kotlinx.android.synthetic.main.uisdk_view_supplier.locateMeButton

class BookingActivity : BaseActivity(), AddressBarMVP.Actions, BookingMapMVP.Actions,
                        TripAllocationMVP.Actions, BookingRequestMVP.Actions {

    private val categoriesViewModel: CategoriesViewModel by lazy { ViewModelProvider(this).get(CategoriesViewModel::class.java) }
    private val bookingStatusStateViewModel: BookingStatusStateViewModel by lazy { ViewModelProvider(this).get(BookingStatusStateViewModel::class.java) }
    private val bookingRequestStateViewModel: BookingRequestStateViewModel by lazy { ViewModelProvider(this).get(BookingRequestStateViewModel::class.java) }
    private val bookingSupplierViewModel: BookingSupplierViewModel by lazy { ViewModelProvider(this).get(BookingSupplierViewModel::class.java) }
    private val liveFleetsViewModel: LiveFleetsViewModel by lazy { ViewModelProvider(this).get(LiveFleetsViewModel::class.java) }

    private var availabilityProvider: AvailabilityProvider? = null
    private var quote: QuoteV2? = null

    ////////////////////////////////////////////
    private var tripDetails: TripInfo? = null // field can be removed if we remove usage of the BaseActivity "lifecycle"
    private var outboundTripId: String? = null // field can be removed if we remove usage of the BaseActivity "lifecycle"
    private var journeyInfo: JourneyInfo? = null
    private var passengerDetails: PassengerDetails? = null
    private var bookingComments: String? = ""

    ////////////////////////////////////////////
    private var isGuest = KarhooUISDKConfigurationProvider.isGuest()

    override val layout: Int
        get() = R.layout.uisdk_activity_booking_main

    override fun onCreate(savedInstanceState: Bundle?) {
        window.allowEnterTransitionOverlap = true
        super.onCreate(savedInstanceState)

        if (callingActivity != null) {
            KarhooUISDK.analytics?.bookingWithCallbackOpened()
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

        bookingMapWidget.onCreate(savedInstanceState, this, bookingStatusStateViewModel,
                                  tripDetails?.destination == null, journeyInfo != null)
    }

    override fun onResume() {
        super.onResume()
        if (tripAllocationWidget.visibility != View.VISIBLE) {
            initAvailability()
        }
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

    private fun initAvailability() {
        availabilityProvider?.cleanup()
        availabilityProvider = KarhooAvailability(KarhooApi.quotesService,
                                                  KarhooUISDK.analytics, categoriesViewModel, liveFleetsViewModel,
                                                  bookingStatusStateViewModel, this).apply {
            setErrorView(this@BookingActivity)
            setAllCategory(getString(R.string.all_category))
            supplierListWidget.bindAvailability(this)
        }
    }

    private fun setWatchers() {
        addressBarWidget.bindTripToView(tripDetails)
        tripDetails = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        bookingMapWidget.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        bookingMapWidget.onLowMemory()
    }

    override fun onStop() {
        availabilityProvider?.cleanup()
        super.onStop()
    }

    override fun handleExtras() {
        extras?.let { extras ->
            tripDetails = extras.get(Builder.EXTRA_TRIP_DETAILS) as TripInfo?
            tripDetails?.let {
                bookingStatusStateViewModel.process(AddressBarViewContract.AddressBarEvent
                                                            .AsapBookingEvent(it.origin?.toSimpleLocationInfo(), it.destination?.toSimpleLocationInfo()))
            }
            outboundTripId = extras.getString(Builder.EXTRA_OUTBOUND_TRIP_ID, null)
            val initialLocation = extras.getParcelable<Location>(Builder.EXTRA_INITIAL_LOCATION)
            initialLocation?.let { bookingMapWidget.initialLocation = LatLng(it.latitude, it.longitude) }
            journeyInfo = extras.getParcelable(Builder.EXTRA_JOURNEY_INFO)
            passengerDetails = extras.getParcelable(Builder.EXTRA_PASSENGER_DETAILS)
            bookingComments = extras.getString(Builder.EXTRA_COMMENTS)
            bookingComments = ""
        }
    }

    override fun initialiseViews() {
        addressBarWidget.watchBookingStatusState(this@BookingActivity, bookingStatusStateViewModel)
        tripAllocationWidget.watchBookingRequestStatus(this@BookingActivity,
                                                       bookingRequestStateViewModel)
    }

    override fun bindViews() {

        if (isGuest) {
            navigationWidget.menu.removeItem(R.id.action_rides)
            navigationWidget.menu.removeItem(R.id.action_profile)
        }

        supplierListWidget.bindViewToData(this@BookingActivity, bookingStatusStateViewModel,
                                          categoriesViewModel, liveFleetsViewModel, bookingSupplierViewModel)
        bookingRequestWidget.apply {
            bindViewToBookingStatus(this@BookingActivity, bookingStatusStateViewModel)
            bindViewToBookingRequest(this@BookingActivity, bookingRequestStateViewModel)
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

        passengerDetails?.let {
            bookingRequestPassengerDetailsWidget.setPassengerDetails(it)
        }

        bookingComments?.let {
            bookingRequestCommentsWidget.setBookingOptionalInfo(it)
        }

        lifecycle.apply {
            addObserver(bookingMapWidget)
            addObserver(bookingRequestWidget)
        }
    }

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
        bookingRequestWidget.actions = this
        bookingMapWidget.actions = this
        tripAllocationWidget.actions = this

        navigationWidget.setNavigationItemSelectedListener(navigationDrawerWidget)

        bookingStatusStateViewModel.viewActions().observe(this, bindToAddressBarOutputs())
        bookingRequestStateViewModel.viewActions().observe(this, bindToBookingRequestOutputs())
        bookingSupplierViewModel.viewActions().observe(this, bindToBookingSupplierOutputs())
    }

    private fun bindToAddressBarOutputs(): Observer<in AddressBarViewContract.AddressBarActions> {
        return Observer { actions ->
            when (actions) {
                is AddressBarViewContract.AddressBarActions.ShowAddressActivity ->
                    startActivityForResult(actions.intent, actions.addressCode)
            }
        }
    }

    private fun bindToBookingRequestOutputs(): Observer<in BookingRequestViewContract.BookingRequestAction> {
        return Observer { actions ->
            when (actions) {
                is BookingRequestViewContract.BookingRequestAction.WaitForTripAllocation ->
                    waitForTripAllocation()
                is BookingRequestViewContract.BookingRequestAction.HandleBookingError ->
                    showErrorDialog(actions.stringId)
            }
        }
    }

    private fun bindToBookingSupplierOutputs(): Observer<in BookingSupplierViewContract.BookingSupplierAction> {
        return Observer { actions ->
            when (actions) {
                is BookingSupplierViewContract.BookingSupplierAction.ShowError ->
                    showSnackbar(actions.snackbarConfig)
                is BookingSupplierViewContract.BookingSupplierAction.HideError -> dismissSnackbar()
                is BookingSupplierViewContract.BookingSupplierAction.UpdateViewForSupplierListVisibilityChange ->
                    updateMapViewForSupplierListVisibilityChange(actions.isVisible)
                is BookingSupplierViewContract.BookingSupplierAction.ShowBookingRequest -> {
                    this.quote = actions.quote
                    bookingRequestWidget.showBookingRequest(actions.quote, outboundTripId)
                }
            }
        }
    }

    override fun selectAddress(intent: Intent, addressCode: Int) {
        startActivityForResult(intent, addressCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_CODE_BRAINTREE || requestCode == REQ_CODE_BRAINTREE_GUEST) {
            bookingRequestWidget.onActivityResult(requestCode, resultCode, data)
        } else if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                AddressCodes.PICKUP -> addressBarWidget.onActivityResult(requestCode, resultCode, data)
                AddressCodes.DESTINATION -> addressBarWidget.onActivityResult(requestCode, resultCode, data)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (navigationDrawerWidget.closeIfOpen()) {
            super.onBackPressed()
        }
    }

    override fun showWebView(url: String?) {
        url?.let {
            khWebView?.show(url = it)
        }
    }

    private fun waitForTripAllocation() {
        supplierListWidget.hideList()
        availabilityProvider?.cleanup()
        addressBarWidget.visibility = View.INVISIBLE
        bookingRequestWidget.visibility = View.INVISIBLE
        toolbar.visibility = View.INVISIBLE
        navigationDrawerWidget.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        bookingMapWidget.centreMapToPickupPin()
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
        initAvailability()
        availabilityProvider?.let { supplierListWidget.bindAvailability(it) }
        addressBarWidget.visibility = View.VISIBLE
        toolbar.visibility = View.VISIBLE
        navigationDrawerWidget.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        bookingMapWidget.zoomMapToOriginAndDestination()
    }

    override fun dismissSnackbar() {
        supplierListWidget.setSupplierListVisibility()
        super.dismissSnackbar()
    }

    private fun updateMapViewForSupplierListVisibilityChange(isVisible: Boolean) {
        if (isVisible) {
            bookingMapWidget.setDefaultPadding()
        } else {
            bookingMapWidget.setNoBottomPadding()
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

            val builder: Builder
                get() = Builder()
        }
    }

    companion object {
        private const val REQ_CODE_BRAINTREE = 301
        private const val REQ_CODE_BRAINTREE_GUEST = 302
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 1001
        private const val NAVIGATION_ICON_DELAY = 100L
    }

}
