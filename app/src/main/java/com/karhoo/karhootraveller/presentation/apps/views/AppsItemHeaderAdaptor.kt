package com.karhoo.karhootraveller.presentation.apps.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.karhoo.karhootraveller.R

class AppsItemHeaderAdaptor : RecyclerView.Adapter<AppsItemHeaderAdaptor.HeaderViewHolder>() {
    private var appsCount: Int = 0
    private var appsTitle: String = "Apps"

    /* ViewHolder for displaying header. */
    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val headerText: TextView = itemView.findViewById(R.id.header_text)
        private val appsHeaderNumber: TextView = itemView.findViewById(R.id.apps_number_text)

        fun bind(title: String, appsCount: Int) {
            headerText.text = title
            appsHeaderNumber.text = appsCount.toString()
        }
    }

    /* Inflates view and returns HeaderViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_apps_item_header, parent, false)
        return HeaderViewHolder(view)
    }

    /* Binds number of flowers to the header. */
    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind(appsTitle, appsCount)
    }

    /* Returns number of items, since there is only one item in the header return one  */
    override fun getItemCount(): Int {
        return 1
    }

    /* Updates header to display number of flowers when a flower is added or subtracted. */
    fun updateAppsCount(title: String, updatedAppsCount: Int) {
        appsTitle = title
        appsCount = updatedAppsCount
        notifyDataSetChanged()
    }
}
