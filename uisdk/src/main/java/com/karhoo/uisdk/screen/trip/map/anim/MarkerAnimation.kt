package com.karhoo.uisdk.screen.trip.map.anim

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.util.Property

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

object MarkerAnimation {

    fun animateMarkerTo(marker: Marker, finalPosition: LatLng,
                        latLngInterpolator: LatLngInterpolator,
                        duration: Long) {
        val typeEvaluator = TypeEvaluator<LatLng> { fraction, startValue, endValue -> latLngInterpolator.interpolate(fraction, startValue, endValue) }
        val property = Property.of(Marker::class.java, LatLng::class.java, "position")
        val animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition)
        animator.duration = duration
        animator.start()
    }

}
