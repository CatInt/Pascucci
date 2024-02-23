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

    override fun plan(from: GeoPoint, to: GeoPoint) {
        Timber.d("onRouteRepo plan ${Thread.currentThread()}")
        runBlocking(dispatchers.io) {
            Timber.d("onRouteRepo ${Thread.currentThread()}")
            val result = planner.plan(from, to)
            when (result) {
                is AsyncResult.Success<List<Route>> -> result.data
                else -> emptyList()
            }.let { list ->
                _routesObservable.postValue(list)
            }
        }
    }
}