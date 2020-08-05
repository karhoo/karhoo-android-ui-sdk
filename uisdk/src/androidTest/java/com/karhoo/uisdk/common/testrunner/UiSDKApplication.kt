package com.karhoo.uisdk.common.testrunner

import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.SystemClock
import android.view.MenuItem
import com.google.android.gms.location.LocationServices
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.base.MenuHandler
import com.karhoo.uisdk.util.TestSDKConfig

class UiSDKApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        KarhooUISDK.setConfiguration(configuration = TestSDKConfig(context = this
                .applicationContext))
        KarhooUISDK.menuHandler = EmptyMenuHandler()

        setupMockLocation()
    }

    private fun setupMockLocation() {
        val mockLocation = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = UiSDKTestConfig.DEVICE_LAT
            longitude = UiSDKTestConfig.DEVICE_LNG
            altitude = 5.0
            accuracy = 4f
            time = SystemClock.currentThreadTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        }

        with(getSystemService(Context.LOCATION_SERVICE) as LocationManager) {
            if (getProvider(LocationManager.GPS_PROVIDER) == null) {
                addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, false,
                                true, true, 0, 5)
                setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
                setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation)
            }
        }

        LocationServices.getFusedLocationProviderClient(this).apply {
            setMockMode(true)
            setMockLocation(mockLocation)
        }
    }

}

class EmptyMenuHandler : MenuHandler {

    override fun onNavigationItemSelected(context: Context, item: MenuItem) {

    }

}