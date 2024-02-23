package io.pascucci.ui.home

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import com.tomtom.sdk.common.UniqueId
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.location.mapmatched.MapMatchedLocationProvider
import com.tomtom.sdk.location.simulation.SimulationLocationProvider
import com.tomtom.sdk.location.simulation.strategy.InterpolationStrategy
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraChangeListener
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.map.display.gesture.MapLongClickListener
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.marker.Label
import com.tomtom.sdk.map.display.marker.MarkerClickListener
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.route.Instruction
import com.tomtom.sdk.map.display.route.RouteClickListener
import com.tomtom.sdk.map.display.route.RouteOptions
import com.tomtom.sdk.navigation.ActiveRouteChangedListener
import com.tomtom.sdk.navigation.NavigationState
import com.tomtom.sdk.navigation.ProgressUpdatedListener
import com.tomtom.sdk.navigation.RouteAddedListener
import com.tomtom.sdk.navigation.RouteAddedReason
import com.tomtom.sdk.navigation.RouteRemovedListener
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.routing.route.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import io.pascucci.R
import io.pascucci.data.PascucciLocationProvider
import io.pascucci.repos.route.IRouteRepository
import io.pascucci.repos.search.ISearchRepository
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

// This is shared ViewModel interacting with navigation and map
@HiltViewModel
class MapViewModel @Inject internal constructor(
    private val locationProvider: LocationProvider,
    private val routeRepo: IRouteRepository,
    val tomTomNavigation: TomTomNavigation,
    searchRepo: ISearchRepository,
) : ViewModel() {

    private val _observableMap = MutableLiveData<TomTomMap>()
    private val observableMap: LiveData<TomTomMap> = _observableMap
    private var _displayMap: TomTomMap? = null
    private val displayMap: TomTomMap get() = _displayMap!!

    val displayMarkers = combine(
        observableMap.asFlow(),
        searchRepo.destinationsObservable.asFlow()
    ) { displayMap, destinations ->
        displayMap.clear()
        destinations.map {
            val marker = it.run {
                MarkerOptions(
                    coordinate = geo,
                    pinImage = ImageFactory.fromResource(R.drawable.ic_geo_marker),
                    label = Label(name)
                )
            }
            displayMap.addMarker(marker)
        }.also {
            displayMap.zoomToMarkers()
        }
    }.asLiveData()

    private val routesCache = mutableMapOf<UniqueId, Route>()
    val displayRoutes = combine(
        observableMap.asFlow(),
        routeRepo.routesObservable.asFlow()
    ) { displayMap, routes ->
        displayMap.clear()
        routesCache.clear()
        routes.reversed().map {
            drawRoute(it).also { displayRoute ->
                routesCache[displayRoute.id] = it
            }
        }.also {
            displayMap.zoomToRoutes(24)
        }
    }.asLiveData()

    private val currentPos: GeoPoint? get() = locationProvider.lastKnownLocation?.position

    fun setupMap(displayMap: TomTomMap) {
        _displayMap = displayMap
        displayMap.setLocationProvider(locationProvider)
        centerOnCurrent()
        locationProvider.enable()
        setUpMapListeners(displayMap)
        _observableMap.postValue(displayMap)
    }

    private fun setUpMapListeners(displayMap: TomTomMap) {
        displayMap.addMapLongClickListener(mapLongClickListener)
        displayMap.addMarkerClickListener(markerClickListener)
        displayMap.addRouteClickListener(routeClickListener)
    }

    fun centerOnCurrent() {
        (locationProvider as PascucciLocationProvider).switchToOrigin()
        // zoom to current location at city level
        var onLocationUpdateListener: OnLocationUpdateListener? = null
        onLocationUpdateListener = OnLocationUpdateListener { location ->
            displayMap.animateCamera(CameraOptions(location.position, zoom = 10.0))
            locationProvider.removeOnLocationUpdateListener(onLocationUpdateListener!!)
        }
        locationProvider.addOnLocationUpdateListener(onLocationUpdateListener)
        val locationMarker = LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
        displayMap.enableLocationMarker(locationMarker)
    }

    private var _focusRouteObservable = MutableLiveData<Route>()
    val focusRouteObservable: LiveData<Route> = _focusRouteObservable

    private var _navigationRouteObservable = MutableLiveData<Route?>()
    val navigationRouteObservable: LiveData<Route?> = _navigationRouteObservable

    fun startNavigation(route: Route) {
        _navigationRouteObservable.postValue(route)
        bindNavigationToMap()
    }

    private fun bindNavigationToMap() {
        tomTomNavigation.addProgressUpdatedListener(progressUpdatedListener)
        tomTomNavigation.addActiveRouteChangedListener(activeRouteChangedListener)
        tomTomNavigation.addRouteAddedListener(routeAddedListener)
        tomTomNavigation.addRouteRemovedListener(routeRemovedListener)
    }

    private fun unbindNavigationToMap() {
        tomTomNavigation.removeProgressUpdatedListener(progressUpdatedListener)
        tomTomNavigation.removeActiveRouteChangedListener(activeRouteChangedListener)
        tomTomNavigation.removeRouteAddedListener(routeAddedListener)
        tomTomNavigation.removeRouteRemovedListener(routeRemovedListener)
    }

    private var cameraChangeListener: CameraChangeListener? = null
    fun onNavigationStarted(onCameraChanged: (CameraTrackingMode) -> Unit) {
        cameraChangeListener = CameraChangeListener {
            onCameraChanged(displayMap.cameraTrackingMode)
        }
        displayMap.addCameraChangeListener(cameraChangeListener!!)
        displayMap.cameraTrackingMode = CameraTrackingMode.FollowRouteDirection
        displayMap.enableLocationMarker(LocationMarkerOptions(LocationMarkerOptions.Type.Chevron))
        setMapMatchedLocationProvider()
        setSimulationLocationProviderToNavigation(navigationRouteObservable.value!!)
        setMapNavigationPadding()
    }

    fun onNavigationStopped() {
        _navigationRouteObservable.postValue(null)
        displayMap.cameraTrackingMode = CameraTrackingMode.None
        displayMap.enableLocationMarker(LocationMarkerOptions(LocationMarkerOptions.Type.Pointer))
        displayMap.removeCameraChangeListener(cameraChangeListener!!)
        unbindNavigationToMap()
        displayMap.clear()
        centerOnCurrent()
    }

    private fun setMapNavigationPadding() {
        // Do nothing for now
    }

    private val progressUpdatedListener = ProgressUpdatedListener {
        displayMap.routes.first().progress = it.distanceAlongRoute
    }

    private val isNavigationIdle get() = tomTomNavigation.navigationState == NavigationState.Idle

    private val mapLongClickListener by lazy {
        MapLongClickListener { pos ->
            if (isNavigationIdle) {
                displayMap.clear()
                routeRepo.plan(currentPos!!, pos)
            }
            true
        }
    }

    private val markerClickListener by lazy {
        MarkerClickListener { marker ->
            if (isNavigationIdle) {
                routeRepo.plan(currentPos!!, marker.coordinate)
                displayMap.removeMarkers()
            }
        }
    }

    private val routeClickListener by lazy {
        RouteClickListener { route ->
            if (isNavigationIdle) {
                displayRoutes.value?.forEach { it.color = Color.LTGRAY }
                route.color = Color.GREEN
                route.bringToFront()
                _focusRouteObservable.postValue(routesCache[route.id])
            }
        }
    }

    private val routeAddedListener by lazy {
        RouteAddedListener { route, _, routeAddedReason ->
            if (routeAddedReason !is RouteAddedReason.NavigationStarted) {
                drawRoute(
                    route = route,
                    color = Color.GRAY,
                    withDepartureMarker = false
                )
            }
        }
    }

    private fun drawRoute(
        route: Route,
        color: Int = RouteOptions.DEFAULT_COLOR,
        withDepartureMarker: Boolean = true
    ) = createRouteOptions(route, withDepartureMarker, color).let {
        displayMap.addRoute(it)
    }

    private val activeRouteChangedListener by lazy {
        ActiveRouteChangedListener { route ->
            displayMap.routes.forEach {
                if (it.tag == route.id.toString()) {
                    it.color = RouteOptions.DEFAULT_COLOR
                } else {
                    it.color = Color.GRAY
                }
            }
        }
    }

    private fun setSimulationLocationProviderToNavigation(route: Route) {
        val routeGeoLocations = route.geometry.map { GeoLocation(it) }
        val simulationStrategy = InterpolationStrategy(routeGeoLocations)
        val simulationProvider = SimulationLocationProvider.create(strategy = simulationStrategy)
        tomTomNavigation.locationProvider = simulationProvider
        simulationProvider.enable()
    }

    private fun setMapMatchedLocationProvider() {
        val provider = MapMatchedLocationProvider(tomTomNavigation)
        (locationProvider as PascucciLocationProvider).switch(provider)
    }

    private val routeRemovedListener by lazy {
        RouteRemovedListener { route, _ ->
            displayMap.routes.find { it.tag == route.id.toString() }?.remove()
        }
    }

    companion object {

        private fun createRouteOptions(
            route: Route,
            withDepartureMarker: Boolean,
            color: Int
        ): RouteOptions {
            val instructions = route.legs
                .flatMap { routeLeg -> routeLeg.instructions }
                .map {
                    Instruction(
                        routeOffset = it.routeOffset
                    )
                }
            return RouteOptions(
                geometry = route.geometry,
                destinationMarkerVisible = true,
                departureMarkerVisible = withDepartureMarker,
                instructions = instructions,
                routeOffset = route.routePoints.map { it.routeOffset },
                color = color,
                tag = route.id.toString()
            )
        }
    }
}