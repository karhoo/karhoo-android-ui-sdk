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
import com.karhoo.sdk.api.model.Quote
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
import com.karhoo.uisdk.screen.booking.booking.payment.adyen.AdyenPaymentView.Companion.REQ_CODE_ADYEN
import com.karhoo.uisdk.screen.booking.booking.quotes.BookingQuotesViewContract
import com.karhoo.uisdk.screen.booking.booking.quotes.BookingQuotesViewModel
import com.karhoo.uisdk.screen.booking.booking.tripallocation.TripAllocationMVP
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.address.JourneyInfo
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStateViewModel
import com.karhoo.uisdk.screen.booking.map.BookingMapMVP
import com.karhoo.uisdk.screen.rides.RidesActivity
import com.karhoo.uisdk.util.extension.isLocateMeEnabled
import com.karhoo.uisdk.util.extension.toSimpleLocationInfo
import kotlinx.android.synthetic.main.uisdk_activity_base.khWebView
import kotlinx.android.synthetic.main.uisdk_activity_booking_content.addressBarWidget
import kotlinx.android.synthetic.main.uisdk_activity_booking_content.bookingMapWidget
import kotlinx.android.synthetic.main.uisdk_activity_booking_content.bookingRequestWidget
import kotlinx.android.synthetic.main.uisdk_activity_booking_content.quotesListWidget
import kotlinx.android.synthetic.main.uisdk_activity_booking_content.toolbar
import kotlinx.android.synthetic.main.uisdk_activity_booking_content.tripAllocationWidget
import kotlinx.android.synthetic.main.uisdk_activity_booking_main.navigationDrawerWidget
import kotlinx.android.synthetic.main.uisdk_activity_booking_main.navigationWidget
import kotlinx.android.synthetic.main.uisdk_booking_request.bookingRequestCommentsWidget
import kotlinx.android.synthetic.main.uisdk_booking_request.bookingRequestPassengerDetailsWidget
import kotlinx.android.synthetic.main.uisdk_nav_header_main.navigationHeaderIcon
import kotlinx.android.synthetic.main.uisdk_view_booking_map.locateMeButton

class BookingActivity : BaseActivity(), AddressBarMVP.Actions, BookingMapMVP.Actions,
                        TripAllocationMVP.Actions {

    private val bookingStatusStateViewModel: BookingStatusStateViewModel by lazy { ViewModelProvider(this).get(BookingStatusStateViewModel::class.java) }
    private val bookingRequestStateViewModel: BookingRequestStateViewModel by lazy { ViewModelProvider(this).get(BookingRequestStateViewModel::class.java) }
    private val bookingQuotesViewModel: BookingQuotesViewModel by lazy { ViewModelProvider(this).get(BookingQuotesViewModel::class.java) }

    private var quote: Quote? = null

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
            quotesListWidget.initAvailability(this)
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
        quotesListWidget?.cleanup()
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

        quotesListWidget.bindViewToData(this@BookingActivity, bookingStatusStateViewModel, bookingQuotesViewModel)
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
        bookingMapWidget.actions = this
        tripAllocationWidget.actions = this

        navigationWidget.setNavigationItemSelectedListener(navigationDrawerWidget)

        bookingStatusStateViewModel.viewActions().observe(this, bindToAddressBarOutputs())
        bookingRequestStateViewModel.viewActions().observe(this, bindToBookingRequestOutputs())
        bookingQuotesViewModel.viewActions().observe(this, bindToBookingQuoteOutputs())
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
                is BookingRequestViewContract.BookingRequestAction.ShowTermsAndConditions ->
                    showWebView(actions.url)
                is BookingRequestViewContract.BookingRequestAction.WaitForTripAllocation ->
                    waitForTripAllocation()
                is BookingRequestViewContract.BookingRequestAction.HandleBookingError ->
                    showErrorDialog(actions.stringId, actions.karhooError)
            }
        }
    }

    private fun bindToBookingQuoteOutputs(): Observer<in BookingQuotesViewContract.BookingQuotesAction> {
        return Observer { actions ->
            when (actions) {
                is BookingQuotesViewContract.BookingQuotesAction.ShowError ->
                    showSnackbar(actions.snackbarConfig)
                is BookingQuotesViewContract.BookingQuotesAction.HideError -> dismissSnackbar()
                is BookingQuotesViewContract.BookingQuotesAction.UpdateViewForQuotesListVisibilityChange ->
                    updateMapViewForQuoteListVisibilityChange(actions.isVisible)
                is BookingQuotesViewContract.BookingQuotesAction.UpdateViewForQuotesListCollapsed ->
                    bookingMapWidget.updateMapViewForQuotesListVisibilityCollapsed()
                is BookingQuotesViewContract.BookingQuotesAction.UpdateViewForQuotesListExpanded ->
                    bookingMapWidget.updateMapViewForQuotesListVisibilityExpanded()
                is BookingQuotesViewContract.BookingQuotesAction.ShowBookingRequest -> {
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
        if (requestCode == REQ_CODE_BRAINTREE || requestCode == REQ_CODE_BRAINTREE_GUEST ||
                requestCode == REQ_CODE_ADYEN) {
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

    private fun showWebView(url: String) {
        khWebView?.show(url)
    }

    private fun waitForTripAllocation() {
        quotesListWidget.hideList()
        quotesListWidget?.cleanup()
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
        quotesListWidget.initAvailability(this)
        addressBarWidget.visibility = View.VISIBLE
        toolbar.visibility = View.VISIBLE
        navigationDrawerWidget.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        bookingMapWidget.zoomMapToOriginAndDestination()
    }

    override fun dismissSnackbar() {
        quotesListWidget.setQuotesListVisibility()
        super.dismissSnackbar()
    }

    private fun updateMapViewForQuoteListVisibilityChange(isVisible: Boolean) {
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
