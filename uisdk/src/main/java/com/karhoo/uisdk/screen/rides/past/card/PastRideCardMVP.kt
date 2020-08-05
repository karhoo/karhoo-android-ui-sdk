package com.karhoo.uisdk.screen.rides.past.card

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.base.ScheduledDateView

interface PastRideCardMVP {

    interface Presenter {
        fun selectDetails()

        fun bindState()

        fun bindPrice()

        fun bindDate()
    }

    interface View : ScheduledDateView {
        fun goToDetails(trip: TripInfo)

        fun displayState(@DrawableRes icon: Int, @StringRes state: Int, @ColorRes color: Int)

        fun displayPricePending()

        fun displayPrice(price: String)
    }

}
