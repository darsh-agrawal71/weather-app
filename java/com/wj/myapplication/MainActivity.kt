package com.wj.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.twotone.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wj.myapplication.api.Weather
import com.wj.myapplication.api.WeatherApi
import com.wj.myapplication.api.Wind
import com.wj.myapplication.ui.theme.MyApplicationTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                App()
            }
        }
    }
}

fun makeDataRouteWithCity(city: String): String = "data/$city"
fun formatCityName(city: String): String {
    val builder = StringBuilder(city.lowercase())
    builder[0] = builder[0].uppercaseChar()
    return builder.toString()
}

@Composable
fun App() {
    val backgroundGradientColors: List<Color> = listOf(
        Color(0xFF458EDF), Color(0xFF93F9F1)
    )

    val backgroundGradientBrush: Brush = Brush.verticalGradient(backgroundGradientColors)
    val navHostController: NavHostController = rememberNavController()

    /* Navigation */
    val input: String = "input"
    val data: String = "data/{city}"/* Navigation */

    Scaffold(
        Modifier.background(backgroundGradientBrush),
        containerColor = Color.Transparent,
        contentColor = Color.White
    ) {
        Column(Modifier.padding(it)) {
            Text(
                "⛅",
                style = TextStyle(fontSize = 192.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Weather",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            NavHost(navController = navHostController, startDestination = input) {
                composable(input) {
                    InputFragment(navHostController)
                }
                composable(data) { navBackStackEntry ->
                    val city = navBackStackEntry.arguments?.getString("city")!!
                    DataFragment(navHostController, city)
                }
            }
        }
    }
}

@Composable
fun InputFragment(navController: NavController) {
    var cityField: String by remember { mutableStateOf("") }
    Card(Modifier.padding(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = cityField,
                onValueChange = { cityField = it },
                Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.PinDrop, "")},
                label = { Text("City") },
                supportingText = { Text("We don't store this data.") },
                singleLine = true
            )
            Button(
                onClick = { navController.navigate(makeDataRouteWithCity(cityField)) },
                Modifier.fillMaxWidth()
            ) {
                Text("Go")
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DataFragment(navController: NavController, city: String) {
    val City: String = formatCityName(city)
    var isApiLoading: Boolean by remember { mutableStateOf(!true) }
    var weather: Weather by remember { mutableStateOf(Weather()) }
    var wind: Wind by remember { mutableStateOf(Wind()) }


    LaunchedEffect(City) {
        WeatherApi.getWeatherForCity(City) { we: Weather, wi: Wind ->
            isApiLoading = false
            weather = we
            wind = wi
        }
    }


    Card(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(Modifier.padding(8.dp)) {
            AnimatedContent(
                targetState = isApiLoading,
                transitionSpec = {
                    slideInVertically { height -> -height } + fadeIn() togetherWith
                            slideOutVertically { height -> height } + fadeOut() using SizeTransform(
                        clip = false
                    )

                }, label = "ApiLoadedAnimation"
            ) { loading ->
                if (loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier
                                .padding(8.dp)
                                .height(IntrinsicSize.Min)) {
                                Text(
                                    City,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.ExtraLight,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "${Weather.kelvinToCelsius(weather.temp)}°C",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.ExtraLight
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row {
                                    Icon(Icons.Filled.Thermostat, "")
                                    Text(
                                        "Temperature",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                ListItem(
                                    headlineContent = { Text("Feels Like") },
                                    trailingContent = { Text("${Weather.kelvinToCelsius(weather.feels_like)}°C", style = MaterialTheme.typography.bodyMedium) },

                                )
                                ListItem(
                                    headlineContent = { Text("Minimum") },
                                    trailingContent = { Text("${Weather.kelvinToCelsius(weather.temp_min)}°C", style = MaterialTheme.typography.bodyMedium) },

                                )
                                ListItem(
                                    headlineContent = { Text("Maximum") },
                                    trailingContent = { Text("${Weather.kelvinToCelsius(weather.temp_max)}°C", style = MaterialTheme.typography.bodyMedium) },

                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .fillMaxSize()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row {
                                        Icon(Icons.Filled.Air, "")
                                        Text(
                                            "Wind",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }

                                    ListItem(
                                        headlineContent = { Text("Speed") },
                                        trailingContent = { Text("${wind.speed}", style = MaterialTheme.typography.bodyMedium) },

                                    )
                                    ListItem(
                                        headlineContent = { Text("Direction") },
                                        trailingContent = { Text("${wind.deg}", style = MaterialTheme.typography.bodyMedium) },
                                        
                                    )
                                }
                            }
                            Column(modifier = Modifier
                                .weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                ) {
                                    Column(modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)) {
                                        Row {
                                            Icon(Icons.Filled.WaterDrop, "")
                                            Text(
                                                " Humidity",
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                        }
                                        Text("${weather.humidity.roundToInt()}%", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
                                    }
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                ) {
                                    Column(modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)) {
                                        Row {
                                            Icon(Icons.Filled.Speed, "")
                                            Text(
                                                " Pressure",
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                        }
                                        Text("${weather.pressure.roundToInt()} mB", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
    }

}

@Composable
fun ListItem(headlineContent: @Composable () -> Unit, trailingContent: @Composable () -> Unit) {
    Row {
        Box(Modifier.weight(1f)) {
            headlineContent()
        }
        Box {
            trailingContent()
        }

    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    MyApplicationTheme {
        App()
    }
}

@Preview
@Composable
private fun InputFragmentPreview() {
    MyApplicationTheme {
        InputFragment(navController = rememberNavController())
    }
}

@Preview
@Composable
private fun DataFragmentPreview() {
    MyApplicationTheme {
        DataFragment(navController = rememberNavController(), city = "Mumbai")
    }
}