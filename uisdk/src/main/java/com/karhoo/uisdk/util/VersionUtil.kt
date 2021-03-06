package com.karhoo.uisdk.util

import android.content.Context
import android.os.Build
import androidx.annotation.NonNull
import androidx.annotation.Nullable

interface VersionUtilContact {
    @NonNull
    fun createBuildVersionString(context: Context): String

    @Nullable
    fun getAppNameString(context: Context): String?

    @Nullable
    fun getVersionString(context: Context): String?

    fun appAndDeviceInfo(): String
}

object VersionUtil: VersionUtilContact {

    @NonNull
    override fun createBuildVersionString(context: Context): String {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionName + " (" + packageInfo.versionCode + ")"
        } catch (e: Exception) {
            return ""
        }
    }

    @Nullable
    override fun getAppNameString(context: Context): String? {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString()
    }

    @Nullable
    override fun getVersionString(context: Context): String? {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return "Version: " + packageInfo.versionName
        } catch (e: Exception) {
            return null
        }
    }

    override fun appAndDeviceInfo(): String {
        val apiLevel = Build.VERSION.SDK_INT
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER

        return "Device: " + manufacturer.toUpperCase() + " " + model.toUpperCase() + " (Android OS: " + apiLevel + ")"
    }
}
