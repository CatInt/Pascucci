package io.pascucci.ui.home

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.map.display.ui.MapReadyCallback
import com.tomtom.sdk.map.display.ui.currentlocation.CurrentLocationButton
import dagger.hilt.android.AndroidEntryPoint
import io.pascucci.R
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var mapViewModel: MapViewModel

    private val mapFragment get() = childFragmentManager.findFragmentById(R.id.map_fragment_container) as MapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapViewModel = ViewModelProvider(requireActivity())[MapViewModel::class.java]
    }

    fun setupMapFragment() {
        mapFragment.getMapAsync(onMapReady)
    }

    private val onMapReady by lazy {
        MapReadyCallback {
            mapViewModel.displayRoutes.observe(viewLifecycleOwner) { routes ->
                Timber.d("${routes?.size} routes showed!")
            }
            mapViewModel.displayMarkers.observe(viewLifecycleOwner) { markers ->
                Timber.d("${markers?.size} Markers showed!")
            }
            mapViewModel.navigationRouteObservable.observe(viewLifecycleOwner) { route ->
                Timber.d("Navigate to $route")
            }
            mapViewModel.setupMap(it)
            mapFragment.currentLocationButton.visibilityPolicy =
                CurrentLocationButton.VisibilityPolicy.InvisibleWhenRecentered
        }
    }
}