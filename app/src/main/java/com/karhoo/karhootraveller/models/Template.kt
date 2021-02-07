package com.karhoo.karhootraveller.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Template(
        @SerializedName("config") val templateConfig: TemplateConfig,
        @SerializedName("name") val name: String,
        @SerializedName("version") val version: String
                   ): Parcelable