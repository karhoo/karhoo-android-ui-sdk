package com.karhoo.karhootraveller.presentation.karhooLabs

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.presentation.karhooLabs.FeatureFlagStore.Companion.KEY_DARK_MODE
import com.karhoo.karhootraveller.presentation.karhooLabs.FeatureFlagStore.Companion.KEY_GUEST_CHECKOUT
import kotlinx.android.synthetic.main.activity_karhoo_labs.darkModeSwitch
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
        guestCheckoutSwitch.setOnCheckedChangeListener { buttonView, isChecked -> guestCheckoutClicked() }
        darkModeSwitch.setOnCheckedChangeListener { buttonView, isChecked -> darkModeClicked() }
        darkModeSwitch.setOnClickListener {
            val isNightTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (isNightTheme) {
                Configuration.UI_MODE_NIGHT_YES ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Configuration.UI_MODE_NIGHT_NO ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    fun initialiseFeatureFlags() {
        val guestCheckout = featureFlag.enabled(KEY_GUEST_CHECKOUT)
        guestCheckoutSwitch.isChecked = guestCheckout
        val darkMode = featureFlag.enabled(KEY_DARK_MODE)
        darkModeSwitch.isChecked = darkMode
    }

    fun guestCheckoutClicked() {
        val value = guestCheckoutSwitch.isChecked
        featureFlag.updateFlag("guestCheckout", value)
    }

    fun darkModeClicked() {
        val value = darkModeSwitch.isChecked
        featureFlag.updateFlag("darkMode", value)
    }


}
