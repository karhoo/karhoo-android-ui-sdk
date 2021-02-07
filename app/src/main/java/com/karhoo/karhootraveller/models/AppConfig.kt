package com.karhoo.karhootraveller.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppConfig(
        @SerializedName("auth") val auth: Auth? = null,
        @SerializedName("base_uris") val base_uris: List<String>,
        @SerializedName("namespace") val namespace: String,
        @SerializedName("primary_domain") val primary_domain: String,
        @SerializedName("template") val template: Template
                    ) : Parcelable