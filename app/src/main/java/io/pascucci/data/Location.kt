package io.pascucci.data

import com.tomtom.sdk.location.GeoPoint

data class Location(
    val id: String,
    val name: String,
    val geo: GeoPoint
)
