package com.karhoo.karhootraveller.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Application(
        @SerializedName("config") val config: AppConfig,
        @SerializedName("description") val description: String,
        @SerializedName("id") val id: String,
        @SerializedName("is_active") val is_active: Boolean,
        @SerializedName("name") val name: String,
        @SerializedName("organisation_id") val organisation_id: String,
        @SerializedName("status") val status: String,
                      ) : Parcelable