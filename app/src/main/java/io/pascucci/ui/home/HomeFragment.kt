package io.pascucci.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_FADE
import com.google.android.material.snackbar.Snackbar
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.map.display.ui.MapReadyCallback
import com.tomtom.sdk.map.display.ui.currentlocation.CurrentLocationButton
import dagger.hilt.android.AndroidEntryPoint
import io.pascucci.R
import io.pascucci.databinding.FragmentHomeBinding
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    @Suppress("unused")
    private val homeViewModel: HomeViewModel by viewModels()
    private val mapViewModel: MapViewModel by activityViewModels()

    private val mapFragment get() = childFragmentManager.findFragmentById(R.id.map_fragment_container) as MapFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return DataBindingUtil.inflate<FragmentHomeBinding>(
            inflater,
            R.layout.fragment_home,
            container,
            false
        ).let {
            binding = it
            it.root
        }
    }

    var hasSetup = false
    fun setupMapFragment() {
        hasSetup = true
        mapFragment.getMapAsync(onMapReady)
    }

    private val onMapReady by lazy {
        MapReadyCallback {
            binding.lifecycleOwner = this
            mapViewModel.displayRoutes.observe(viewLifecycleOwner) { routes ->
                Timber.d("${routes?.size} routes showed!")
            }
            mapViewModel.displayMarkers.observe(viewLifecycleOwner) { markers ->
                Timber.d("${markers?.size} Markers showed!")
            }
            mapViewModel.navigationRouteObservable.observe(viewLifecycleOwner) { route ->
                Timber.d("Navigate to $route")
            }
            mapViewModel.guideMessage.observe(viewLifecycleOwner) { msg ->
                announce(msg)
            }
            mapViewModel.setupMap(it)
            mapFragment.currentLocationButton.visibilityPolicy =
                CurrentLocationButton.VisibilityPolicy.InvisibleWhenRecentered
        }
    }

    private fun announce(msg: String) {
        Snackbar.make(requireContext(), binding.root, msg, Snackbar.LENGTH_LONG)
            .setAnchorView(binding.snackbarAnchor)
            .setAnimationMode(ANIMATION_MODE_FADE)
            .show()
    }
}