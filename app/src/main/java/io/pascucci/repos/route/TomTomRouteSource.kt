package io.pascucci.repos.route

import com.tomtom.sdk.common.Result
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.options.calculation.AlternativeRoutesOptions
import com.tomtom.sdk.routing.options.calculation.CostModel
import com.tomtom.sdk.routing.options.calculation.RouteType
import com.tomtom.sdk.routing.options.guidance.ExtendedSections
import com.tomtom.sdk.routing.options.guidance.GuidanceOptions
import com.tomtom.sdk.routing.options.guidance.InstructionPhoneticsType
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.vehicle.Vehicle
import io.pascucci.repos.AsyncResult
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

class TomTomRouteSource @Inject constructor(
    private val routePlanner: RoutePlanner
) : IRoutePlanner {
    override suspend fun plan(from: GeoPoint, to: GeoPoint): AsyncResult<List<Route>> {
        routePlanningOptions = RoutePlanningOptions(
            itinerary = Itinerary(from, to),
            costModel = CostModel(routeType = RouteType.Efficient),
            vehicle = Vehicle.Car(),
            alternativeRoutesOptions = AlternativeRoutesOptions(maxAlternatives = 2),
            guidanceOptions = GuidanceOptions(
                language = Locale.ENGLISH,
                phoneticsType = InstructionPhoneticsType.Ipa,
                extendedSections = ExtendedSections.All
            )
        )

        val result = routePlanner.planRoute(routePlanningOptions!!)
        Timber.d("onPlanRouteResult? $result")
        return when (result) {
            is Result.Success -> AsyncResult.Success(result.value().routes)
            is Result.Failure -> AsyncResult.Error(IllegalStateException(result.failure().message))
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
        var routePlanningOptions: RoutePlanningOptions? = null
    }
}