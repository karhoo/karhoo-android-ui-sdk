package com.karhoo.uisdk.screen.booking.checkout.component.views

interface ActionCellContract {
    interface View {
        fun setActionIcon(iconId: Int)
        fun setDottedBackground(show: Boolean)
        fun setTitle(titleId: String)
        fun setSubtitle(subtitleId: String)
    }
}
