package com.karhoo.karhootraveller.presentation.apps.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.models.Application
import com.karhoo.karhootraveller.repository.AppsDataSource

class AppsView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr), AppsMVP.View {

    var actions: AppsMVP.Actions? = null

    private val appsListViewModel by lazy {
        AppsListViewModel(AppsDataSource.getDataSource())
    }

    init {
        inflate(context, R.layout.view_apps, this)
        onResume()
    }

    override fun onResume() {
        val headerAdapter = AppsItemHeaderAdaptor()
        val appsItemAdapter = AppsItemAdaptor { app -> adapterOnClick(app) }
        val concatAdapter = ConcatAdapter(headerAdapter, appsItemAdapter)
        val recyclerView: RecyclerView = findViewById(R.id.listApps)
        recyclerView.apply {
            adapter = concatAdapter
            layoutManager = LinearLayoutManager(context)
        }

        val apps = appsListViewModel.getApps()
        apps.observe(context as AppCompatActivity, Observer { list ->
            appsItemAdapter.submitList(list)
            headerAdapter.updateAppsCount("Applications", list.size)
        })

        appsListViewModel.fetchApps()
        val appList = apps.value
        appsItemAdapter.submitList(appList)
        headerAdapter.updateAppsCount("Applications", appList?.size ?: 0)

    }

    private fun adapterOnClick(application: Application) {
        actions?.didSelectApplication(application)
    }

}