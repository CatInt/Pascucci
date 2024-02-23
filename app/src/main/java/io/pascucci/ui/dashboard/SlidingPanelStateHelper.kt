package io.pascucci.ui.dashboard

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SlidingPanelStateHelper @Inject constructor() {

    private val _slideOffsetObservable = MutableLiveData(0f)
    val slideOffsetObservable: LiveData<Float> = _slideOffsetObservable

    private val panelSlideListener = object : SlidingUpPanelLayout.PanelSlideListener {
        override fun onPanelSlide(panel: View?, slideOffset: Float) {
            _slideOffsetObservable.postValue(slideOffset)
        }

        override fun onPanelStateChanged(
            panel: View?,
            previousState: SlidingUpPanelLayout.PanelState?,
            newState: SlidingUpPanelLayout.PanelState?
        ) {
            // Do nothing
        }
    }

    var slidingUpPanelLayout: SlidingUpPanelLayout? = null
        set(value) {
            field = value
            slidingUpPanelLayout?.addPanelSlideListener(panelSlideListener)
        }

    fun setSlidingEnable(enable: Boolean) {
        slidingUpPanelLayout?.isTouchEnabled = enable
    }

//    fun setPanelHeight(height: Int) {
//        slidingUpPanelLayout?.apply { panelHeight = height }
//    }

//    @TestOnly
//    fun setSlideOffset(float: Float) {
//        _slideOffsetObservable.postValue(float)
//    }
}