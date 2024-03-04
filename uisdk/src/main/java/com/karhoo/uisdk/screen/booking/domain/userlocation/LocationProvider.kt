package com.karhoo.uisdk.screen.booking.domain.userlocation

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.address.AddressService
import com.karhoo.uisdk.util.returnErrorStringOrLogoutIfRequired

class LocationProvider(private val context: Context,
                       private val addressService: AddressService = KarhooApi.addressService,
                       private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context.applicationContext),
                       private val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)) {

    private var locationCallback: LocationCallback? = null

    fun getAddress(locationInfoListener: LocationInfoListener) {

        listenForLocations(object : PositionListener {
            override fun onPositionUpdated(location: Location) {
                val position = Position(location.latitude, location.longitude)

                addressService.reverseGeocode(position).execute { result ->
                    when (result) {
                        is Resource.Success -> locationInfoListener.onLocationInfoReady(result.data)
                        is Resource.Failure -> locationInfoListener.onLocationInfoUnavailable(
                                context.getString(returnErrorStringOrLogoutIfRequired(result.error)), result.error)
                    }
                }
            }

            override fun onLocationServicesDisabled() {
                locationInfoListener.onLocationServicesDisabled()
                locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
            }

            override fun onResolutionRequired(resolvableApiException: ResolvableApiException) {
                locationInfoListener.onResolutionRequired(resolvableApiException)
            }

        }, numberOfUpdates = 1)
    }

    @SuppressLint("MissingPermission")
    fun listenForLocations(positionListener: PositionListener, numberOfUpdates: Int? = null) {

        val locationRequest = LocationRequest().apply {
            interval = LOCATION_INTERVAL
            fastestInterval = LOCATION_FASTEST_INTERVAL
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            numberOfUpdates?.let { numUpdates = it }
        }

        val settingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                val location = result.locations[0]
                positionListener.onPositionUpdated(location)
            }
        }

        fun requestFreshLocationUpdates() = fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback as LocationCallback, Looper.myLooper())

        settingsClient.checkLocationSettings(settingsRequest)
                .addOnSuccessListener {
                    if (numberOfUpdates == 1) {
                        fusedLocationClient.lastLocation
                                .addOnSuccessListener {
                                    if (it == null) {
                                        requestFreshLocationUpdates()
                                    } else {
                                        positionListener.onPositionUpdated(it)
                                    }
                                }
                                .addOnFailureListener { requestFreshLocationUpdates() }
                    } else {
                        requestFreshLocationUpdates()
                    }

                }
                .addOnFailureListener {
                    if (it is ResolvableApiException) {
                        positionListener.onResolutionRequired(it)
                    } else {
                        positionListener.onLocationServicesDisabled()
                    }
                }
    }

    fun stopListeningForLocations() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
    }

    companion object {
        const val LOCATION_INTERVAL = 5000L
        const val LOCATION_FASTEST_INTERVAL = 5000L
    }

}

interface LocationInfoListener {

    fun onLocationInfoReady(locationInfo: LocationInfo)

    fun onLocationServicesDisabled()

    fun onLocationInfoUnavailable(errorMessage: String, karhooError: KarhooError?)

    fun onResolutionRequired(resolvableApiException: ResolvableApiException)

}

interface PositionListener {

    fun onPositionUpdated(location: Location)

    fun onLocationServicesDisabled()

    fun onResolutionRequired(resolvableApiException: ResolvableApiException)

}
