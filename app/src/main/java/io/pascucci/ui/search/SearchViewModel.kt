package io.pascucci.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.vehicle.VehicleType
import dagger.hilt.android.lifecycle.HiltViewModel
import io.pascucci.data.Location
import io.pascucci.repos.route.IRouteRepository
import io.pascucci.repos.search.ISearchRepository
import kotlinx.coroutines.launch
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
            viewModelScope.launch {
                routeRepo.plan(from, dest.geo, null)
            }
        }
    }

    val searchResults: LiveData<List<Location>> = searchRepo.destinationsObservable
    val vehicleType: LiveData<VehicleType> = routeRepo.vehicleTypeObservable

    fun search(query: String) {
        if (query.isNotEmpty()) {
            viewModelScope.launch {
                Timber.d("Search $query")
                searchRepo.search(query)
            }
        }
    }

    fun setCurrentRouteType(t: Int) {
        val map = arrayOf(VehicleType.Car, VehicleType.Bus, VehicleType.Bicycle, VehicleType.Pedestrian)
        viewModelScope.launch {
            routeRepo.setVehicleType(map[t])
        }
    }
}