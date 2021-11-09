package com.karhoo.uisdk.screen.booking.domain.userlocation

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.service.address.AddressService
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UserLocationProviderTest {

    internal lateinit var userLocationProvider: LocationProvider

    internal var addressService: AddressService = KarhooApi.addressService
    internal var context: Context = mock()
    internal var locationService: LocationServices = mock()
    internal var fusedLocationClient: FusedLocationProviderClient = mock()
    internal var settingsClient: SettingsClient = mock()
    internal var locationSettingsTask: Task<LocationSettingsResponse> = mock()
    internal var locationOnSuccess: Task<LocationSettingsResponse> = mock()
//    internal var locationCallBack: LocationCallback = mock()

    @Before
    fun setUp() {
        userLocationProvider = LocationProvider(context = context, addressService = addressService, fusedLocationClient = fusedLocationClient, settingsClient = settingsClient)
        whenever(settingsClient.checkLocationSettings(any())).thenReturn(locationSettingsTask)
        whenever(locationSettingsTask.addOnSuccessListener(any())).thenReturn(locationOnSuccess)
    }

    /**
     * When    Initialized
     * Then    The location provider should be correctly configured
     */
    @Test
    fun locationProviderShouldBeConfiguredCorrectlyAtStart() {
        val positionListener: PositionListener = mock()
        userLocationProvider.listenForLocations(positionListener = positionListener, numberOfUpdates = 1)
        verify(settingsClient).checkLocationSettings(any())
    }

    /**
     * Given    A user has denied location permissions
     * When    Getting the last know position
     * Then    Null should be the last known position of the location manager
     */
    @Test
    fun gettingPositionManuallyReturnsTheLastKnownPosition() {
        // Needs to be filled out
    }

    /**
     * Given   A a user has allowed there location has
     * When    A new location is received
     * Then    The location should be passed to the callback
     */
    @Test
    fun onLocationUpdateShouldReturnNewLocationThroughCallback() {
        // Needs to be filled out
    }

    /**
     * Given   A callback has been set
     * When    An empty location is received
     * Then    The location should not be passed to the callback
     */
    @Test
    fun dontUpdateLocationIfLocationIsNull() {
        // Needs to be filled out
    }

    /**
     * Given   A callback has not been set
     * When    A new location is received
     * Then    Nothing should happen
     */
    @Test
    fun whenNoCallbackIsSetNoUpdateShouldHappen() {
        // Needs to be filled out
    }

    /**
     * Given:   A settings callback has been set
     * When:    A request is made to monitor the settings
     * Then:    The call should be made on the set callback
     */
    @Test
    fun settingsAllowedCallsBackOnTheCorrectCallback() {
        // Needs to be filled out
    }

    /**
     * Given:   A settings callback has been set
     * When:    A request is made to monitor the settings
     * And:     The settings are denied
     * Then:    The call should be made on the set callback
     */
    @Test
    fun settingsDeniedCallsBackOnTheCorrectCallback() {
        // Needs to be filled out
    }

    /**
     * When:   A request is made to stop getting updates
     * Then:   the request should be forwarded to the location service
     */
    // TODO: Need to refactor LocationProvider for locationCallback
//    @Test
//    fun cancellingLocationUpdatesForwardsTheCall() {
//        userLocationProvider.stopListeningForLocations()
//        verify(fusedLocationClient).removeLocationUpdates(locationCallBack)
//    }

    /**
     * When:   A request is made to start getting updates
     * Then:   the request should be forwarded to the location service
     */
    @Test
    fun startingLocationUpdatesForwardsTheCall() {
        // listenForLocations() needs a positionListener setup

//        userLocationProvider.listenForLocations(positionListener = )
//        verify(userLocationProvider).listenForLocations(positionListener = )
    }

    /**
     * Given:   A location update callback has been set
     * When:    A request is made to monitor the users location
     * Then:    The call should be made on the set callback
     */
    @Test
    fun locationUpdatedCallsBackOnTheCorrectCallback() {
        // Needs to be filled out
    }
}
