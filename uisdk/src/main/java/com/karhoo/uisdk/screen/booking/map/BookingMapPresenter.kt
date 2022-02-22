package com.karhoo.uisdk.screen.booking.map

import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.android.gms.maps.model.LatLng
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarViewContract
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import com.karhoo.uisdk.util.ViewsConstants.BOOKING_MAP_PICKUP_GEOCODE_DELAY
import java.util.Timer
import kotlin.concurrent.schedule

internal class BookingMapPresenter(view: BookingMapMVP.View, private val pickupOnlyPresenter: BookingMapStategy.Presenter,
                                   private val pickupDropoffPresenter: BookingMapStategy.Presenter,
                                   private val analytics: Analytics?)
    : BasePresenter<BookingMapMVP.View>(), BookingMapMVP.Presenter, BookingMapStategy.Owner {

    private var mainPresenter: BookingMapStategy.Presenter? = null
    private var currentJourneyDetails: JourneyDetails? = null
    private var journeyDetailsStateViewModel: JourneyDetailsStateViewModel? = null
    private var mapMoving: Boolean = false
    private var timer: Timer? = null

    init {
        attachView(view)
        pickupOnlyPresenter.setOwner(this)
        pickupDropoffPresenter.setOwner(this)
    }

    override fun watchBookingStatus(lifecycleOwner: LifecycleOwner, journeyDetailsStateViewModel: JourneyDetailsStateViewModel) {
        this.journeyDetailsStateViewModel = journeyDetailsStateViewModel
        val observer = Observer<JourneyDetails> { currentStatus ->
            currentJourneyDetails = currentStatus

            if (currentJourneyDetails?.pickup != null && currentJourneyDetails?.destination != null) {
                if (mainPresenter !== pickupDropoffPresenter) {
                    mainPresenter = pickupDropoffPresenter
                }
                addMarkers()
                moveToMarker(currentJourneyDetails?.pickup, currentJourneyDetails?.destination)
            } else if (currentJourneyDetails?.pickup != null && currentJourneyDetails?.destination == null) {
                if (mainPresenter !== pickupOnlyPresenter) {
                    mainPresenter = pickupOnlyPresenter
                    setPickupLocation(currentJourneyDetails?.pickup)
                }
                moveToMarker(currentJourneyDetails?.pickup, currentJourneyDetails?.destination)
            } else if (!mapMoving && currentJourneyDetails?.pickup == null) {
                view?.doReverseGeolocate()
            }
        }
        journeyDetailsStateViewModel.viewStates().observe(lifecycleOwner, observer)
    }

    private fun addMarkers() {
        val pickup = currentJourneyDetails?.pickup?.position
        val dropOff = currentJourneyDetails?.destination?.position

        if (pickup != null) {
            view?.addMarkers(pickup, dropOff)
            view?.zoomMapToOriginAndDestination(pickup, dropOff)
        }
    }

    override fun mapMoved(position: LatLng?) {
        timer?.let {
            it.purge()
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
            journeyDetailsStateViewModel?.process(AddressBarViewContract.AddressBarEvent
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

    override fun onError(@StringRes errorMessage: Int, karhooError: KarhooError?) {
        view?.showSnackbar(SnackbarConfig(text = null, messageResId = errorMessage, karhooError = karhooError))
    }

    override fun locateUserPressed() {
        mainPresenter?.let {
            mapMoving = false
            it.locateUserPressed()
        }
    }

    override fun locationPermissionGranted() {
        view?.resetMap()
        view?.locateUser()
    }

    override fun checkLocateUser() {
        view?.showLocationButton(true)
    }
}
