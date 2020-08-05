package com.karhoo.uisdk.screen.rides.past

import android.view.ViewGroup
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.base.BaseRecyclerAdapter
import com.karhoo.uisdk.base.BaseRecyclerView
import com.karhoo.uisdk.screen.rides.past.card.PastRideCardView

class PastRidesAdapter : BaseRecyclerAdapter<TripInfo, PastRideCardView>() {

    override fun onCreateItemView(parent: ViewGroup, viewType: Int): PastRideCardView {
        return PastRideCardView(parent.context)
    }

    override fun onBindViewHolder(holder: BaseRecyclerView<PastRideCardView>, position: Int) {
        val trip = items[position]
        holder.view.bind(trip)
    }

}
