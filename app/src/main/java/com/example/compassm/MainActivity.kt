package com.example.compassm

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.compassm.ui.theme.CompassMTheme
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.viewport
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.*
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.search.*
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion

var mapView: MapView? = null

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        if (!MapboxNavigationApp.isSetup()) {
//            MapboxNavigationApp.setup {
//                NavigationOptions.Builder(this)
//                    .accessToken("YOUR_ACCESS_TOKEN")
//                    // additional options
//                    .build()
//            }
//        }
        setContent {
            CompassMTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen()
{
//    var username by remember { mutableStateOf( TextFieldValue("")) }
    var placeName by remember { mutableStateOf( TextFieldValue("")) }
    var currentLocation: Point? = null
    var destinationLocation: Point? = null
    var LOCALCONTEXT = LocalContext.current;
    var hideSuggestion by remember { mutableStateOf(true) }
    val navigationLocationProvider = NavigationLocationProvider()

//    var mapboxNavigation = MapboxNavigation(NavigationOptions.Builder(LOCALCONTEXT)
//        .accessToken("pk.eyJ1Ijoic29mdGNodW5jayIsImEiOiJjbGFkcmR6bHAwbW9oM3VtaGNpb3lpbDQ4In0.3mzCh9e8kmyv_MCZIpHO5w")
//        .build()
//    )
    var mapView by remember { mutableStateOf( MapView(LOCALCONTEXT).apply {
        getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            cameraOptions {
                zoom(19.0)
            }
            location.updateSettings {
                enabled = true
                pulsingEnabled = true
                pulsingColor = R.color.purple_200
                pulsingMaxRadius = 90F
            }
            viewport.transitionTo(viewport.makeFollowPuckViewportState(
                FollowPuckViewportStateOptions.Builder()
                    .bearing(FollowPuckViewportStateBearing.Constant(0.0))
                    .padding(EdgeInsets(200.0 * resources.displayMetrics.density, 0.0, 0.0, 0.0))
                    .build()
            ))
//            location.apply {
//                setLocationProvider(NavigationLocationProvider())
//                enabled = true
//            }
        }
    } )}
    var searchSuggestions by remember { mutableStateOf(listOf<SearchSuggestion>()) }
    val searchEngine = SearchEngine.createSearchEngine(
        SearchEngineSettings("pk.eyJ1Ijoic29mdGNodW5jayIsImEiOiJjbGFkcmR6bHAwbW9oM3VtaGNpb3lpbDQ4In0.3mzCh9e8kmyv_MCZIpHO5w")
    )
    val searchCallback = object : SearchSuggestionsCallback {
        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
            val suggestion = suggestions.firstOrNull()
        }

        override fun onError(e: Exception) {

        }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        AndroidView(
            modifier = Modifier,
            factory = { context ->
                ResourceOptionsManager.getDefault(
                    context,
                    context.getString(R.string.mapbox_access_token)
                )
                 mapView
            }
        )
    }
    Column (
        modifier = Modifier
            .fillMaxWidth()
            ){
        Row (
            modifier = Modifier
                .padding(all = 4.dp),
        ) {
            TextField(value = placeName,
                onValueChange = {
                    placeName = it
                    searchEngine.search(
                        placeName.text,
                        SearchOptions(limit = 5),
                        object : SearchSuggestionsCallback {

                            override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
//                            val suggestion = suggestions.firstOrNull()
                                searchSuggestions = listOf()
                                hideSuggestion = false
                                suggestions.forEach {
                                        suggestion ->
                                    searchSuggestions = searchSuggestions + suggestion
                                }
                            }

                            override fun onError(e: Exception) {

                                placeName = TextFieldValue("Error")

                            }
                        }
                    )
                                },
//            label = { Text("Search")},
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.White
                    ),
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search Location"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                keyboardActions = KeyboardActions( onDone = {

                }),
                singleLine = true,
                placeholder = { Text(text = "Search Place ... ", color = Color.Gray)}

            )
        }
        Button(onClick = {

            var mapNav = MapboxNavigation(NavigationOptions.Builder(LOCALCONTEXT)
                .accessToken("pk.eyJ1Ijoic29mdGNodW5jayIsImEiOiJjbGFkcmR6bHAwbW9oM3VtaGNpb3lpbDQ4In0.3mzCh9e8kmyv_MCZIpHO5w")
                .build()
            )
                mapNav.requestRoutes(com.mapbox.api.directions.v5.models.RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .coordinatesList(listOf(Point.fromLngLat(72.6506,33.5673),Point.fromLngLat(73.0169,33.5651)))
                .build(),
                object : NavigationRouterCallback {
                    override fun onCanceled(
                        routeOptions: RouteOptions,
                        routerOrigin: RouterOrigin
                    ) {
                        TODO("Not yet implemented")
                    }

                    override fun onFailure(
                        reasons: List<RouterFailure>,
                        routeOptions: RouteOptions
                    ) {
                        Log.d("Failure : ",reasons.toString())
                    }

                    override fun onRoutesReady(
                        routes: List<NavigationRoute>,
                        routerOrigin: RouterOrigin
                    ) {
                        mapNav.setNavigationRoutes(routes)
                    }

                }
            )
        }) {
            Text(text="Find Routes")
        }
        searchSuggestions.forEach { suggestion ->
            if(!hideSuggestion)
            {
                ListItem(
                    headlineText = {Text(text = suggestion.name.toString())},
                    supportingText = { Text(text = suggestion.address?.region.toString()+","+suggestion.address?.country.toString(),
                        color = Color.Gray)},
                    leadingContent = {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = "Location"
                        )
                    },
                    trailingContent = {
                        Icon(
                            Icons.Filled.ArrowForward,
                            contentDescription = "Select"
                        )
                    },
                    colors = ListItemDefaults.colors(MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier
                        .clickable {
                            searchEngine.select(suggestion,
                                object : SearchSelectionCallback {
                                    override fun onResult(
                                        suggestion: SearchSuggestion,
                                        result: SearchResult,
                                        responseInfo: ResponseInfo
                                    ) {
                                        val coordinates = result.coordinate
                                        mapView.annotations.createCircleAnnotationManager().create(CircleAnnotationOptions()
                                            .withPoint(Point.fromLngLat(coordinates.longitude(),coordinates.latitude()))
                                            // Style the circle that will be added to the map.
                                            .withCircleRadius(8.0)
                                            .withCircleColor("#ee4e8b")
                                            .withCircleStrokeWidth(2.0)
                                            .withCircleStrokeColor("#ffffff")
                                        )
//                                        mapView.getMapboxMap().setCamera(CameraOptions.Builder()
//                                            .zoom(14.0)
//                                            .center(coordinates)
//                                            .build()
//                                        )
                                        mapView.getMapboxMap().flyTo(CameraOptions.Builder()
                                            .zoom(14.0)
                                            .center(coordinates)
                                            .build(),
                                            MapAnimationOptions.mapAnimationOptions { duration(4000) }
                                        )
//                                    mapView.camera.apply {
//                                        playAnimatorsTogether(
//                                            createBearingAnimator(CameraAnimatorOptions.cameraAnimatorOptions(coordinates.longitude(),coordinates.latitude()){startValue(15.0)}),
//                                            createPitchAnimator(CameraAnimatorOptions.cameraAnimatorOptions(30.0){startValue(15.0)})
//                                            {duration = 2000}
//                                        )
//                                    }
                                        hideSuggestion = true
                                        if(currentLocation == null)
                                        {
                                            currentLocation = coordinates
                                        }
                                        else{
                                            destinationLocation = coordinates
                                        }
//                                    searchSuggestions = listOf()
                                    }

                                    override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                                    }

                                    override fun onCategoryResult(
                                        suggestion: SearchSuggestion,
                                        results: List<SearchResult>,
                                        responseInfo: ResponseInfo
                                    ) {
                                    }

                                    override fun onError(e: Exception) {
                                    }
                                })
                        }
                )
                Divider()
            }
            }
    }
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//
//        }

    }
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CompassMTheme {

    }
}
