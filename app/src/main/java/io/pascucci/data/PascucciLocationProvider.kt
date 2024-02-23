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
        _current = provider
        if (currentProvider != old) {
            if (old == original)
            {
                old.disable()
            } else {
                old.close()
            }
            currentProvider.enable()
        }
    }
}