package io.pascucci.repos.route

import androidx.lifecycle.LiveData
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.routing.route.Route
import io.pascucci.data.Location

interface IRouteRepository {
    val destinationObservable: LiveData<Location>
    val routesObservable: LiveData<List<Route>>
    fun plan(from: GeoPoint, to: GeoPoint)
}
