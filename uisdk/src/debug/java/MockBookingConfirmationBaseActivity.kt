package com.karhoo.uisdk.booking.checkout.bookingconfirmation

import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.screen.booking.checkout.bookingconfirmation.BookingConfirmationView
import com.karhoo.uisdk.screen.booking.checkout.component.views.CheckoutViewContract
import com.karhoo.uisdk.screen.booking.checkout.loyalty.LoyaltyMode

class MockBookingConfirmationBaseActivity : BaseActivity() {
    override val layout: Int
        get() = com.karhoo.uisdk.R.layout.uisdk_mock_booking_confirmation_layout

    override fun handleExtras() {
        val bookingConfirmationView = BookingConfirmationView(
            intent.getParcelableExtra("SCREENSHOT_TEST_JOURNEY_DETAILS")!!,
            intent.getParcelableExtra("SCREENSHOT_TEST_QUOTE")!!,
            null,
            null,
            null,
            true
        )

        bookingConfirmationView.setLoyaltyProperties(
            intent.getBooleanExtra("SCREENSHOT_TEST_LOYALTY_VISIBILITY", false),
            LoyaltyMode.BURN,
            10
        )

        bookingConfirmationView.actions = object : CheckoutViewContract.BookingConfirmationActions {
            override fun openRideDetails() {
                val activity = this@MockBookingConfirmationBaseActivity
                activity.finish()
            }

            override fun dismissedPrebookDialog() {
                val activity = this@MockBookingConfirmationBaseActivity
                activity.finish()
            }
        }

        this.supportFragmentManager.let {
            bookingConfirmationView.show(it, BookingConfirmationView.TAG)
        }
    }
}