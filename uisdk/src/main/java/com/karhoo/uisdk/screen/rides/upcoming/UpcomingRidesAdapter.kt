package com.karhoo.uisdk.screen.rides.upcoming

import android.view.ViewGroup
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.base.BaseRecyclerAdapter
import com.karhoo.uisdk.base.BaseRecyclerView
import com.karhoo.uisdk.screen.rides.upcoming.card.UpcomingRideCardView

class UpcomingRidesAdapter : BaseRecyclerAdapter<TripInfo, UpcomingRideCardView>() {

    override fun onCreateItemView(parent: ViewGroup, viewType: Int): UpcomingRideCardView {
        return UpcomingRideCardView(parent.context)
    }

    override fun onBindViewHolder(holder: BaseRecyclerView<UpcomingRideCardView>, position: Int) {
        val trip = items[position]
        holder.view.bind(trip)
    }

}
