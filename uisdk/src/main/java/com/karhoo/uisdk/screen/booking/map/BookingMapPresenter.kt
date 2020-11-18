package com.karhoo.uisdk.screen.booking.map

import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.android.gms.maps.model.LatLng
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider.isGuest
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarViewContract
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.util.ViewsConstants.BOOKING_MAP_PICKUP_GEOCODE_DELAY
import java.util.Timer
import kotlin.concurrent.schedule

internal class BookingMapPresenter(view: BookingMapMVP.View, private val pickupOnlyPresenter: BookingMapStategy.Presenter,
                                   private val pickupDropoffPresenter: BookingMapStategy.Presenter,
                                   private val analytics: Analytics?)
    : BasePresenter<BookingMapMVP.View>(), BookingMapMVP.Presenter, BookingMapStategy.Owner {

    private var mainPresenter: BookingMapStategy.Presenter? = null
    private var currentBookingStatus: BookingStatus? = null
    private var bookingStatusStateViewModel: BookingStatusStateViewModel? = null
    private var mapMoving: Boolean = false
    private var timer: Timer? = null

    init {
        attachView(view)
        pickupOnlyPresenter.setOwner(this)
        pickupDropoffPresenter.setOwner(this)
    }

    override fun watchBookingStatus(lifecycleOwner: LifecycleOwner, bookingStatusStateViewModel: BookingStatusStateViewModel) {
        this.bookingStatusStateViewModel = bookingStatusStateViewModel
        val observer = Observer<BookingStatus> { currentStatus ->
            currentBookingStatus = currentStatus

            if (currentBookingStatus?.pickup != null && currentBookingStatus?.destination != null) {
                if (mainPresenter !== pickupDropoffPresenter) {
                    mainPresenter = pickupDropoffPresenter
                }
                addMarkers()
                moveToMarker(currentBookingStatus?.pickup, currentBookingStatus?.destination)
            } else if (currentBookingStatus?.pickup != null && currentBookingStatus?.destination == null) {
                if (mainPresenter !== pickupOnlyPresenter) {
                    mainPresenter = pickupOnlyPresenter
                    setPickupLocation(currentBookingStatus?.pickup)
                }
                moveToMarker(currentBookingStatus?.pickup, currentBookingStatus?.destination)
            } else if (!mapMoving && currentBookingStatus?.pickup == null) {
                view?.doReverseGeolocate()
            }
        }
        bookingStatusStateViewModel.viewStates().observe(lifecycleOwner, observer)
    }

    private fun addMarkers() {
        val pickup = currentBookingStatus?.pickup?.position
        val dropOff = currentBookingStatus?.destination?.position

        if (pickup != null) {
            view?.addMarkers(pickup, dropOff)
            view?.zoomMapToOriginAndDestination(pickup, dropOff)
        }
    }

    override fun mapMoved(position: LatLng?) {
        timer?.let {
            it.cancel()
            timer = null
        }
        timer = Timer()
        timer?.schedule(BOOKING_MAP_PICKUP_GEOCODE_DELAY) {
            position?.let {
                mainPresenter?.mapMoved(it)
            }
        }
        mapMoving = false
    }

    override fun setPickupLocation(pickupLocation: LocationInfo?) {
        if (!mapMoving) {
            bookingStatusStateViewModel?.process(AddressBarViewContract.AddressBarEvent
                                                         .PickUpAddressEvent(pickupLocation))
        }
    }

    private fun moveToMarker(pickupLocation: LocationInfo?, destinationLocation: LocationInfo?) {
        if (pickupLocation != null) {
            view?.addPickUpMarker(pickupLocation.position, destinationLocation?.position)
        }
    }

    override fun mapDragged() {
        mapMoving = true
        mainPresenter?.mapDragged()
    }

    override fun zoom(position: LatLng) {
        view?.zoom(position)
    }

    override fun zoomMapToMarkers() {
        view?.zoomMapToOriginAndDestination()
    }

    override fun locateAndUpdate() {
        view?.doReverseGeolocate()
    }

    override fun onError(@StringRes errorMessage: Int) {
        view?.showSnackbar(SnackbarConfig(text = null, stringId = errorMessage))
    }

    override fun locateUserPressed() {
        mainPresenter?.let {
            mapMoving = false
            analytics?.reverseGeo()
            it.locateUserPressed()
        }
    }

    override fun locationPermissionGranted() {
        view?.resetMap()
        view?.locateUser()
    }

    override fun checkLocateUser() {
        if (isGuest()) {
            view?.hideLocateUserButton()
        } else {
            view?.showLocateUserButton()
        }
    }
}
