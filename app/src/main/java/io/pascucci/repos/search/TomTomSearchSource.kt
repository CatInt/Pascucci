package io.pascucci.repos.search

import com.tomtom.quantity.Distance
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.SearchOptions
import com.tomtom.sdk.search.model.geometry.CircleGeometry
import com.tomtom.sdk.search.model.result.SearchResult
import io.pascucci.data.Location
import io.pascucci.repos.AsyncResult
import timber.log.Timber
import javax.inject.Inject

class TomTomSearchSource @Inject constructor(
    private val tomtomApi: Search,
) : ISearchDataSource {

    override suspend fun search(query: String, current: GeoPoint?): AsyncResult<List<Location>> =
        try {
            val center = current!!
            val radius = Distance.kilometers(20)
            val circleGeometry = CircleGeometry(center, radius)
            val search = SearchOptions(
                query = query,
                geoBias = current,
                searchAreas = setOf(circleGeometry)
            ).let {
                tomtomApi.search(it)
            }
            val result = if (search.isSuccess()) {
                Timber.d("tomtom search success ${search.value().results.size}")
                search.value().results.map { convertToLocation(it) }
            } else {
                Timber.d("tomtom search failure ${search.failure().message}")
                emptyList()
            }
            AsyncResult.Success(result)
        } catch (e: Exception) {
            AsyncResult.Error(e)
        }

    private fun convertToLocation(item: SearchResult): Location {
        return Location(
            id = item.searchResultId.id,
            name = item.poi?.names?.first() ?: item.place.address?.freeformAddress ?: "Unknown",
            geo = item.place.coordinate,
        )
    }
}
