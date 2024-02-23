package io.pascucci.repos.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import io.pascucci.AppCoroutineDispatchers
import io.pascucci.data.Location
import io.pascucci.repos.AsyncResult
import kotlinx.coroutines.runBlocking

class SearchRepository(
//    private val cacheStore: CacheStore,
    private val searchDataSource: ISearchDataSource,
    private val locationProvider: LocationProvider,
    private val dispatchers: AppCoroutineDispatchers
) : ISearchRepository {

    private val currentGeoPoint: GeoPoint?
        get() = locationProvider.lastKnownLocation?.position

    private val _locationsObservable = MutableLiveData<List<Location>>()
    override val destinationsObservable: LiveData<List<Location>>
        get() = _locationsObservable

    override fun search(query: String) {

        if (query.isBlank()) {
            _locationsObservable.postValue(emptyList())
            return
        }

//        val cached = cacheStore.getResults(query)
//        if (cached != null) {
//            return cached.map { channelDao.getChannel(it)!! }
//        }

        runBlocking(dispatchers.io) {
            searchDataSource.search(query, currentGeoPoint).let { result ->
                if (result is AsyncResult.Success<List<Location>>) {
                    result.data
                } else {
                    emptyList()
                }
            }.also {
                _locationsObservable.postValue(it)
            }
        }
    }

}