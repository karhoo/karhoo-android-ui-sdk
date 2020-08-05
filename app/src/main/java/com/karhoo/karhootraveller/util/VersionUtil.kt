package com.karhoo.karhootraveller.util

import android.content.Context
import android.os.Build

object VersionUtil {

    fun createBuildVersionString(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName + " (" + packageInfo.versionCode + ")"
        } catch (e: Exception) {
            ""
        }

    }

    fun getVersionString(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "Version: " + packageInfo.versionName
        } catch (e: Exception) {
            ""
        }

    }

    fun appAndDeviceInfo(): String {
        val apiLevel = Build.VERSION.SDK_INT
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER

        return "Device: " + manufacturer.toUpperCase() + " " + model.toUpperCase() + " (Android OS: " + apiLevel + ")\n"
    }
}
