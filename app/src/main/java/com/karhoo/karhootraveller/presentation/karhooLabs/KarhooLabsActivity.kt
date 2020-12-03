package com.karhoo.karhootraveller.presentation.karhooLabs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.presentation.karhooLabs.FeatureFlagStore.Companion.KEY_GUEST_CHECKOUT
import kotlinx.android.synthetic.main.activity_karhoo_labs.guestCheckoutSwitch
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
    }

    fun initialiseFeatureFlags() {
        val guestCheckout = featureFlag.enabled(KEY_GUEST_CHECKOUT)
        guestCheckoutSwitch.isChecked = guestCheckout
    }

    fun guestCheckoutClicked() {
        val value = guestCheckoutSwitch.isChecked
        featureFlag.updateFlag("guestCheckout", value)
    }
}
