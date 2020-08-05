package com.karhoo.karhootraveller.presentation.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.presentation.profile.user.UserProfileMVP
import com.karhoo.karhootraveller.util.logoutAndResetApp
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.screen.booking.booking.BookingPaymentMVP
import kotlinx.android.synthetic.main.activity_profile.bookingPaymentDetailsWidget
import kotlinx.android.synthetic.main.activity_profile.toolbar
import kotlinx.android.synthetic.main.activity_profile.userProfileView

class ProfileActivity : BaseActivity(), BookingPaymentMVP.Actions, UserProfileMVP.Actions {

    override val layout: Int = R.layout.activity_profile

    private var canUpdateProfile = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setTitle(R.string.title_activity_profile)
        }
        userProfileView.actions = this
        lifecycle.addObserver(userProfileView)
        bookingPaymentDetailsWidget.actions = this
    }

    override fun handleExtras() {
    }

    override fun initialiseViews() {
    }

    override fun initialiseViewListeners() {
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_edit_profile).isVisible = !userProfileView.isEditingProfile()
        menu.findItem(R.id.action_logout).isVisible = !userProfileView.isEditingProfile()
        menu.findItem(R.id.action_save).isVisible = userProfileView.isEditingProfile()
        menu.findItem(R.id.action_discard).isVisible = userProfileView.isEditingProfile()
        menu.findItem(R.id.action_save).isEnabled = canUpdateProfile
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logoutAndResetApp(false)
                true
            }
            R.id.action_edit_profile -> {
                userProfileView.onProfileEditButtonPressed()
                true
            }
            R.id.action_save -> {
                userProfileView.onProfileSaveButtonPressed()
                true
            }
            R.id.action_discard -> {
                userProfileView.onProfileEditDiscardButtonPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        userProfileView.validateUser()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun showPaymentUI(braintreeSDKToken: String) {
        val dropInRequest = DropInRequest().clientToken(braintreeSDKToken)
        startActivityForResult(dropInRequest.getIntent(this), REQ_CODE_BRAINTREE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQ_CODE_BRAINTREE -> extractActivityResultAndPassBackNonce(data)
            }
        } else if (requestCode == REQ_CODE_BRAINTREE) {
            bookingPaymentDetailsWidget.refresh()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun extractActivityResultAndPassBackNonce(data: Intent) {
        val braintreeResult = data.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
        bookingPaymentDetailsWidget.passBackBraintreeSDKNonce(braintreeResult?.paymentMethodNonce?.nonce.orEmpty())
    }

    override fun onProfileUpdateModeChanged(canUpdateProfile: Boolean) {
        this.canUpdateProfile = canUpdateProfile
        invalidateOptionsMenu()
    }

    override fun onProfileEditModeChanged(canEditProfile: Boolean) {
        invalidateOptionsMenu()
    }

    companion object {
        private const val REQ_CODE_BRAINTREE = 301
    }

    class Builder private constructor() {
        private val extras: Bundle = Bundle()

        fun build(context: Context): Intent {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtras(extras)
            return intent
        }

        companion object {
            val builder: Builder
                get() = Builder()
        }
    }
}
