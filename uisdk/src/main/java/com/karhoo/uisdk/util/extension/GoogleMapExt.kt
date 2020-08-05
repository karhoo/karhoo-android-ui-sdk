package com.karhoo.uisdk.util.extension

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.SphericalUtil
import java.util.ArrayList

private const val DEFAULT_CURVE_ROUTE_CURVATURE = 0.2
private const val DEFAULT_CURVE_POINTS = 100
private const val MAX_LINE_DISTANCE = 1000000

fun GoogleMap.showCurvedPolyline(origin: LatLng, destination: LatLng, lineColor: Int) {
    val distance = SphericalUtil.computeDistanceBetween(origin, destination)

    if (distance > MAX_LINE_DISTANCE) {
        return
    }
    //make sure west point is always first
    var p1 = origin
    var p2 = destination

    if (origin.longitude >= destination.longitude) {
        p1 = destination
        p2 = origin
    }

    val heading = SphericalUtil.computeHeading(p1, p2)
    val halfDistance = distance / 2

    // Calculate midpoint position
    val midPoint = SphericalUtil.computeOffset(p1, halfDistance, heading)

    // Calculate position of the curve center point
    val sqrCurvature = DEFAULT_CURVE_ROUTE_CURVATURE * DEFAULT_CURVE_ROUTE_CURVATURE
    val extraParam = distance / (4 * DEFAULT_CURVE_ROUTE_CURVATURE)
    val midPerpendicularLength = (1 - sqrCurvature) * extraParam
    val r = (1 + sqrCurvature) * extraParam

    val circleCenterPoint = SphericalUtil.computeOffset(midPoint, midPerpendicularLength, heading + 90.0)

    // Calculate heading between circle center and two points
    val headingToOrigin = SphericalUtil.computeHeading(circleCenterPoint, p1)

    // Calculate positions of points on the curve
    val step = Math.toDegrees(Math.atan(halfDistance / midPerpendicularLength)) * 2 / DEFAULT_CURVE_POINTS

    val points = ArrayList<LatLng>()
    for (i in 0 until DEFAULT_CURVE_POINTS) {
        points.add(SphericalUtil.computeOffset(circleCenterPoint, r, headingToOrigin + i * step))
    }

    //Draw polyline
    this.addPolyline(PolylineOptions()
                             .width(17F)
                             .color(lineColor)
                             .geodesic(false)
                             .addAll(points)
                             .startCap(RoundCap())
                             .endCap(RoundCap()))
}

fun GoogleMap.showShadowedPolyLine(origin: LatLng, destination: LatLng, lineColor: Int) {
    val distance = SphericalUtil.computeDistanceBetween(origin, destination)

    if (distance > MAX_LINE_DISTANCE) {
        return
    }

    this.addPolyline(PolylineOptions()
                             .add(origin, destination)
                             .width(15F)
                             .color(lineColor)
                             .endCap(RoundCap())
                             .startCap(RoundCap()))
}


