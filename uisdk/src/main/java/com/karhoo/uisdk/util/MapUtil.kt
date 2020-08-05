package com.karhoo.uisdk.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object MapUtil {

    fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        var vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable?.let {
            vectorDrawable = DrawableCompat.wrap(it).mutate()
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            val bitmap = Bitmap.createBitmap(it.intrinsicWidth, it.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            it.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }
        return null
    }

}
