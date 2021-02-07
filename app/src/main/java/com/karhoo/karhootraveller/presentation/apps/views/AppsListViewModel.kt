package com.karhoo.karhootraveller.presentation.apps.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.karhoo.karhootraveller.models.Application
import com.karhoo.karhootraveller.repository.AppsDataSource

class AppsListViewModel(val dataSource: AppsDataSource) : ViewModel() {

    fun insertApplication(app: Application) {
        dataSource.addApplication(app)
    }

    private val appsLiveData: MutableLiveData<List<Application>> by lazy {
        initialiseApps()
    }

    fun getApps(): LiveData<List<Application>> {
        return appsLiveData
    }

    fun fetchApps() {
        dataSource.loadApps()
    }

    private fun initialiseApps(): MutableLiveData<List<Application>> {
        return dataSource.getAppsList()
    }
}
