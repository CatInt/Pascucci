/*
 * *******************************************************************************
 *  ** Copyright (C), 2014-2021, OnePlus Mobile Comm Corp., Ltd
 *  ** All rights reserved.
 *  *******************************************************************************
 */

package io.pascucci.repos.route

import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.routing.route.Route
import io.pascucci.repos.AsyncResult

interface IRoutePlanner {
    suspend fun plan(from: GeoPoint, to: GeoPoint): AsyncResult<List<Route>>
}