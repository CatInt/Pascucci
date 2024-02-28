package io.pascucci.data

import com.tomtom.sdk.location.LocationProvider
import javax.inject.Inject

class PascucciLocationProvider @Inject constructor(
    private val original: LocationProvider
) : LocationProviderDelegator() {
    private var _current: LocationProvider = original

    override val currentProvider: LocationProvider
        get() = _current

    fun switchToOrigin() {
        switch(original)
    }

    fun switch(provider: LocationProvider) {
        val old = currentProvider
        if (provider != old) {
            if (old == original) {
                disable()
            } else {
                close()
            }
            _current = provider
            enable()
        }
    }
}