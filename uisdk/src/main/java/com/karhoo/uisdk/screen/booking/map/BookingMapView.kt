package com.karhoo.uisdk.screen.booking.map

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.karhoo.sdk.analytics.AnalyticsManager
import com.karhoo.sdk.analytics.Event
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.snackbar.SnackbarAction
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.base.snackbar.SnackbarPriority
import com.karhoo.uisdk.base.snackbar.SnackbarType
import com.karhoo.uisdk.base.state.NoObserverAttachedException
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarViewContract
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import com.karhoo.uisdk.screen.booking.domain.userlocation.LocationInfoListener
import com.karhoo.uisdk.screen.booking.domain.userlocation.LocationProvider
import com.karhoo.uisdk.util.MapUtil
import com.karhoo.uisdk.util.ViewsConstants.BOOKING_MAP_CAMERA_ZOOM_WIDTH_PADDING
import com.karhoo.uisdk.util.ViewsConstants.BOOKING_MAP_DESTINATION_MARKER_MAX_ZOOM_PREFERENCE
import com.karhoo.uisdk.util.ViewsConstants.BOOKING_MAP_PICKUP_MARKER_MAX_ZOOM_PREFERENCE_DEFAULT
import com.karhoo.uisdk.util.extension.isLocateMeEnabled
import com.karhoo.uisdk.util.extension.orZero
import com.karhoo.uisdk.util.extension.showCurvedPolyline
import com.karhoo.uisdk.util.extension.showShadowedPolyLine
import kotlinx.android.synthetic.main.uisdk_view_booking_map.view.bookingMapLayout
import kotlinx.android.synthetic.main.uisdk_view_booking_map.view.locateMeButton
import kotlinx.android.synthetic.main.uisdk_view_booking_map.view.mapView
import kotlinx.android.synthetic.main.uisdk_view_booking_map.view.pickupPinIcon

@Suppress("TooManyFunctions")
class BookingMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
                                              ) : FrameLayout(context, attrs, defStyleAttr),
                                                  GoogleMap.OnCameraIdleListener,
                                                  GoogleMap.OnCameraMoveStartedListener,
                                                  BookingMapMVP.View, LifecycleObserver {

    private var isDeepLink: Boolean = false
    var initialLocation: LatLng? = null
    private var googleMap: GoogleMap? = null

    private var presenter: BookingMapMVP.Presenter = BookingMapPresenter(
        this,
        PickupOnlyPresenter(KarhooApi.addressService),
        PickupDropoffPresenter(), KarhooUISDK.analytics
                                                                        )

    private val locationProvider: LocationProvider =
        LocationProvider(context, KarhooUISDK.karhooApi.addressService)
    private var journeyDetailsStateViewModel: JourneyDetailsStateViewModel? = null

    var actions: BookingMapMVP.Actions? = null

    private var shouldReverseGeolocate: Boolean = true

    private var origin: LatLng? = null
    private var destination: LatLng? = null

    private var pickupPinRes: Int = R.drawable.uisdk_ic_pickup_pin
    private var dropOffPinRes: Int = R.drawable.uisdk_ic_dropoff_pin
    private var curvedLineColour: Int = R.color.kh_uisdk_primary_blue
    private var isLocateMeEnabled = isLocateMeEnabled(context)

    init {
        getCustomisationParameters(context, attrs, defStyleAttr)
        View.inflate(context, R.layout.uisdk_view_booking_map, this)
    }

    private fun getCustomisationParameters(
        context: Context,
        attr: AttributeSet?,
        defStyleAttr: Int
                                          ) {
        val typedArray = context.obtainStyledAttributes(
            attr, R.styleable.BookingMapView,
            defStyleAttr, R.style.KhBookingMapViewStyle
                                                       )
        pickupPinRes = typedArray.getResourceId(
            R.styleable.BookingMapView_mapPickupPin, R.drawable
                .uisdk_ic_pickup_pin
                                               )
        dropOffPinRes = typedArray.getResourceId(
            R.styleable.BookingMapView_mapDropOffPin, R
                .drawable.uisdk_ic_dropoff_pin
                                                )
        curvedLineColour = typedArray.getResourceId(
            R.styleable.BookingMapView_curvedLineColor, R
                .color.kh_uisdk_primary_blue
                                                   )
        typedArray.recycle()
    }

    fun centreMapToPickupPin() {
        googleMap?.let {
            it.setPadding(0, 0, 0, 0)
            it.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, MAP_DEFAULT_ZOOM))
        }
    }

    override fun zoomMapToOriginAndDestination() {
        zoomMapToOriginAndDestination(
            origin = Position(origin?.latitude.orZero(), origin?.longitude.orZero()),
            destination = Position(destination?.latitude.orZero(), destination?.longitude.orZero())
                                     )
    }

    override fun zoomMapToOriginAndDestination(origin: Position, destination: Position?) {
        googleMap?.let {
            it.setPadding(
                0, 0,
                0, resources.getDimensionPixelSize(R.dimen.kh_uisdk_map_padding_bottom)
                         )
            val destinationLatLng = destination?.let {
                LatLng(
                    destination.latitude, destination
                        .longitude
                      )
            }
            zoomMapToMarkers(LatLng(origin.latitude, origin.longitude), destinationLatLng)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupMap() {
        isLocateMeEnabled = isLocateMeEnabled(context)
        googleMap?.apply {
            if (isLocateMeEnabled) {
                setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
                isMyLocationEnabled = true
                isIndoorEnabled = false
                uiSettings.isMyLocationButtonEnabled = false
                uiSettings.isMapToolbarEnabled = false
                with(TypedValue()) {
                    resources.getValue(R.dimen.kh_uisdk_map_zoom_max, this, true)
                    setMaxZoomPreference(this.float)
                }

                when {
                    initialLocation != null -> {
                        zoom(initialLocation)
                    }
                    journeyDetailsStateViewModel?.currentState?.pickup != null -> {
                        journeyDetailsStateViewModel?.currentState?.pickup?.let {
                            zoom(LatLng(it.position?.latitude!!, it.position?.longitude!!))
                        }

                    }
                    else -> {
                        zoom(null)
                        getCurrentLocation()
                    }
                }

                AnalyticsManager.fireEvent(Event.LOADED_USERS_LOCATION)
                pickupPinIcon.visibility = View.VISIBLE
                showLocationButton(true)

                googleMap?.setOnMapLoadedCallback {
                    googleMap?.setOnCameraIdleListener(this@BookingMapView)
                    googleMap?.setOnCameraMoveStartedListener(this@BookingMapView)
                }
            } else {
                zoom(null)
                isMyLocationEnabled = false
                pickupPinIcon.visibility = View.GONE
            }
        }
    }

    override fun zoom(position: LatLng?) {
        if (position != null) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, MAP_DEFAULT_ZOOM)
            googleMap?.animateCamera(
                cameraUpdate,
                resources.getInteger(R.integer.kh_uisdk_map_anim_duration),
                null
            )
        } else {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    MAP_DEFAULT_LOCATION_LATITUDE,
                    MAP_DEFAULT_LOCATION_LONGITUDE
                ),
                MAP_DEFAULT_NO_PERMISSIONS_ZOOM
            )
            googleMap?.moveCamera(cameraUpdate)
        }
    }

    override fun moveTo(position: LatLng?) {
        position?.let {
            val cameraUpdate = CameraUpdateFactory.newLatLng(it)
            googleMap?.animateCamera(
                cameraUpdate,
                resources.getInteger(R.integer.kh_uisdk_map_anim_duration),
                null
                                    )
        }
    }

    override fun addPickUpMarker(pickup: Position?, dropoff: Position?) {
        pickup?.let {
            clearMarkers()
            if (!isLocateMeEnabled(context)) {
                addMarkers(it, dropoff)
            } else {
                val latLng = LatLng(it.latitude, it.longitude)
                moveTo(latLng)
                zoom(latLng)
            }
        }
    }

    override fun addMarkers(pickup: Position, dropoff: Position?) {
        mapView.getMapAsync { googleMap ->
            googleMap.clear()
            val origin = LatLng(pickup.latitude, pickup.longitude)
            addPinToMap(origin, pickupPinRes, R.string.kh_uisdk_address_pick_up)
            val destination = dropoff?.let { LatLng(dropoff.latitude, dropoff.longitude) }
            destination?.let {
                googleMap.setMaxZoomPreference(BOOKING_MAP_DESTINATION_MARKER_MAX_ZOOM_PREFERENCE)
                addPinToMap(destination, dropOffPinRes, R.string.kh_uisdk_address_drop_off)
                googleMap.showShadowedPolyLine(
                    origin,
                    destination,
                    ContextCompat.getColor(
                        context,
                        R.color.kh_uisdk_transparent_black_map
                                          )
                                              )
                googleMap.showCurvedPolyline(
                    origin,
                    destination,
                    ContextCompat.getColor(context, curvedLineColour)
                                            )

            }
                ?: googleMap.setMaxZoomPreference(
                    BOOKING_MAP_PICKUP_MARKER_MAX_ZOOM_PREFERENCE_DEFAULT
                                                 )
            pickupPinIcon.visibility = View.GONE
            zoomMapToMarkers(origin, destination)

            this.origin = origin
            this.destination = destination
        }
    }

    private fun addPinToMap(latLng: LatLng, @DrawableRes markerIcon: Int, @StringRes title: Int) {
        if (latLng.latitude != 0.0 && latLng.longitude != 0.0) {
            MapUtil.bitmapDescriptorFromVector(context, markerIcon)?.let {
                val marker = googleMap?.addMarker(
                    MarkerOptions()
                        .draggable(false)
                        .icon(it)
                        .position(latLng)
                                                 )
                marker?.title = context.getString(title)
            }
        }
    }

    private fun zoomMapToMarkers(origin: LatLng?, destination: LatLng?) {
        val boundsBuilder = LatLngBounds.Builder()
        origin?.let { boundsBuilder.include(it) }
        destination?.let { boundsBuilder.include(it) }
        val bounds = boundsBuilder.build()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * BOOKING_MAP_CAMERA_ZOOM_WIDTH_PADDING).toInt()

        val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
        googleMap?.moveCamera(cu)
    }

    private fun recentreMapIfDestinationIsNull() {
        with(journeyDetailsStateViewModel?.currentState) {
            val lat = this?.pickup?.position?.latitude
            val lng = this?.pickup?.position?.longitude
            val dest = this?.destination
            if (lat != null && lng != null && dest == null) {
                zoom(LatLng(lat, lng))
            }
        }
    }

    override fun clearMarkers() {
        if (pickupPinIcon.visibility == View.GONE) {
            pickupPinIcon.visibility = View.VISIBLE
        }
        googleMap?.clear()
    }

    //region map lifecycle
    fun onCreate(
        bundle: Bundle?,
        lifecycleOwner: LifecycleOwner,
        journeyDetailsStateViewModel: JourneyDetailsStateViewModel,
        shouldReverseGeolocate: Boolean = true,
        isDeepLink: Boolean = false
                ) {
        this.isDeepLink = isDeepLink
        this.shouldReverseGeolocate = if (isLocateMeEnabled) shouldReverseGeolocate else false
        bindViewToJourneyDetails(lifecycleOwner, journeyDetailsStateViewModel)
        mapView.onCreate(bundle)
        mapView.getMapAsync { googleMap ->
            this.googleMap = googleMap
            setupMap()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        mapView.onStop()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        mapView.onStart()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        mapView.onPause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        isLocateMeEnabled = isLocateMeEnabled(context)
        mapView.onResume()
    }

    private fun getCurrentLocation() {
        if (isLocateMeEnabled) {
            locationProvider.getAddress(object : LocationInfoListener {
                override fun onLocationInfoReady(locationInfo: LocationInfo) {
                    dismissSnackbar()
                    if (shouldReverseGeolocate) {
                        shouldReverseGeolocate = false
                        locationInfo.position?.let {
                            KarhooUISDK.analytics?.userLocated(Location("").apply {
                                latitude = it.latitude
                                longitude = it.longitude
                            })
                        }
                        try {
                            journeyDetailsStateViewModel?.process(
                                AddressBarViewContract.AddressBarEvent
                                    .PickUpAddressEvent(locationInfo)
                                                                )

                        } catch (exception: NoObserverAttachedException) {
                            // seems the callback fired after viewModel no longer has observers
                            exception.printStackTrace()
                        }
                    }
                }

                override fun onLocationServicesDisabled() {
                    val snackbarAction =
                        SnackbarAction(resources.getString(R.string.kh_uisdk_settings)) {
                            (context as Activity).startActivity(Intent(Settings.ACTION_SETTINGS))
                        }
                    showSnackbar(
                        SnackbarConfig(
                            type = SnackbarType.BLOCKING,
                            priority = SnackbarPriority.HIGH,
                            action = snackbarAction,
                            text = resources.getString(R.string.kh_uisdk_location_disabled)
                                      )
                                )
                }

                override fun onLocationInfoUnavailable(
                    errorMessage: String,
                    karhooError: KarhooError?
                                                      ) {
                    showSnackbar(SnackbarConfig(text = errorMessage, karhooError = karhooError))
                }

                override fun onResolutionRequired(resolvableApiException: ResolvableApiException) {
                    resolvableApiException.startResolutionForResult((context as Activity), 1)
                }
            })
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        mapView.onDestroy()
    }

    fun onLowMemory() {
        mapView.onLowMemory()
    }

    fun onSaveInstanceState(outState: Bundle) {
        mapView.onSaveInstanceState(outState)
    }
    //endregion

    private fun bindViewToJourneyDetails(
        lifecycleOwner: LifecycleOwner,
        journeyDetailsStateViewModel: JourneyDetailsStateViewModel
                                       ) {
        presenter.watchJourneyDetails(lifecycleOwner, journeyDetailsStateViewModel)
        this.journeyDetailsStateViewModel = journeyDetailsStateViewModel
    }

    override fun locateUser() {
        presenter.locateUserPressed()
    }

    override fun doReverseGeolocate() {
        getCurrentLocation()

        shouldReverseGeolocate = isLocateMeEnabled && !isDeepLink
        isDeepLink = false
    }

    override fun onCameraIdle() {
        presenter.mapMoved(googleMap?.cameraPosition?.target)
        googleMap?.setOnCameraIdleListener { }
    }

    override fun onCameraMoveStarted(reason: Int) {
        when (reason) {
            GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> {
                presenter.mapDragged()
                googleMap?.setOnCameraIdleListener(this)
            }
        }
    }

    //region Map Padding

    fun setNoBottomPadding() {
        googleMap?.setPadding(0, 0, 0, 0)
        recentreMapIfDestinationIsNull()
        showLocationButton(true)

        animateLocateMeButton(
            R.dimen.kh_uisdk_spacing_small,
            R.integer.kh_uisdk_animation_duration_slide_out_or_in_quotes
                             )
    }

    fun setDefaultPadding() {
        googleMap?.setPadding(
            0, 0,
            0, resources.getDimensionPixelSize(R.dimen.kh_uisdk_map_padding_bottom)
                             )

        showLocationButton(false)
    }

    private fun animateLocateMeButton(bottomMarginRes: Int, durationRes: Int) {

        val constraintSet = ConstraintSet()
        constraintSet.clone(bookingMapLayout)
        constraintSet.setMargin(
            R.id.locateMeButton, ConstraintSet.BOTTOM, resources
                .getDimension(bottomMarginRes).toInt()
                               )
        constraintSet.applyTo(bookingMapLayout)

        val transition = AutoTransition()
        transition.duration = resources.getInteger(durationRes).toLong()
        TransitionManager.beginDelayedTransition(
            bookingMapLayout, transition
                                                )
    }

    //endregion

    override fun showErrorDialog(stringId: Int, karhooError: KarhooError?) {
        // Do nothing
    }

    override fun showSnackbar(snackbarConfig: SnackbarConfig) {
        actions?.showSnackbar(snackbarConfig)
    }

    override fun dismissSnackbar() {
        // Do nothing
    }

    override fun showTopBarNotification(stringId: Int) {
        // Do nothing
    }

    override fun showTopBarNotification(value: String) {
        // Do nothing
    }

    override fun resetMap() {
        setupMap()
        this.shouldReverseGeolocate = isLocateMeEnabled
    }

    override fun locationPermissionGranted() {
        presenter.locationPermissionGranted()
    }

    override fun showLocationButton(show: Boolean) {
        if (show) {
            locateMeButton.visibility = View.VISIBLE
            locateMeButton.isClickable = true
        } else {
            locateMeButton.visibility = View.INVISIBLE
            locateMeButton.isClickable = false
        }
    }

    override fun updateMapViewForQuotesListVisibilityCollapsed() {
        animateLocateMeButton(
            R.dimen.kh_uisdk_quote_list_height,
            R.integer.kh_uisdk_animation_duration_slide_out_or_in
                             )
    }

    override fun updateMapViewForQuotesListVisibilityExpanded() {
        animateLocateMeButton(
            R.dimen.kh_uisdk_collapsible_pane_expanded_height,
            R.integer.kh_uisdk_animation_duration_slide_out_or_in
                             )
    }

    companion object {
        private const val MAP_DEFAULT_NO_PERMISSIONS_ZOOM = 5.0f
        private const val MAP_DEFAULT_ZOOM = 16f
        private const val MAP_DEFAULT_LOCATION_LATITUDE = 50.999763
        private const val MAP_DEFAULT_LOCATION_LONGITUDE = 1.614991
    }
}
