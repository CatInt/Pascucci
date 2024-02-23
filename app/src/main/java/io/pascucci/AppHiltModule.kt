package io.pascucci

import android.content.Context
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStore
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStoreConfiguration
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.LocationProviderType
import com.tomtom.sdk.location.simulation.SimulationLocationProvider
import com.tomtom.sdk.location.simulation.strategy.TimestampStrategy
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.online.Configuration
import com.tomtom.sdk.navigation.online.OnlineTomTomNavigationFactory
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.online.OnlineRoutePlanner
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.online.OnlineSearch
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.pascucci.data.PascucciLocationProvider
import io.pascucci.repos.route.IRouteRepository
import io.pascucci.repos.route.IRoutePlanner
import io.pascucci.repos.route.RouteRepository
import io.pascucci.repos.route.TomTomRouteSource
import io.pascucci.repos.search.ISearchRepository
import io.pascucci.repos.search.ISearchDataSource
import io.pascucci.repos.search.SearchRepository
import io.pascucci.repos.search.TomTomSearchSource
import java.time.LocalDate
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppHiltModule {

    @Singleton
    @Provides
    fun provideLocationProvider(): LocationProvider = TimestampStrategy(
        listOf(
            GeoLocation(
                position = GeoPoint(52.379189, 4.899431),
                accuracy = null,
                course = null,
                speed = null,
                altitude = null,
                time = LocalDate.now().toEpochDay(),
                elapsedRealtimeNanos = LocalDate.now().toEpochDay() / 1000L,
                provider = "",
                providerType = LocationProviderType.SOFT_DR,
                extras = null
            )
        )
    ).let {
        val p = SimulationLocationProvider.create(it)
        PascucciLocationProvider(p)
    }

    @Singleton
    @Provides
    fun provideSearchRepository(
//        cacheStore: CacheStore,
        searchDataSource: ISearchDataSource,
        locationProvider: LocationProvider,
        dispatchers: AppCoroutineDispatchers
    ): ISearchRepository =
        SearchRepository(/*cacheStore, */searchDataSource, locationProvider, dispatchers)

    @Singleton
    @Provides
    fun provideTomTomSearch(
        @ApplicationContext context: Context
    ): Search = OnlineSearch.create(context, BuildConfig.TOMTOM_API_KEY)

    @Singleton
    @Provides
    fun provideRouteRepository(
        routeDataSource: IRoutePlanner,
        dispatchers: AppCoroutineDispatchers,
    ): IRouteRepository = RouteRepository(routeDataSource, dispatchers)

    @Singleton
    @Provides
    fun provideTomTomPlanner(
        @ApplicationContext context: Context
    ): RoutePlanner = OnlineRoutePlanner.create(context, BuildConfig.TOMTOM_API_KEY)

    @Singleton
    @Provides
    fun provideTomTomNavigationTileStore(
        @ApplicationContext context: Context
    ): NavigationTileStore = NavigationTileStore.create(
        context = context,
        navigationTileStoreConfig = NavigationTileStoreConfiguration(BuildConfig.TOMTOM_API_KEY)
    )

    @Singleton
    @Provides
    fun provideTomTomNavigation(
        @ApplicationContext context: Context,
        navigationTileStore: NavigationTileStore,
        locationProvider: LocationProvider,
        routePlanner: RoutePlanner,
    ): TomTomNavigation = Configuration(
        context,
        navigationTileStore,
        locationProvider,
        routePlanner
    ).let {
        OnlineTomTomNavigationFactory.create(it)
    }


}

@InstallIn(SingletonComponent::class)
@Module
abstract class ProductionHiltModules {

    @Singleton
    @Binds
    abstract fun provideCoroutineDispatchers(dispatchers: DefaultAppCoroutineDispatchers): AppCoroutineDispatchers

    @Singleton
    @Binds
    abstract fun provideSearchDataSource(source: TomTomSearchSource): ISearchDataSource

    @Singleton
    @Binds
    abstract fun provideRoutePlanner(source: TomTomRouteSource): IRoutePlanner

}