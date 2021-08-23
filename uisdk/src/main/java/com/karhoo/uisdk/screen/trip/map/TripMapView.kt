package com.karhoo.uisdk.screen.trip.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.karhoo.sdk.analytics.AnalyticsManager
import com.karhoo.sdk.analytics.Event
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.Position
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.LocationLock
import com.karhoo.uisdk.base.snackbar.SnackbarAction
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.base.snackbar.SnackbarPriority
import com.karhoo.uisdk.base.snackbar.SnackbarType
import com.karhoo.uisdk.screen.booking.domain.userlocation.LocationProvider
import com.karhoo.uisdk.screen.trip.map.anim.LatLngInterpolator
import com.karhoo.uisdk.screen.trip.map.anim.MarkerAnimation
import com.karhoo.uisdk.util.MapUtil
import com.karhoo.uisdk.util.ViewsConstants.TRIP_MAP_CAMERA_ZOOM_WIDTH_PADDING
import com.karhoo.uisdk.util.ViewsConstants.TRIP_MAP_DRIVER_ZINDEX
import com.karhoo.uisdk.util.extension.hasLocationPermission
import com.karhoo.uisdk.util.extension.orZero
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.uisdk_view_trip_map.view.mapView

class TripMapView @JvmOverloads constructor(context: Context,
                                            attrs: AttributeSet? = null,
                                            defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), OnMapReadyCallback, PermissionListener,
      TripMapMVP.View, GoogleMap.OnCameraMoveStartedListener, LifecycleObserver {

    private var presenter: TripMapPresenter = TripMapPresenter(this, KarhooApi.driverTrackingService,
                                                               KarhooApi.tripService, KarhooUISDK.analytics,
                                                               LocationProvider(context, KarhooApi.addressService))

    var actions: TripMapMVP.Actions? = null

    private var googleMap: GoogleMap? = null
    private var driverCar: Marker? = null
    private var pickupPinRes: Int = R.drawable.uisdk_ic_pickup_pin
    private var dropOffPinRes: Int = R.drawable.uisdk_ic_dropoff_pin
    private var curvedLineColour: Int = R.color.kh_uisdk_primary_blue
    private var hasLocationPermission: Boolean = false

    override var userLocationVisible: Boolean
        get() = hasLocationPermission && (googleMap?.isMyLocationEnabled ?: false)
        @SuppressLint("MissingPermission")
        set(value) {
            if (hasLocationPermission) {
                googleMap?.isMyLocationEnabled = value
            }
        }

    init {
        getCustomisationParameters(context, attrs, defStyleAttr)
        inflate(context, R.layout.uisdk_view_trip_map, this)
        checkPermissions()
    }

    private fun getCustomisationParameters(context: Context, attr: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attr, R.styleable.BookingMapView,
                                                        defStyleAttr, R.style.KhBookingMapViewStyle)
        pickupPinRes = typedArray.getResourceId(R.styleable.BookingMapView_mapPickupPin, R.drawable
                .uisdk_ic_pickup_pin)
        dropOffPinRes = typedArray.getResourceId(R.styleable.BookingMapView_mapDropOffPin, R
                .drawable.uisdk_ic_dropoff_pin)
        curvedLineColour = typedArray.getResourceId(R.styleable.BookingMapView_curvedLineColor, R
                .color.kh_uisdk_primary_blue)
        typedArray.recycle()
    }

    private fun checkPermissions() {
        if (hasLocationPermission && isLocationEnabled()) {
            Dexter.withActivity(context as Activity)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(this)
                    .check()
        } else {
            val snackbarAction = SnackbarAction(resources.getString(R.string.kh_uisdk_settings)) { (context as Activity).startActivity(Intent(Settings.ACTION_SETTINGS)) }
            val snackbarConfig = SnackbarConfig(type = SnackbarType.BLOCKING,
                                                priority = SnackbarPriority.NORMAL,
                                                action = snackbarAction,
                                                text = resources.getString(R.string.kh_uisdk_location_disabled))
            actions?.showSnackbar(snackbarConfig)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun trackDriver(tripIdentifier: String) {
        presenter.trackDriverPosition(tripIdentifier)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.setOnCameraMoveStartedListener(this)
        setupMap()
    }

    fun bindPickupAndDropOffLocations(origin: Position, destination: Position?) {
        presenter.apply {
            setOrigin(origin)
            setDestination(destination!!)
        }
    }

    fun locateMe() {
        presenter.locateMe()
    }

    override fun addPinToMap(position: Position, isPickup: Boolean, @StringRes title: Int) {
        if (position.latitude != 0.0 && position.longitude != 0.0) {
            googleMap?.addMarker(MarkerOptions()
                                         .draggable(false)
                                         .icon(MapUtil.bitmapDescriptorFromVector(context, if (isPickup) pickupPinRes else dropOffPinRes))
                                         .position(LatLng(position.latitude, position.longitude)))?.title = context.getString(title)
        }
    }

    override fun onPermissionGranted(response: PermissionGrantedResponse) {
        setupMap()
    }

    @SuppressLint("MissingPermission")
    private fun setupMap() {
        googleMap?.apply {
            isMyLocationEnabled = hasLocationPermission && !KarhooUISDKConfigurationProvider.isGuest()
            setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = false
            isIndoorEnabled = false
            with(TypedValue()) {
                resources.getValue(R.dimen.kh_uisdk_map_zoom_max, this, true)
                setMaxZoomPreference(this.float)
            }
            setPadding(0, 0,
                       0, resources.getDimensionPixelSize(R.dimen.kh_uisdk_map_padding_bottom_trip))

            presenter.mapIsReady()
            AnalyticsManager.fireEvent(Event.LOADED_USERS_LOCATION)
        }
    }

    private fun setRationalShown(isShown: Boolean) {
        context
                .getSharedPreferences(context.getString(R.string.kh_uisdk_permissions), Context.MODE_PRIVATE)
                .edit().putBoolean(context.getString(R.string.kh_uisdk_location_rationale_shown), isShown)
                .apply()
    }

    private fun rationalShown(): Boolean {
        return context
                .getSharedPreferences(context.getString(R.string.kh_uisdk_permissions), Context.MODE_PRIVATE)
                .getBoolean(context.getString(R.string.kh_uisdk_location_rationale_shown), false)
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse) {
        if (response.isPermanentlyDenied) {
            setRationalShown(false)
            AnalyticsManager.fireEvent(Event.REJECT_LOCATION_SERVICES)
            (context as LocationLock).showLocationLock()
        } else if (!rationalShown()) {
            setRationalShown(true)
            Dexter.withActivity(context as Activity)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(this)
                    .check()
        }
    }

    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
        // Do nothing
    }

    override fun animateDriverPositionToLatLng(duration: Int, latitude: Double, longitude: Double) {
        val position = LatLng(latitude, longitude)
        if (driverCar == null) {
            driverCar = googleMap?.addMarker(MarkerOptions()
                                                     .zIndex(TRIP_MAP_DRIVER_ZINDEX)
                                                     .position(position)
                                                     .draggable(false)
                                                     .icon(MapUtil.bitmapDescriptorFromVector(context, R.drawable.uisdk_ic_car)))
        } else {
            driverCar?.let { MarkerAnimation.animateMarkerTo(it, position, LatLngInterpolator.Linear(), duration.toLong()) }
        }
    }

    override fun zoomMapToIncludeLatLngs(duration: Int, vararg positions: Position?) {
        val boundsBuilder = LatLngBounds.Builder()
        for (position in positions) {
            boundsBuilder.include(LatLng(position?.latitude.orZero(), position?.longitude.orZero()))
        }

        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * TRIP_MAP_CAMERA_ZOOM_WIDTH_PADDING).toInt()

        val cu = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), width, height, padding)
        googleMap?.animateCamera(cu, duration, null)
    }

    override fun onCameraMoveStarted(i: Int) {
        if (i == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            presenter.mapDragged()
        }
    }

    override fun resolveApiException(resolvableApiException: ResolvableApiException) {
        resolvableApiException.startResolutionForResult((context as Activity), 1)
    }

    //region Map lifecycle

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        hasLocationPermission = hasLocationPermission(context)
        checkPermissions()
        mapView.onResume()
        mapView.getMapAsync(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        presenter.onDestroy()
        mapView.onDestroy()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        mapView.onPause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        mapView.onStart()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        mapView.onStop()
        presenter.onStop()
    }

    fun onSaveInstanceState(outState: Bundle) {
        mapView.onSaveInstanceState(outState)
    }

    fun onLowMemory() {
        mapView.onLowMemory()
    }

    fun onCreate(savedInstanceState: Bundle) {
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    //endregion
}
