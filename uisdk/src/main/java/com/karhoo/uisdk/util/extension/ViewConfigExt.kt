package com.karhoo.uisdk.util.extension

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import androidx.core.app.ActivityCompat
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.network.request.UIConfigRequest
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider

fun View.configure() {

    tag?.toString()?.let { tag ->
        KarhooApi.configService.uiConfig(UIConfigRequest(viewId = tag)).execute {
            when (it) {
                is Resource.Success -> {
                    visibility = if (it.data.hidden) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }
                is Resource.Failure -> {
                } //dont change the view config
            }
        }
    }
}

fun hasLocationPermission(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission
            .ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

fun isLocateMeEnabled(context: Context): Boolean {
    return hasLocationPermission(context) && !KarhooUISDKConfigurationProvider.isGuest()
}

fun isGuest(): Boolean {
    return KarhooUISDKConfigurationProvider.isGuest()
}
