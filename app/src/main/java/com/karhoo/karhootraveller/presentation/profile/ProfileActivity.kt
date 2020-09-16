package com.karhoo.karhootraveller.presentation.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.presentation.profile.user.UserProfileMVP
import com.karhoo.karhootraveller.util.logoutAndResetApp
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentMVP
import kotlinx.android.synthetic.main.activity_profile.bookingPaymentDetailsWidget
import kotlinx.android.synthetic.main.activity_profile.toolbar
import kotlinx.android.synthetic.main.activity_profile.userProfileView

class ProfileActivity : BaseActivity(), PaymentMVP.CardActions, UserProfileMVP
.Actions {

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
        bookingPaymentDetailsWidget.cardActions = this
    }

    override fun handleExtras() {
        // Do nothing
    }

    override fun initialiseViews() {
        // Do nothing
    }

    override fun initialiseViewListeners() {
        // Do nothing
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        bookingPaymentDetailsWidget.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onProfileUpdateModeChanged(canUpdateProfile: Boolean) {
        this.canUpdateProfile = canUpdateProfile
        invalidateOptionsMenu()
    }

    override fun onProfileEditModeChanged(canEditProfile: Boolean) {
        invalidateOptionsMenu()
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

    override fun handleChangeCard() {
        bookingPaymentDetailsWidget.initialiseChangeCard(null)
    }
}
