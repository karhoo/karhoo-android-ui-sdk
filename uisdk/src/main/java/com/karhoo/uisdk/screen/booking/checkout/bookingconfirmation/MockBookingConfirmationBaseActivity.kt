package com.karhoo.uisdk.screen.booking.checkout.bookingconfirmation

import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.screen.booking.checkout.component.views.CheckoutViewContract
import com.karhoo.uisdk.screen.booking.checkout.loyalty.LoyaltyMode

internal class MockBookingConfirmationBaseActivity : BaseActivity() {
    override val layout: Int
        get() = R.layout.uisdk_mock_booking_confirmation_layout

    override fun handleExtras() {
        val bookingConfirmationView = BookingConfirmationView(
            intent.getParcelableExtra("SCREENSHOT_TEST_JOURNEY_DETAILS")!!,
            intent.getParcelableExtra("SCREENSHOT_TEST_QUOTE")!!,
            null,
            null,
            null
        )

        bookingConfirmationView.setLoyaltyProperties(
            intent.getBooleanExtra("SCREENSHOT_TEST_LOYALTY_VISIBILITY", false),
            LoyaltyMode.BURN,
            LOYALTY_POINTS
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

    companion object {
        private const val LOYALTY_POINTS: Int = 10
    }
}
