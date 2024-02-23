package io.pascucci

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import io.pascucci.ui.dashboard.SlidingPanelStateHelper
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var slidingPanelStateHelper: SlidingPanelStateHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Make UI take full screen
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setupDashboard()
    }

    private fun setupDashboard() {
        slidingPanelStateHelper.slidingUpPanelLayout = findViewById(R.id.activity_main)
//        slidingPanelStateHelper.slidingUpPanelLayout!!.isTouchEnabled = false
        slidingPanelStateHelper.setSlidingEnable(true)
    }
}