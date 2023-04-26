package com.karhoo.uisdk.screen.rideplanning

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import com.google.android.gms.maps.model.LatLng
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.checkout.component.views.CheckoutViewContract
import com.karhoo.uisdk.screen.booking.checkout.tripallocation.TripAllocationContract
import com.karhoo.uisdk.screen.booking.map.BookingMapMVP
import kotlinx.android.synthetic.main.uisdk_ride_planning_view.view.*
import kotlinx.android.synthetic.main.uisdk_view_booking_map.view.*
import org.joda.time.DateTime

class RidePlanningView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), RidePlanningContract.View, TripAllocationContract.Actions {

    private lateinit var presenter: RidePlanningContract.Presenter
    private lateinit var viewModelStoreOwner: ViewModelStoreOwner
    override lateinit var checkoutObserver: Observer<in CheckoutViewContract.Action>
    override lateinit var handleNavigationVisiblityCallback: (toolbarVisibility: Int, navigationMode: Int) -> Unit

    init {
        inflate(context, R.layout.uisdk_ride_planning_view, this)
    }

    override fun setup(
        presenter: RidePlanningContract.Presenter?,
        savedInstanceState: Bundle?,
        viewModelStoreOwner: ViewModelStoreOwner
    ) {
        this.viewModelStoreOwner = viewModelStoreOwner
        this.presenter = presenter ?: RidePlanningPresenter()
        this.presenter.setup(
            this,
            savedInstanceState,
            RidePlanningCoordinator(viewModelStoreOwner as Activity)
        )

        addressBarWidget.watchJourneyDetailsState(
            viewModelStoreOwner as LifecycleOwner,
            BookingStorage.journeyDetailsStateViewModel
        )
        bookingModeWidget.watchJourneyDetailsState(
            viewModelStoreOwner as LifecycleOwner,
            BookingStorage.journeyDetailsStateViewModel
        )
        tripAllocationWidget.watchBookingRequestStatus(
            viewModelStoreOwner as LifecycleOwner,
            BookingStorage.bookingRequestStateViewModel
        )

        bookingMapWidget.onCreate(
            savedInstanceState,
            viewModelStoreOwner,
            BookingStorage.journeyDetailsStateViewModel
        )

        bookingModeWidget.callbackToStartQuoteList = { isPrebook ->
            this.presenter.onBookingModeSelected(isPrebook)
        }

        addressBarWidget.bindTripToView(BookingStorage.tripDetails)
    }

    override fun bindViews(context: Context) {
        addressBarWidget.setJourneyInfo(BookingStorage.journeyInfo)

        locateMeButton.setOnClickListener {
            if (presenter.hasLocationPermissions(context)) {
                bookingMapWidget.locateUser()
            } else {
                presenter.requestLocationPermissions(context)
            }
        }

        (viewModelStoreOwner as LifecycleOwner).lifecycle.apply {
            addObserver(bookingMapWidget)
        }
    }

    override fun saveInstanceState(bundle: Bundle) {
        presenter.saveInstanceState(bundle)

        bookingMapWidget.onSaveInstanceState(bundle)
    }

    override fun setMapLocation(latLng: LatLng) {
        bookingMapWidget.initialLocation = LatLng(latLng.latitude, latLng.longitude)
    }

    override fun getViewModelStoreOwner(): ViewModelStoreOwner {
        return viewModelStoreOwner
    }

    override fun onReceivedPermissionsResult(
        context: Context,
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ): Boolean {
        return if (presenter.onReceivedPermissionsResult(
                context,
                requestCode,
                permissions,
                grantResults
            )
        ) {
            bookingMapWidget.locationPermissionGranted()
            true
        } else {
            false
        }
    }

    override fun parseExtrasBundle(extras: Bundle?) {
        presenter.parseExtrasBundle(extras)
    }

    override fun initializeListeners(mapActions: BookingMapMVP.Actions) {
        bookingMapWidget.actions = mapActions
        tripAllocationWidget.actions = this

        presenter.initializeListeners()
    }

    override fun validateCoverage(hasAddresses: Boolean, hasCoverage: Boolean) {
        if (hasAddresses) {
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

    override fun parseDataFromActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.parseDataFromActivityResult(requestCode, resultCode, data)
    }

    override fun startTripAllocation(tripInfo: TripInfo) {
        addressBarWidget.visibility = View.INVISIBLE
        bookingModeWidget.visibility = View.GONE
        bookingMapWidget.centreMapToPickupPin()

        handleNavigationVisiblityCallback.invoke(
            View.INVISIBLE,
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        )

        tripAllocationWidget.onActivityResult(tripInfo)
    }

    override fun setAddresses(requestCode: Int, resultCode: Int, data: Intent?) {
        addressBarWidget.onActivityResult(requestCode, resultCode, data)
    }

    override fun setAddresses(pickup: LocationInfo?, destination: LocationInfo?, date: DateTime?) {
        pickup?.let {
            addressBarWidget.setPickup(pickup, -1)
        }

        destination?.let {
            addressBarWidget.setDestination(destination, -1)
        }

        date?.let {
            addressBarWidget.setPrebookTime(date)
        }
    }

    override fun onBookingCancelledOrFinished() {
        addressBarWidget.visibility = View.VISIBLE
        bookingModeWidget.visibility = View.VISIBLE
    }


}
