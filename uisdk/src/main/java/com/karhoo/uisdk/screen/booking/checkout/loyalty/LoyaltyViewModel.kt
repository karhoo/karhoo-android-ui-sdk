package com.karhoo.uisdk.screen.booking.checkout.loyalty

data class LoyaltyViewModel(val loyaltyId: String,
                            val currency: String,
                            val tripAmount: Double,
                            var canEarn: Boolean,
                            var canBurn: Boolean)
