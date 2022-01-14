package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LoyaltyInfo(val loyaltyEnabled: Boolean,
                       val loyaltyCanBurn: Boolean,
                       val loyaltyCanEarn: Boolean): Parcelable
