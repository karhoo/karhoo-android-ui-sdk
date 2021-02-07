package com.karhoo.karhootraveller.repository

import androidx.lifecycle.MutableLiveData
import com.karhoo.karhootraveller.models.Anonymous
import com.karhoo.karhootraveller.models.AppConfig
import com.karhoo.karhootraveller.models.Application
import com.karhoo.karhootraveller.models.Auth
import com.karhoo.karhootraveller.models.Template
import com.karhoo.karhootraveller.models.TemplateConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppsDataSource {
    private var initialAppsList = listOf<Application>()
    private var appsLiveData = MutableLiveData(initialAppsList)

    fun allApps(): List<Application> {
        val apps = mutableListOf<Application>()
        apps.add(Application(config = AppConfig(auth = Auth(anonymous =
                                                            Anonymous
                                                            ("ApWdGzxUFQBlVZbIaYdRMnkHJiWItHFS")),
                                                base_uris = listOf("https://emails.book.stg.karhoo.net"), namespace = "niko",
                                                primary_domain = "", template = Template
        (templateConfig = TemplateConfig(primaryColor = "#0062B1", secondaryColor = "#9F0500"),
         name = "default", version = "0.1.0")), is_active = true, name = "Niko Bookers",
                             organisation_id = "a1013897-132a-456c-9be2-636979095ad9", status =
                             "ready", id = "wjJzUPmGlVOL2W3dbZ4mhmTcaRTiwYVT", description =
                             "Private booker for testing purposes"))
        apps.add(Application(config = AppConfig(auth = Auth(anonymous =
                                                            Anonymous
                                                            ("ApWdGzxUFQBlVZbIaYdRMnkHJiWItHFS")),
                                                base_uris = listOf(), namespace = "jaro",
                                                primary_domain = "", template = Template
        (templateConfig = TemplateConfig(primaryColor = "#c45100", secondaryColor = "#653294"),
         name = "default", version = "0.1.0")), is_active = true, name = "Jareks booker",
                             organisation_id = "a1013897-132a-456c-9be2-636979095ad9", status =
                             "ready", id = "iROnhRmxyBPpHxFsORHqcwiKcfOSh7tC", description =
                             "Jareks booker"))

        return apps
    }

    /* Adds flower to liveData and posts value. */
    fun addApplication(app: Application) {
        val currentList = appsLiveData.value
        if (currentList == null) {
            appsLiveData.postValue(listOf(app))
        } else {
            val updatedList = currentList.toMutableList()
            updatedList.add(0, app)
            appsLiveData.postValue(updatedList)
        }
    }

    fun getAppsList(): MutableLiveData<List<Application>> {
        return appsLiveData
    }

    fun loadApps() {
        val request = ServiceBuilder.buildService(ApplicationsAPI::class.java)
        val call = request.loadApplications()

        call.enqueue(object : Callback<List<Application>> {
            override fun onResponse(call: Call<List<Application>>, response: Response<List<Application>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        initialAppsList = it
                        appsLiveData.postValue(initialAppsList)
                    }
                } else {
                    initialAppsList = allApps()
                    appsLiveData.postValue(initialAppsList)
                }
            }

            override fun onFailure(call: Call<List<Application>>, t: Throwable) {

            }
        })
    }

    companion object {
        private var INSTANCE: AppsDataSource? = null

        fun getDataSource(): AppsDataSource {
            return synchronized(AppsDataSource::class) {
                val newInstance = INSTANCE ?: AppsDataSource()
                INSTANCE = newInstance
                newInstance
            }
        }
    }
}