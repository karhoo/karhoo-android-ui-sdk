package com.karhoo.uisdk.screen.address.map

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.karhoo.sdk.analytics.AnalyticsManager
import com.karhoo.sdk.analytics.Event
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.address.AddressType
import com.karhoo.uisdk.util.extension.hideSoftKeyboard
import com.karhoo.uisdk.util.extension.isLocateMeEnabled
import com.karhoo.uisdk.util.extension.orZero
import com.karhoo.uisdk.util.extension.showSoftKeyboard
import kotlinx.android.synthetic.main.uisdk_view_address_map.view.addressSelectButton
import kotlinx.android.synthetic.main.uisdk_view_address_map.view.addressSelectionWidget
import kotlinx.android.synthetic.main.uisdk_view_address_map.view.mapDismissIcon
import kotlinx.android.synthetic.main.uisdk_view_address_map.view.pickupPinIcon
import kotlinx.android.synthetic.main.uisdk_view_address_map_picker.view.pickupDropoffIcon
import kotlinx.android.synthetic.main.uisdk_view_booking_map.view.mapView

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

    private val presenter: AddressMapMVP.Presenter = AddressMapPresenter(this)

    init {
        View.inflate(context, R.layout.uisdk_view_address_map, this)
        actions?.getLifecycle()?.addObserver(this)
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
        presenter.getAddress(Position(
                googleMap?.cameraPosition?.target?.latitude.orZero(),
                googleMap?.cameraPosition?.target?.longitude.orZero()))
    }

    override fun onCameraMoveStarted(p0: Int) {
        //kill any callbacks
    }

    private fun setupMap() {
        googleMap?.apply {
            if (isLocateMeEnabled(context)) {
                setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
                isMyLocationEnabled = true
                isIndoorEnabled = false
                uiSettings.isMyLocationButtonEnabled = false
                uiSettings.isMapToolbarEnabled = false
                setOnCameraIdleListener(this@AddressMapView)
                setOnCameraMoveStartedListener(this@AddressMapView)
                setPadding(0, resources.getDimensionPixelSize(R.dimen.map_padding_top), 0, resources.getDimensionPixelSize(R.dimen.map_padding_bottom))
                with(TypedValue()) {
                    resources.getValue(R.dimen.map_zoom_max, this, true)
                    setMaxZoomPreference(this.float)
                }
                zoom(initialLocation)

                AnalyticsManager.fireEvent(Event.LOADED_USERS_LOCATION)
            } else {
                isMyLocationEnabled = false
            }
        }
    }

    private fun zoom(position: LatLng?) {
        if (position != null) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, MAP_DEFAULT_ZOOM)
            googleMap?.animateCamera(cameraUpdate, resources.getInteger(R.integer.map_anim_duration), null)
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
}