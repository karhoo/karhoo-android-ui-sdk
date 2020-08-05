package com.karhoo.karhootraveller.presentation.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.presentation.web.KarhooWebActivity
import com.karhoo.karhootraveller.util.VersionUtil
import kotlinx.android.synthetic.main.activity_about.licenceAttributionLayout
import kotlinx.android.synthetic.main.activity_about.privacyPolicyLayout
import kotlinx.android.synthetic.main.activity_about.termsAndConditionsLabel
import kotlinx.android.synthetic.main.activity_about.toolbar
import kotlinx.android.synthetic.main.activity_about.versionCodeText

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        versionCodeText.text = VersionUtil.createBuildVersionString(this)

        termsAndConditionsLabel.setOnClickListener { launchTermsAndConditions() }
        privacyPolicyLayout.setOnClickListener { launchPrivacyPolicy() }
        licenceAttributionLayout.setOnClickListener { launchLicencesInWebActivity() }
    }

    private fun launchTermsAndConditions() {
        val webIntent = KarhooWebActivity.Builder.builder
                .setScrollable(true)
                .url(getString(R.string.link_t_n_c_terms))
                .build(this)
        startActivity(webIntent)
    }

    private fun launchPrivacyPolicy() {
        val webIntent = KarhooWebActivity.Builder.builder
                .setScrollable(true)
                .url(getString(R.string.link_t_n_c_privacy))
                .build(this)
        startActivity(webIntent)
    }

    private fun launchLicencesInWebActivity() {
        val licenceWebIntent = KarhooWebActivity.Builder.builder
                .setScrollable(true)
                .url(getString(R.string.link_open_source_licenses))
                .build(this)
        startActivity(licenceWebIntent)
    }

}