package io.pascucci.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
        mapViewModel.displayRoutes.observe(viewLifecycleOwner) {
            Timber.d("${it?.size} routes showed!")
        }
        mapViewModel.displayMarkers.observe(viewLifecycleOwner) {
            Timber.d("${it?.size} Markers showed!")
        }
        mapViewModel.navigationRouteObservable.observe(viewLifecycleOwner) {
            Timber.d("Navigate to $it")
        }
        setupMapFragment()
    }

    private fun setupMapFragment() {
        mapFragment.getMapAsync(onMapReady)
    }

    private val onMapReady by lazy {
        MapReadyCallback {
            if (isPermissionsGranted) {
                mapViewModel.setupMap(it)
                mapFragment.currentLocationButton.visibilityPolicy =
                    CurrentLocationButton.VisibilityPolicy.InvisibleWhenRecentered
            } else {
                requestPermissions()
            }
        }
    }

    private fun requestPermissions() {
        permissionsRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private val permissionsRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            mapViewModel.centerOnCurrent()
        } else {
            Toast.makeText(
                requireActivity(),
                getString(R.string.location_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val isPermissionsGranted
        get() = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}