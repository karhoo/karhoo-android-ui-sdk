package com.karhoo.karhootraveller.presentation.splash.register

import android.content.Intent
import android.location.Location
import androidx.annotation.StringRes
import com.karhoo.karhootraveller.presentation.splash.SplashActivity

interface SplashActions {

    fun goToBooking(location: Location?)

    fun goFullScreen()

    fun showErrorWithAction(@StringRes error: Int, action: () -> Unit, @StringRes actionText: Int)

    fun dismissErrors()

    fun startActivity(intent: Intent)

    fun startActivityForResult(intent: Intent?, requestCode: Int)

}
