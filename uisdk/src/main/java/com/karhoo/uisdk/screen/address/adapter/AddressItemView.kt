package com.karhoo.uisdk.screen.address.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.PickupType
import com.karhoo.sdk.api.model.Place
import com.karhoo.sdk.api.model.PoiType
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseRecyclerAdapter

class AddressItemView @JvmOverloads constructor(context: Context,
                                                attrs: AttributeSet? = null,
                                                defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

        private lateinit var itemIcon: ImageView
        private lateinit var addressText: TextView

    init {
        View.inflate(context, R.layout.uisdk_view_address_result_item, this)

        itemIcon = findViewById(R.id.itemIcon)
        addressText = findViewById(R.id.addressText)
    }

    fun bindViews(position: Int,
                  @DrawableRes icon: Int,
                  place: Place,
                  itemClickListener: BaseRecyclerAdapter.OnRecyclerItemClickListener<Place>) {

        itemIcon.setImageResource(icon)
        addressText.text = place.displayAddress

        if (place.type == PoiType.AIRPORT) {
            itemIcon.setImageResource(R.drawable.uisdk_ic_airport)
        } else {
            itemIcon.setImageResource(icon)
        }

        setOnClickListener { view -> itemClickListener.onRecyclerItemClicked(view, position, place) }
    }

    fun bindViews(position: Int,
                  @DrawableRes icon: Int,
                  location: LocationInfo,
                  itemClickListener: BaseRecyclerAdapter.OnRecyclerItemClickListener<LocationInfo>) {

        itemIcon.setImageResource(icon)
        addressText.text = location.displayAddress

        when (location.meetingPoint.pickupType) {
            PickupType.STANDBY,
            PickupType.MEET_AND_GREET,
            PickupType.CURBSIDE -> itemIcon.setImageResource(R.drawable.uisdk_ic_airport)
            else -> itemIcon.setImageResource(icon)
        }

        setOnClickListener { view -> itemClickListener.onRecyclerItemClicked(view, position, location) }
    }

}
