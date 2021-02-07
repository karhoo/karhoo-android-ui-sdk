package com.karhoo.karhootraveller.repository

import com.karhoo.karhootraveller.models.Application
import retrofit2.Call
import retrofit2.http.GET

interface ApplicationsAPI {
    @GET("applications/")
    fun loadApplications(): Call<List<Application>>
}