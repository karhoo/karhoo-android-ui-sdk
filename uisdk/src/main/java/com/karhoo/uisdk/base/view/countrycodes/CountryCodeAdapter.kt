package com.karhoo.uisdk.base.view.countrycodes

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

class CountryCodeAdapter(private val items: Array<String>) : BaseAdapter() {

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return buildView(position, convertView, parent)
    }

    private fun buildView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val itemView = getCountryCodeItemView(convertView, parent)
        itemView.bind(items[position])
        return itemView
    }

    private fun getCountryCodeItemView(convertView: View?, parent: ViewGroup?): CountryCodeItemView {
        return if (convertView == null && parent?.context != null) {
            CountryCodeItemView(parent.context)
        } else {
            convertView as CountryCodeItemView
        }
    }
}
