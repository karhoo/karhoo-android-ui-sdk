package com.karhoo.uisdk.screen.address.adapter

import android.content.Context
import android.view.ViewGroup
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseRecyclerAdapter
import com.karhoo.uisdk.base.BaseRecyclerView

class RecentAddressAdapter(private val context: Context) : BaseRecyclerAdapter<LocationInfo, AddressItemView>() {

    override fun onCreateItemView(parent: ViewGroup, viewType: Int): AddressItemView {
        return AddressItemView(context)
    }

    override fun onBindViewHolder(holder: BaseRecyclerView<AddressItemView>, position: Int) {
        val view = holder.view
        val addresses = items
        val location = addresses[position]

        view.bindViews(position, R.drawable.uisdk_ic_location_recent, location, itemClickListener)
    }
}
