package com.karhoo.uisdk.screen.rideplanning

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.base.address.AddressCodes
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarViewContract
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStateViewModel
import com.karhoo.uisdk.screen.booking.domain.quotes.KarhooAvailability
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity
import com.karhoo.uisdk.screen.rideplanning.RidePlanningCoordinator.Companion.REQ_CODE_BOOKING_REQUEST_ACTIVITY
import com.karhoo.uisdk.util.extension.hasLocationPermission
import com.karhoo.uisdk.util.extension.toSimpleLocationInfo
import org.joda.time.DateTime

class RidePlanningPresenter : RidePlanningContract.Presenter {
    private lateinit var view: RidePlanningContract.View
    private var hasPickupCoverage: Boolean = false
    private var hasDestinationCoverage: Boolean = false
    private lateinit var ridePlanningCoordinator: RidePlanningCoordinator

    override fun setup(view: RidePlanningContract.View, bundle: Bundle?, ridePlanningCoordinator: RidePlanningCoordinator) {
        this.view = view
        this.ridePlanningCoordinator = ridePlanningCoordinator

        parseSavedInstanceState(bundle)

        RidePlanningStorage.bookingMetadata =  KarhooUISDKConfigurationProvider.configuration.bookingMetadata()

        KarhooUISDK.analytics?.bookingScreenOpened()

        RidePlanningStorage.tripDetails?.let {
            if (it.origin != null && it.destination != null) {
                ridePlanningCoordinator.startQuoteListActivity(false, null, RidePlanningStorage.journeyDetailsStateViewModel.viewStates().value)
            }
        }

        RidePlanningStorage.tripDetails = null

        RidePlanningStorage.journeyDetailsStateViewModel = ViewModelProvider(
            view.getViewModelStoreOwner()
        ).get(JourneyDetailsStateViewModel::class.java)
        RidePlanningStorage.bookingRequestStateViewModel = ViewModelProvider(
            view.getViewModelStoreOwner()
        ).get(BookingRequestStateViewModel::class.java)
    }

    private fun parseSavedInstanceState(bundle: Bundle?) {
        bundle?.let {
            if (it[JOURNEY_INFO] != null) {
                val journeyDetails = it[JOURNEY_INFO] as JourneyDetails
                RidePlanningStorage.journeyDetailsStateViewModel.process(
                    AddressBarViewContract.AddressBarEvent
                        .PrebookBookingEvent(
                            journeyDetails.pickup,
                            journeyDetails.destination,
                            journeyDetails.date
                        )
                )
            }
        }
    }

    override fun saveInstanceState(bundle: Bundle) {
        bundle.putParcelable(JOURNEY_INFO, RidePlanningStorage.journeyDetailsStateViewModel.currentState)
    }

    override fun parseExtrasBundle(extras: Bundle?) {
        if(extras != null) {
            RidePlanningStorage.tripDetails = extras.get(BookingActivity.Builder.EXTRA_TRIP_DETAILS) as TripInfo?
            RidePlanningStorage.tripDetails?.let {
                RidePlanningStorage.journeyDetailsStateViewModel.process(
                    AddressBarViewContract.AddressBarEvent
                        .AsapBookingEvent(
                            it.origin?.toSimpleLocationInfo(),
                            it.destination?.toSimpleLocationInfo()
                        )
                )
            }
            RidePlanningStorage.outboundTripId = extras.getString(BookingActivity.Builder.EXTRA_OUTBOUND_TRIP_ID, null)

            val initialLocation = extras.getParcelable<Location>(BookingActivity.Builder.EXTRA_INITIAL_LOCATION)
            initialLocation?.let {
                view.setMapLocation(LatLng(it.latitude, it.longitude))
            }

            RidePlanningStorage.journeyInfo = extras.getParcelable(BookingActivity.Builder.EXTRA_JOURNEY_INFO)
            RidePlanningStorage.passengerDetails = extras.getParcelable(BookingActivity.Builder.EXTRA_PASSENGER_DETAILS)
            RidePlanningStorage.bookingComments = extras.getString(BookingActivity.Builder.EXTRA_COMMENTS)
            RidePlanningStorage.loyaltyInfo = extras.getParcelable(BookingActivity.Builder.EXTRA_LOYALTY_INFO)

            val injectedBookingMetadata = extras.getSerializable(BookingActivity.Builder.EXTRA_META) as? HashMap<String, String>
            injectedBookingMetadata?.let {
                RidePlanningStorage.bookingMetadata?.putAll(it)
            }
        }
    }

    override fun hasLocationPermissions(context: Context): Boolean {
        return hasLocationPermission(context)
    }

    override fun requestLocationPermissions(context: Context) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }

    override fun onReceivedPermissionsResult(
        context: Context,
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ): Boolean {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                   return true
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        return false
                    }
                }
                return false
            }
        }

        return false;
    }

    override fun initializeListeners() {
        RidePlanningStorage.journeyDetailsStateViewModel.viewActions().observe(view.getViewModelStoreOwner() as LifecycleOwner, bindToAddressBarOutputs())
        RidePlanningStorage.bookingRequestStateViewModel.viewActions().observe(view.getViewModelStoreOwner() as LifecycleOwner, view.checkoutObserver)
    }

    override fun parseDataFromActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode == Activity.RESULT_OK && requestCode == REQ_CODE_BOOKING_REQUEST_ACTIVITY -> {
                if (data?.hasExtra(CheckoutActivity.BOOKING_CHECKOUT_PREBOOK_SKIP_RIDE_DETAILS_KEY) == true) {
                    RidePlanningStorage.journeyDetailsStateViewModel.process(AddressBarViewContract.AddressBarEvent.ResetJourneyDetailsEvent)
                } else if (data?.hasExtra(CheckoutActivity.BOOKING_CHECKOUT_PREBOOK_TRIP_INFO_KEY) == true) {
                    RidePlanningStorage.journeyDetailsStateViewModel.process(AddressBarViewContract.AddressBarEvent.ResetJourneyDetailsEvent)


                    val tripInfo =
                        data.getParcelableExtra<TripInfo>(CheckoutActivity.BOOKING_CHECKOUT_PREBOOK_TRIP_INFO_KEY)

                    tripInfo?.let {
                        ridePlanningCoordinator.startRideDetailActivity(it)
                    }
                } else {
                    val tripInfo = data?.getParcelableExtra<TripInfo>(CheckoutActivity.BOOKING_CHECKOUT_TRIP_INFO_KEY)

                    tripInfo?.let {
                        view.startTripAllocation(tripInfo)
                    }
                }
            }
            resultCode == AppCompatActivity.RESULT_OK -> {
                when (requestCode) {
                    AddressCodes.PICKUP,
                    AddressCodes.DESTINATION -> view.setAddresses(requestCode, resultCode, data)
                }
            }
            resultCode == QuotesActivity.QUOTES_RESULT_OK -> {
                parseDataFromIntent(data)
                ridePlanningCoordinator.startCheckoutActivity(data, extractJourneyDetails(data))
            }
            resultCode == CheckoutActivity.BOOKING_CHECKOUT_CANCELLED || resultCode == CheckoutActivity.BOOKING_CHECKOUT_EXPIRED -> {
                ridePlanningCoordinator.startQuoteListActivity(
                    restorePreviousData = resultCode == CheckoutActivity.BOOKING_CHECKOUT_CANCELLED,
                    validityTimestamp = data?.getLongExtra(
                        QuotesActivity.QUOTES_SELECTED_QUOTE_VALIDITY_TIMESTAMP,
                        0
                    ),
                    restoredJourneyData = extractJourneyDetails(data)
                )
            }
            resultCode == QuotesActivity.QUOTES_CANCELLED -> {
                val journeyDetails =
                    data?.getParcelableExtra(BookingActivity.Builder.EXTRA_JOURNEY_INFO) as JourneyDetails?

                RidePlanningStorage.journeyDetailsStateViewModel.process(
                    AddressBarViewContract.AddressBarEvent
                        .PrebookBookingEvent(
                            journeyDetails?.pickup,
                            journeyDetails?.destination,
                            journeyDetails?.date
                        )
                )
            }
        }
    }

    override fun onBookingModeSelected(isPrebook: Boolean) {
        RidePlanningStorage.journeyDetailsStateViewModel.let {
            if (it.currentState.pickup != null && it.currentState.destination != null) {
                if (!isPrebook) {
                    it.currentState.date = null
                }
                ridePlanningCoordinator.startQuoteListActivity(false, null, RidePlanningStorage.journeyDetailsStateViewModel.viewStates().value)
            }
        }
    }
    private fun parseDataFromIntent(data: Intent?) {
        val pickup = data?.getParcelableExtra<LocationInfo>(QuotesActivity.QUOTES_PICKUP_ADDRESS)
        val destination =
            data?.getParcelableExtra<LocationInfo>(QuotesActivity.QUOTES_DROPOFF_ADDRESS)
        val date = data?.getSerializableExtra(QuotesActivity.QUOTES_SELECTED_DATE) as? DateTime

        view.setAddresses(pickup, destination, date)

        val passengerNumber = data?.getIntExtra(QuotesActivity.PASSENGER_NUMBER, 1)
        val luggage = data?.getIntExtra(QuotesActivity.LUGGAGE, 0)
        if (RidePlanningStorage.bookingMetadata == null) {
            RidePlanningStorage.bookingMetadata = HashMap()
        }

        RidePlanningStorage.bookingMetadata?.put(QuotesActivity.PASSENGER_NUMBER, passengerNumber.toString())
        RidePlanningStorage.bookingMetadata?.put(QuotesActivity.LUGGAGE, luggage.toString())
    }

    private fun extractJourneyDetails(data: Intent?): JourneyDetails {
        return JourneyDetails(
            data?.getParcelableExtra(QuotesActivity.QUOTES_PICKUP_ADDRESS)
                ?: RidePlanningStorage.journeyDetailsStateViewModel.currentState.pickup,
            data?.getParcelableExtra(QuotesActivity.QUOTES_DROPOFF_ADDRESS)
                ?: RidePlanningStorage.journeyDetailsStateViewModel.currentState.destination,
            data?.getSerializableExtra(QuotesActivity.QUOTES_SELECTED_DATE) as? DateTime?
        )
    }

    private fun bindToAddressBarOutputs(): Observer<in AddressBarViewContract.AddressBarActions> {
        return Observer { actions ->

            when (actions) {
                is AddressBarViewContract.AddressBarActions.ShowAddressActivity -> {
                    ridePlanningCoordinator.startActivityForResult(actions.intent, actions.addressCode)
                }

                is AddressBarViewContract.AddressBarActions.AddressChanged -> {
                    val hasAddresses = RidePlanningStorage.journeyDetailsStateViewModel.currentState.pickup != null &&
                            RidePlanningStorage.journeyDetailsStateViewModel.currentState.destination != null

                    if (actions.address == null) {
                        view.validateCoverage(hasAddresses, hasPickupCoverage || hasDestinationCoverage)
                        return@Observer
                    }

                    if (actions.addressCode == AddressCodes.PICKUP) {
                        KarhooAvailability.checkCoverage(actions.address) { hasCoverage ->
                            hasPickupCoverage = hasCoverage

                            view.validateCoverage(hasAddresses, hasPickupCoverage || hasDestinationCoverage)
                        }
                    }

                    if (actions.addressCode == AddressCodes.DESTINATION) {
                        KarhooAvailability.checkCoverage(actions.address) { hasCoverage ->
                            hasDestinationCoverage = hasCoverage

                            view.validateCoverage(hasAddresses, hasPickupCoverage || hasDestinationCoverage)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val JOURNEY_INFO = "JOURNEY_INFO"
        const val MY_PERMISSIONS_REQUEST_LOCATION = 1001

    }
}
