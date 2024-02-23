/*
 * *******************************************************************************
 *  ** Copyright (C), 2014-2021, OnePlus Mobile Comm Corp., Ltd
 *  ** All rights reserved.
 *  *******************************************************************************
 */

package io.pascucci.repos.search

import com.tomtom.sdk.location.GeoPoint
import io.pascucci.data.Location
import io.pascucci.repos.AsyncResult

interface ISearchDataSource {
    suspend fun search(query: String, current: GeoPoint?): AsyncResult<List<Location>>
}