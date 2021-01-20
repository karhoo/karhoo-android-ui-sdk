package com.karhoo.karhootraveller.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.MenuItem
import com.karhoo.karhootraveller.presentation.about.AboutActivity
import com.karhoo.karhootraveller.presentation.karhooLabs.KarhooLabsActivity
import com.karhoo.karhootraveller.presentation.profile.ProfileActivity
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.MenuHandler
import com.karhoo.uisdk.screen.booking.domain.support.KarhooFeedbackEmailComposer
import com.karhoo.uisdk.screen.rides.RidesActivity

class KHMenuHandler : MenuHandler {

    override fun onNavigationItemSelected(context: Context, item: MenuItem) {
        when (item.itemId) {
            R.id.action_profile -> {
                val profileIntent = ProfileActivity.Builder.builder
                        .build(context)
                context.startActivity(profileIntent)
            }
            R.id.action_feedback -> {
                val activity = context as Activity
                val emailComposer = KarhooFeedbackEmailComposer(context)
                val showFeedbackMail = emailComposer.showFeedbackMail()
                showFeedbackMail?.let { intent ->
                    activity.startActivity(intent)
                }
            }
            R.id.action_rides -> {
                val ridesIntent = RidesActivity.Builder.builder
                        .build(context)
                context.startActivity(ridesIntent)
            }
            R.id.action_about -> {
                val aboutIntent = Intent(context, AboutActivity::class.java)
                context.startActivity(aboutIntent)
            }
            R.id.action_karhoo_labs -> {
                val featureFlagIntent = Intent(context, KarhooLabsActivity::class.java)
                context.startActivity(featureFlagIntent)            }
        }
    }
}
