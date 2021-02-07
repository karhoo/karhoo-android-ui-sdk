package com.karhoo.karhootraveller.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Auth(
        @SerializedName("anonymous") val anonymous: Anonymous
               ) : Parcelable