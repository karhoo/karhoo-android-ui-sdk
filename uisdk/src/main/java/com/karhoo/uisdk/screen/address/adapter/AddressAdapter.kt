package com.karhoo.uisdk.screen.address.adapter

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.karhoo.sdk.api.model.Place
import com.karhoo.uisdk.R
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.BaseRecyclerAdapter
import com.karhoo.uisdk.base.BaseRecyclerView
import com.karhoo.uisdk.screen.address.domain.Addresses

class AddressAdapter(private val context: Context,
                     private val analytics: Analytics?)
    : BaseRecyclerAdapter<Place, AddressItemView>() {

    private var query: String = ""

    override fun onCreateItemView(parent: ViewGroup, viewType: Int): AddressItemView {
        val view = AddressItemView(context)
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return view
    }

    fun setItems(addresses: Addresses) {
        this.query = addresses.query.query
        items = addresses.locations.locations
    }

    fun addressSelected() {
        analytics?.userEnteredTextSearch(query)
    }

    override fun onBindViewHolder(holder: BaseRecyclerView<AddressItemView>, position: Int) {
        val view = holder.view
        val addresses = items
        val place = addresses[position]

        view.bindViews(position, R.drawable.uisdk_ic_location_pin, place, itemClickListener)

        view.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        val layoutParams = view.layoutParams.width
        Log.d("PD36", layoutParams.toString())
    }

}
