package io.pascucci.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.tomtom.sdk.location.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.pascucci.data.Location
import io.pascucci.repos.route.IRouteRepository
import io.pascucci.repos.search.ISearchRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject internal constructor(
    private val searchRepo: ISearchRepository,
    private val routeRepo: IRouteRepository,
    private var locationProvider: LocationProvider
) : ViewModel() {

    val listAdapter = SearchResultListAdapter { dest ->
        locationProvider.lastKnownLocation?.position?.let { from ->
            routeRepo.plan(from, dest.geo)
        }
    }

    val searchResults: LiveData<List<Location>> = searchRepo.destinationsObservable

    fun search(query: String) {
        if (query.isNotEmpty()) {
            Timber.d("Search $query")
            searchRepo.search(query)
        }
    }
}