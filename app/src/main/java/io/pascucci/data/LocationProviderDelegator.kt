package io.pascucci.data

import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener

abstract class LocationProviderDelegator : LocationProvider {
    abstract val currentProvider: LocationProvider

    private val _listeners: MutableList<OnLocationUpdateListener> = mutableListOf()
    private val listener = OnLocationUpdateListener { geo ->
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

    override fun close() = currentProvider.close().also {
        currentProvider.removeOnLocationUpdateListener(listener)
    }

    override fun disable() = currentProvider.disable().also {
        currentProvider.removeOnLocationUpdateListener(listener)
    }

    override fun enable() = currentProvider.enable().also {
        currentProvider.addOnLocationUpdateListener(listener)
    }

    override fun removeOnLocationUpdateListener(listener: OnLocationUpdateListener) {
        _listeners.remove(listener)
    }
}