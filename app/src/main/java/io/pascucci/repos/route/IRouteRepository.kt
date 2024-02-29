package io.pascucci.repos.route

import androidx.lifecycle.LiveData
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.vehicle.VehicleType
import io.pascucci.data.Location

interface IRouteRepository {
    val destinationObservable: LiveData<Location>
    val routesObservable: LiveData<List<Route>>
    val vehicleTypeObservable: LiveData<VehicleType>
    suspend fun plan(from: GeoPoint, to: GeoPoint, type: VehicleType?)
    suspend fun setVehicleType(type: VehicleType)

    companion object {
        var routePlanningOptions: RoutePlanningOptions? = null
        var vehicleType: VehicleType? = null
    }
}
