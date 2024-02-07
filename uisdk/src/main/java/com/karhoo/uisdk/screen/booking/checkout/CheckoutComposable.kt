package com.karhoo.uisdk.screen.booking.checkout

import AddressStaticComponentComposable
import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity.Companion.BOOKING_CHECKOUT_JOURNEY_DETAILS_KEY
import com.karhoo.uisdk.screen.booking.checkout.component.views.BookingPriceComposable
import com.karhoo.uisdk.screen.booking.checkout.loyalty.LoyaltyComposable
import com.karhoo.uisdk.screen.booking.checkout.quotes.BookingVehicleDetailsComposable
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails

@Composable
fun CheckoutComposable(
    extras: Bundle?,
    onCheckoutAction: () -> Unit
) {
    Text(text = "Checkout")
    // Use state to manage data and UI state
    val (isLoading, setIsLoading) = remember { mutableStateOf(false) }

    // Use effect handlers to perform side-effects (like loading data)
    LaunchedEffect(key1 = Unit) {
        setIsLoading(true)
        // Load your data here
        setIsLoading(false)
    }

    // Display your UI here
    Column {
        if (isLoading) {
            // Display a loading indicator
            CircularProgressIndicator()
        } else {
            // Display your custom views here
            // You would need to create separate Composables for each custom view
            extras?.getParcelable<JourneyDetails>(BOOKING_CHECKOUT_JOURNEY_DETAILS_KEY)
                ?.let { AddressStaticComponentComposable(it) }
            BookingVehicleDetailsComposable(extras?.getParcelable(CheckoutActivity.BOOKING_CHECKOUT_QUOTE_KEY)!!)
                BookingPriceComposable(extras.getParcelable(CheckoutActivity.BOOKING_CHECKOUT_QUOTE_KEY)!!)
                LoyaltyComposable(
                    balance = "Balance",
                    earnTitle = "Earn",
                    earnSubtitle = "Earn subtitle",
                    burnTitle = "Burn",
                    burnSubtitle = "Burn subtitle",
                    infoText = "Info text",
                    isInfoVisible = true,
                    isSwitchChecked = true,
                    onSwitchCheckedChange = { }
                )
//                ActionCellViewComposable()
//                BookingTermsViewComposable()
//                LegalNoticeViewComposable()

//                BottomPriceViewComposable()
//                LoadingButtonViewComposable(onClick = onCheckoutAction)

//                PassengerDetailsViewComposable()
        }
    }
}