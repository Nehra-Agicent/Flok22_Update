package com.flok22.android.agicent.utils

import java.lang.Math.*

fun getDistanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Long {
    val l1: Double = toRadians(lat1)
    val l2: Double = toRadians(lat2)
    val g1: Double = toRadians(lng1)
    val g2: Double = toRadians(lng2)
    var dist: Double = acos(sin(l1) * sin(l2) + cos(l1) * cos(l2) * cos(g1 - g2))
    if (dist < 0) {
        dist += PI
    }
    return round(dist * 6378100)
}

fun getDistanceInMetre(distance: String): Int {
    val distanceInDouble = distance.toDoubleOrNull()
    return if (distanceInDouble != null) {
        (distanceInDouble * 1000).toInt()
    } else 0
}
