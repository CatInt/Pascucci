package io.pascucci

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.tomtom.sdk.location.LocationProvider
import dagger.hilt.android.AndroidEntryPoint
import io.pascucci.ui.dashboard.DashboardFragment
import io.pascucci.ui.dashboard.SlidingPanelStateHelper
import io.pascucci.ui.home.HomeFragment
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var slidingPanelStateHelper: SlidingPanelStateHelper
    @Inject
    lateinit var locationProvider: LocationProvider
    private val homeFragment get() = supportFragmentManager.findFragmentById(R.id.home_container) as HomeFragment
    private val dashboardFragment get() = supportFragmentManager.findFragmentById(R.id.dashboard_container) as DashboardFragment?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Make UI take full screen
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setupSliding()
    }

    override fun onResume() {
        super.onResume()

        if (isPermissionsGranted) {
            setupUi()
        } else {
            dashboardFragment?.also {
                supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
            }
            requestPermissions()
        }
    }


    override fun onDestroy() {
        locationProvider.disable()
        super.onDestroy()
    }

    private fun setupSliding() {
        slidingPanelStateHelper.slidingUpPanelLayout = findViewById(R.id.activity_main)
        slidingPanelStateHelper.setSlidingEnable(false)
    }

    private fun setupUi() {
        if (homeFragment.hasSetup)
        {
            return
        }
        homeFragment.setupMapFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.dashboard_container, DashboardFragment()).commitNow()
        slidingPanelStateHelper.setSlidingEnable(true)
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
            setupUi()
        } else {
            Toast.makeText(
                this,
                getString(R.string.location_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val isPermissionsGranted
        get() = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}