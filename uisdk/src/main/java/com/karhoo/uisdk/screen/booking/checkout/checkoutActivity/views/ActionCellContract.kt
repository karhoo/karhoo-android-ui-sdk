package com.karhoo.uisdk.screen.booking.checkout.checkoutActivity.views

interface ActionCellContract {
    interface View {
        fun setActionIcon(iconId: Int)
        fun setTitle(titleId: String)
        fun setSubtitle(subtitleId: String)
    }
}
