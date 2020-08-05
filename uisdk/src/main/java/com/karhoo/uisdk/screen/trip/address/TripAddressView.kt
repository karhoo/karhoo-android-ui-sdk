package com.karhoo.uisdk.screen.trip.address

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_view_trip_address.view.dropOffLabel
import kotlinx.android.synthetic.main.uisdk_view_trip_address.view.pickupLabel

class TripAddressView @JvmOverloads constructor(context: Context,
                                                attrs: AttributeSet? = null,
                                                defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.uisdk_view_trip_address, this)
    }

    fun bindTripPickupAndDropoff(tripDetails: TripInfo?) {
        pickupLabel.text = tripDetails?.origin?.displayAddress
        dropOffLabel.text = tripDetails?.destination?.displayAddress
    }

}
