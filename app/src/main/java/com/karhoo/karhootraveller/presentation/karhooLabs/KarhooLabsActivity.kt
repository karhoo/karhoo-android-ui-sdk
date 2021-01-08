package com.karhoo.karhootraveller.presentation.karhooLabs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.presentation.karhooLabs.FeatureFlagStore.Companion.KEY_GUEST_CHECKOUT
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.uisdk.util.extension.isGuest
import kotlinx.android.synthetic.main.activity_karhoo_labs.guestCheckoutSwitch
import kotlinx.android.synthetic.main.activity_karhoo_labs.karhooLabsForceCrash
import kotlinx.android.synthetic.main.activity_karhoo_labs.karhooLabsSendAnalytics
import kotlinx.android.synthetic.main.activity_karhoo_labs.toolbar

class KarhooLabsActivity : AppCompatActivity() {
    lateinit var featureFlag: FeatureFlag
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_karhoo_labs)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        featureFlag = FeatureFlag(this)
        initialiseFeatureFlags()
        guestCheckoutSwitch.setOnCheckedChangeListener { buttonView, isChecked -> guestCheckoutClicked() }
        karhooLabsForceCrash.setOnClickListener { forceCrashClicked() }
        karhooLabsSendAnalytics.setOnClickListener { sendAnalyticsEvent() }
    }

    fun initialiseFeatureFlags() {
        val guestCheckout = featureFlag.enabled(KEY_GUEST_CHECKOUT)
        guestCheckoutSwitch.isChecked = guestCheckout
    }

    fun guestCheckoutClicked() {
        val value = guestCheckoutSwitch.isChecked
        featureFlag.updateFlag("guestCheckout", value)
    }

    fun forceCrashClicked() {
        throw RuntimeException("Exception")
    }

    fun sendAnalyticsEvent() {
        val name = if (isGuest()) "Guest user" else " ${KarhooApi.userStore.currentUser.firstName} ${KarhooApi.userStore.currentUser.lastName}"
        val email = if (isGuest()) "test@karhoo.com" else KarhooApi.userStore.currentUser.email
        Firebase.analytics.logEvent("KARHOO_LABS") {
            param("email", email)
            param("name", name)
        }
    }
}
