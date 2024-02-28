package io.pascucci.repos.route

import com.tomtom.sdk.common.Result
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.options.calculation.AlternativeRoutesOptions
import com.tomtom.sdk.routing.options.guidance.ExtendedSections
import com.tomtom.sdk.routing.options.guidance.GuidanceOptions
import com.tomtom.sdk.routing.options.guidance.InstructionPhoneticsType
import com.tomtom.sdk.routing.options.guidance.OnlineApiVersion
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.vehicle.Vehicle
import com.tomtom.sdk.vehicle.VehicleType
import io.pascucci.repos.AsyncResult
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

class TomTomRouteSource @Inject constructor(
    private val routePlanner: RoutePlanner
) : IRoutePlanner {
    @Suppress("DEPRECATION")
    override suspend fun plan(
        from: GeoPoint,
        to: GeoPoint,
        type: VehicleType
    ): AsyncResult<List<Route>> {
        val routePlanningOptions = RoutePlanningOptions(
            itinerary = Itinerary(from, to),
//            costModel = CostModel(routeType = RouteType.Fast),
            vehicle = type.toVehicle(),
            alternativeRoutesOptions = AlternativeRoutesOptions(maxAlternatives = 2),
            guidanceOptions = GuidanceOptions(
                language = Locale.ENGLISH,
                phoneticsType = InstructionPhoneticsType.Ipa,
                extendedSections = ExtendedSections.All,
                guidanceVersion = if (type == VehicleType.Car) OnlineApiVersion.v2 else OnlineApiVersion.v1
            )
        ).also {
            IRouteRepository.routePlanningOptions = it
            IRouteRepository.vehicleType = type
        }

        val result = routePlanner.planRoute(routePlanningOptions)
        Timber.d("onPlanRouteResult? $result")
        return when (result) {
            is Result.Success -> AsyncResult.Success(result.value().routes)
            is Result.Failure -> {
                Timber.e(result.failure().message)
                AsyncResult.Error(IllegalStateException(result.failure().message))
            }
        }

//        return suspendCoroutine { continuation ->
//            Timber.d("onRoutePlanning ${Thread.currentThread().toString()}")
//            routePlanner.planRoute(routePlanningOptions, object : RoutePlanningCallback {
//                override fun onFailure(failure: RoutingFailure) {
//                    Timber.e("onRoutePlanned? ${failure.message}")
//                    continuation.resume(AsyncResult.Error(IllegalStateException(failure.message)))
//                }
//
//                override fun onRoutePlanned(route: Route) {
//                    Timber.i("onRoutePlanned? ${route}")
//                }
//
//                override fun onSuccess(result: RoutePlanningResponse) {
//                    Timber.d("onRoutePlanned? onSuccess")
//                    continuation.resume(AsyncResult.Success(result.routes))
//                }
//            })
//        }
    }

    companion object {
        private fun VehicleType.toVehicle() = when (this) {
            VehicleType.Pedestrian -> Vehicle.Pedestrian()
            VehicleType.Bicycle -> Vehicle.Bicycle()
            VehicleType.Bus -> Vehicle.Bus()
            VehicleType.Car -> Vehicle.Car()
            else -> throw IllegalStateException()
        }
    }
}

