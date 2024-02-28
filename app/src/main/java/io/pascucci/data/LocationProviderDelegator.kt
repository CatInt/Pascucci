package io.pascucci.data

import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import timber.log.Timber

abstract class LocationProviderDelegator : LocationProvider {
    abstract val currentProvider: LocationProvider

    private val _listeners: MutableList<OnLocationUpdateListener> = mutableListOf()
    private val listener = OnLocationUpdateListener { geo ->
        Timber.d("OnLocationUpdate $geo")
        _listeners.forEach { it.onLocationUpdate(geo) }
    }

    override val lastKnownLocation: GeoLocation?
        get() = currentProvider.lastKnownLocation

    override fun addOnLocationUpdateListener(listener: OnLocationUpdateListener) {
        _listeners.add(listener)
        lastKnownLocation?.also {
            listener.onLocationUpdate(it)
        }
    }

    override fun close() {
        currentProvider.removeOnLocationUpdateListener(listener)
        currentProvider.close()
    }

    override fun disable() {
        currentProvider.removeOnLocationUpdateListener(listener)
        currentProvider.disable()
    }

    override fun enable() {
        currentProvider.addOnLocationUpdateListener(listener)
        currentProvider.enable()
    }

    override fun removeOnLocationUpdateListener(listener: OnLocationUpdateListener) {
        _listeners.remove(listener)
    }
}