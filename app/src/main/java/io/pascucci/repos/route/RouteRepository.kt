/*
 * *******************************************************************************
 *  ** Copyright (C), 2014-2021, OnePlus Mobile Comm Corp., Ltd
 *  ** All rights reserved.
 *  *******************************************************************************
 */

package io.pascucci.repos.route

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.vehicle.VehicleType
import io.pascucci.AppCoroutineDispatchers
import io.pascucci.data.Location
import io.pascucci.repos.AsyncResult
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class RouteRepository(
    private val planner: IRoutePlanner,
    private val dispatchers: AppCoroutineDispatchers
) : IRouteRepository {

    private val _destinationObservable = MutableLiveData<Location>()
    override val destinationObservable get() = _destinationObservable

    private val _routesObservable = MutableLiveData<List<Route>>()
    override val routesObservable: LiveData<List<Route>> = _routesObservable

    private val _vehicleTypeObservable = MutableLiveData<VehicleType>()
    override val vehicleTypeObservable: LiveData<VehicleType> = _vehicleTypeObservable

    override fun setVehicleType(type: VehicleType) {
        val old = vehicleTypeObservable.value
        if (old != type) {
            if (routesObservable.hasObservers() && cache.isValid()) {
                plan(cache.from!!, cache.to!!, type)
            } else {
                internalSetVehicleType(type)
            }
        }
    }

    private fun internalSetVehicleType(type: VehicleType) {
        _vehicleTypeObservable.postValue(type)
    }

    private var cache = ParamsCache(null, null)
    override fun plan(from: GeoPoint, to: GeoPoint, type: VehicleType?) {
        val vehicleType = type ?: vehicleTypeObservable.value ?: VehicleType.Car
        internalSetVehicleType(vehicleType)
        Timber.d("onRouteRepo plan ${Thread.currentThread()}")
        cache.from = from
        cache.to = to
        runBlocking(dispatchers.io) {
            Timber.d("onRouteRepo ${Thread.currentThread()}")
            val result = planner.plan(from, to, vehicleType)
            when (result) {
                is AsyncResult.Success<List<Route>> -> result.data
                else -> emptyList()
            }.let { list ->
                _routesObservable.postValue(list)
            }
        }
    }

    data class ParamsCache(
        var from: GeoPoint? = null,
        var to: GeoPoint? = null
    ) {
        fun isValid() = from != null && to != null
    }
}