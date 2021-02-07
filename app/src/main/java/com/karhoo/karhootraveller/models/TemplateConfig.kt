package com.karhoo.karhootraveller.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TemplateConfig(
		@SerializedName("primary_color") val primaryColor : String,
		@SerializedName("secondary_color") val secondaryColor : String,
						 ): Parcelable