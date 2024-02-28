package io.pascucci.ui.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.navigation.RoutePlan
import com.tomtom.sdk.navigation.ui.NavigationFragment
import com.tomtom.sdk.navigation.ui.NavigationFragment.NavigationListener
import com.tomtom.sdk.navigation.ui.NavigationUiOptions
import com.tomtom.sdk.routing.route.Route
import dagger.hilt.android.AndroidEntryPoint
import io.pascucci.R
import io.pascucci.databinding.FragmentDashboardBinding
import io.pascucci.repos.route.IRouteRepository
import io.pascucci.ui.home.MapViewModel
import io.pascucci.ui.info.InfoFragment
import io.pascucci.ui.search.SearchFragment
import javax.inject.Inject

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    @Inject
    lateinit var slidingPanelStateHelper: SlidingPanelStateHelper

    private lateinit var binding: FragmentDashboardBinding
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val mapViewModel: MapViewModel by activityViewModels()

    private val searchFragment get() = childFragmentManager.findFragmentById(R.id.search_fragment_container) as SearchFragment
    private val infoFragment get() = childFragmentManager.findFragmentById(R.id.info_fragment_container) as InfoFragment
    private val navigationFragment: NavigationFragment
        get() {
            return childFragmentManager.findFragmentById(R.id.navigation_fragment_container)?.let {
                it as NavigationFragment
            } ?: NavigationFragment.newInstance(
                NavigationUiOptions(
                    keepInBackground = true
                )
            ).also {
                childFragmentManager.beginTransaction()
                    .replace(R.id.navigation_fragment_container, it).commitNow()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return DataBindingUtil.inflate<FragmentDashboardBinding>(
            inflater, R.layout.fragment_dashboard, container, false
        ).let {
            binding = it
            binding.viewModel = dashboardViewModel
            binding.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapViewModel.focusRouteObservable.observe(viewLifecycleOwner) {
            it?.let { route ->
                startNavigation(route)
            }
        }
        mapViewModel.navigationRouteObservable.observe(viewLifecycleOwner) {
            if (it == null && !mapViewModel.isNavigationIdle) {
                navigationListener.onStopped()
            }
        }
        showSearch()
        slidingPanelStateHelper.slideOffsetObservable.observe(viewLifecycleOwner) {

        }
    }

//    private val fragmentOnAttachListener by lazy {
//        FragmentOnAttachListener { fragmentManager, fragment ->
//            if (fragment.)
//        }
//    }

    private fun startNavigation(route: Route) {
        showNavigation()
        navigationFragment.setTomTomNavigation(mapViewModel.tomTomNavigation)
        val routePlan = RoutePlan(route, IRouteRepository.routePlanningOptions!!)
        navigationFragment.startNavigation(routePlan)
        navigationFragment.addNavigationListener(navigationListener)
        mapViewModel.startNavigation(route)
    }

    private val navigationListener by lazy {
        object : NavigationListener {
            override fun onStarted() {
                mapViewModel.onNavigationStarted { mode ->
                    if (mode == CameraTrackingMode.FollowRouteDirection) {
                        navigationFragment.navigationView.showSpeedView()
                    } else {
                        navigationFragment.navigationView.hideSpeedView()
                    }
                }
            }

            override fun onStopped() {
                navigationFragment.stopNavigation()
                navigationFragment.removeNavigationListener(this)
                mapViewModel.onNavigationStopped()
                showSearch()
            }
        }
    }


    private fun showAfterCollapsed(show: () -> Unit) {
        slidingPanelStateHelper.setSlidingEnable(false)
        slidingPanelStateHelper.slidingUpPanelLayout?.panelState =
            SlidingUpPanelLayout.PanelState.COLLAPSED
        slidingPanelStateHelper.slideOffsetObservable.observe(viewLifecycleOwner) {
            if (it == 0f) {
                slidingPanelStateHelper.slideOffsetObservable.removeObservers(viewLifecycleOwner)
                show()
                slidingPanelStateHelper.setSlidingEnable(true)
            }
        }
    }

    private fun showSearch() = showAfterCollapsed {
        childFragmentManager.beginTransaction()
            .show(searchFragment)
            .hide(infoFragment)
            .hide(navigationFragment)
            .commitNowAllowingStateLoss()
    }

//    private fun showInfo() = showAfterCollapsed {
//        // TODO
//        childFragmentManager.beginTransaction()
//            .hide(searchFragment)
//            .show(infoFragment)
//            .hide(navigationFragment)
//            .commitNowAllowingStateLoss()
//    }

    private fun showNavigation() = showAfterCollapsed {
        childFragmentManager.beginTransaction()
            .hide(searchFragment)
            .hide(infoFragment)
            .show(navigationFragment)
            .commitNowAllowingStateLoss()
        slidingPanelStateHelper.slidingUpPanelLayout?.panelState =
            SlidingUpPanelLayout.PanelState.EXPANDED
    }
}