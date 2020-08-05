package com.karhoo.uisdk.screen.trip.summary

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.karhoo.uisdk.R

class PickupDropOffFullView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.uisdk_view_pickup_dropoff, this)
    }
}