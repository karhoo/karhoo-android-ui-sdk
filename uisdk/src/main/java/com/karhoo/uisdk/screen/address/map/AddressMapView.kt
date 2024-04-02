package com.karhoo.uisdk.screen.address.map

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.karhoo.sdk.analytics.AnalyticsManager
import com.karhoo.sdk.analytics.Event
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.address.AddressType
import com.karhoo.uisdk.base.snackbar.SnackbarAction
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.base.snackbar.SnackbarPriority
import com.karhoo.uisdk.base.snackbar.SnackbarType
import com.karhoo.uisdk.screen.address.map.picker.AddressPickerView
import com.karhoo.uisdk.screen.booking.domain.userlocation.LocationProvider
import com.karhoo.uisdk.util.extension.hideSoftKeyboard
import com.karhoo.uisdk.util.extension.isLocateMeEnabled
import com.karhoo.uisdk.util.extension.orZero
import com.karhoo.uisdk.util.extension.showSoftKeyboard

private const val MAP_DEFAULT_ZOOM = 16.0f

class AddressMapView @JvmOverloads constructor(context: Context,
                                               attrs: AttributeSet? = null,
                                               @AttrRes defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr), AddressMapMVP.View,
      GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveStartedListener,
      LifecycleObserver {

    private var googleMap: GoogleMap? = null
    var initialLocation: LatLng? = null
    var actions: AddressMapMVP.Actions? = null

    private val presenter: AddressMapMVP.Presenter = AddressMapPresenter(this,
            locationProvider = LocationProvider(context, KarhooUISDK.karhooApi.addressService))

    private lateinit var addressSelectButton: FloatingActionButton
    private lateinit var mapDismissIcon: ImageView
    private lateinit var mapView: MapView
    private lateinit var addressSelectionWidget: AddressPickerView
    private lateinit var pickupDropoffIcon: ImageView
    private lateinit var pickupPinIcon: ImageView

    init {
        View.inflate(context, R.layout.uisdk_view_address_map, this)

        addressSelectButton = findViewById(R.id.addressSelectButton)
        mapDismissIcon = findViewById(R.id.mapDismissIcon)
        mapView = findViewById(R.id.mapView)
        addressSelectionWidget = findViewById(R.id.addressSelectionWidget)
        pickupDropoffIcon = findViewById(R.id.pickupDropoffIcon)
        pickupPinIcon = findViewById(R.id.pickupPinIcon)

        actions?.getLifecycleActivity()?.addObserver(this)
        addressSelectButton.setOnClickListener {
            presenter.selectAddressPressed()
        }
        mapDismissIcon.setOnClickListener {
            presenter.onBackArrowPressed()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(bundle: Bundle?) {
        mapView.onCreate(bundle)
        mapView.getMapAsync { googleMap ->
            this.googleMap = googleMap
            setupMap()
        }
    }

    fun onStop() {
        mapView.onStop()
    }

    fun onStart() {
        mapView.onStart()
    }

    fun onPause() {
        mapView.onPause()
    }

    fun onResume() {
        mapView.onResume()
    }

    fun onDestroy() {
        mapView.onDestroy()
    }

    fun onLowMemory() {
        mapView.onLowMemory()
    }

    override fun onCameraIdle() {
        googleMap?.setOnCameraIdleListener { }
        presenter.getAddress(Position(
                    googleMap?.cameraPosition?.target?.latitude.orZero(),
                    googleMap?.cameraPosition?.target?.longitude.orZero()))
    }

    override fun onCameraMoveStarted(reason: Int) {
        when (reason) {
            GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> {
                googleMap?.setOnCameraIdleListener(this@AddressMapView)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupMap() {
        googleMap?.apply {
            if (isLocateMeEnabled(context)) {
                setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
                isMyLocationEnabled = true
                isIndoorEnabled = false
                uiSettings.isMyLocationButtonEnabled = false
                uiSettings.isMapToolbarEnabled = false
                setOnCameraMoveStartedListener(this@AddressMapView)
                with(TypedValue()) {
                    resources.getValue(R.dimen.kh_uisdk_map_zoom_max, this, true)
                    setMaxZoomPreference(this.float)
                }
                presenter.getLastLocation()
                AnalyticsManager.fireEvent(Event.LOADED_USERS_LOCATION)
            } else {
                isMyLocationEnabled = false
                zoom(initialLocation)
            }

        }
    }

    override fun zoom(position: LatLng?) {
        if (position != null) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, MAP_DEFAULT_ZOOM)
            googleMap?.animateCamera(cameraUpdate, resources.getInteger(R.integer.kh_uisdk_map_anim_duration), null)
        } else {
            val cameraUpdate = CameraUpdateFactory.zoomTo(MAP_DEFAULT_ZOOM)
            googleMap?.moveCamera(cameraUpdate)
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.GONE) {
            showSoftKeyboard()
        } else {
            hideSoftKeyboard()
        }
    }

    override fun updateDisplayAddress(displayAddress: String) {
        addressSelectionWidget.setText(displayAddress)
    }

    override fun setFinalAddress(locationInfo: LocationInfo?) {
        locationInfo?.let {
            actions?.addressSelected(locationInfo)
        }
    }

    override fun showSnackbar(snackbarConfig: SnackbarConfig) {
        actions?.showSnackbar(snackbarConfig)
    }

    override fun hideMap() {
        visibility = GONE
    }

    override fun setAddressType(addressType: AddressType?) {
        addressType?.let {
            when (it) {
                AddressType.PICKUP -> {
                    pickupDropoffIcon.setImageResource(R.drawable.uisdk_ic_pickup)
                    pickupPinIcon.setImageResource(R.drawable.uisdk_ic_pickup_pin)
                }
                AddressType.DESTINATION -> {
                    pickupDropoffIcon.setImageResource(R.drawable.uisdk_ic_destination)
                    pickupPinIcon.setImageResource(R.drawable.uisdk_ic_dropoff_pin)
                }
            }
        }
    }

    override fun showLocationDisabledSnackbar() {
        val snackbarAction = SnackbarAction(resources.getString(R.string.kh_uisdk_settings)) { (context as Activity).startActivity(Intent(Settings.ACTION_SETTINGS)) }
        actions?.showSnackbar(SnackbarConfig(type = SnackbarType.BLOCKING_DISMISSIBLE,
                priority = SnackbarPriority.HIGH,
                action = snackbarAction,
                text = resources.getString(R.string.kh_uisdk_location_disabled)))
    }
}
