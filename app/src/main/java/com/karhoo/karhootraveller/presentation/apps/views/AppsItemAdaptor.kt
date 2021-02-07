package com.karhoo.karhootraveller.presentation.apps.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.models.Application

class AppsItemAdaptor(private val onClick: (Application) -> Unit) :
        ListAdapter<Application, AppsItemAdaptor.AppsViewHolder>(AppsDiffCallback) {

    /* ViewHolder for Flower, takes in the inflated view and the onClick behavior. */
    class AppsViewHolder(itemView: View, val onClick: (Application) -> Unit) :
            RecyclerView.ViewHolder(itemView) {
        private val appTitleView: TextView = itemView.findViewById(R.id.app_title_text)
        private val appDescriptionView: TextView = itemView.findViewById(R.id.app_description_text)
        private var currentApplication: Application? = null

        init {
            itemView.setOnClickListener {
                currentApplication?.let {
                    onClick(it)
                }
            }
        }

        /* Bind flower name and image. */
        fun bind(application: Application) {
            currentApplication = application

            appTitleView.text = application.name
            appDescriptionView.text = application.description
        }
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppsViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_apps_item, parent, false)
        return AppsViewHolder(view, onClick)
    }

    /* Gets current flower and uses it to bind view. */
    override fun onBindViewHolder(holder: AppsViewHolder, position: Int) {
        val flower = getItem(position)
        holder.bind(flower)

    }
}

object AppsDiffCallback : DiffUtil.ItemCallback<Application>() {
    override fun areItemsTheSame(oldItem: Application, newItem: Application): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Application, newItem: Application): Boolean {
        return oldItem.id == newItem.id
    }
}