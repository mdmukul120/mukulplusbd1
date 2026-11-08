package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class CityLocation(
    val name: String,
    val lat: Double,
    val lon: Double
)

val BangladeshCities = listOf(
    CityLocation("Dhaka", 23.8103, 90.4125),
    CityLocation("Chattogram", 22.3569, 91.7832),
    CityLocation("Sylhet", 24.8949, 91.8687),
    CityLocation("Rajshahi", 24.3636, 88.6241),
    CityLocation("Khulna", 22.8456, 89.5403),
    CityLocation("Barishal", 22.7010, 90.3535),
    CityLocation("Rangpur", 25.7439, 89.2752),
    CityLocation("Mymensingh", 24.7471, 90.4203),
    CityLocation("Cox's Bazar", 21.4272, 92.0058)
)

@Composable
fun WeatherScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val weather by viewModel.weather.collectAsState()
    val isLoading by viewModel.isWeatherLoading.collectAsState()

    var selectedCity by remember { mutableStateOf(BangladeshCities[0]) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = BrandCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WEATHER FORECAST",
                        color = BrandCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "Live Meteorological Updates • Bangladesh",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = {
                    viewModel.loadWeather(selectedCity.lat, selectedCity.lon, selectedCity.name)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = BrandCyan
                )
            }
        }

        // Cities Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(BangladeshCities) { city ->
                val isSelected = (city.name == selectedCity.name)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedCity = city
                        viewModel.loadWeather(city.lat, city.lon, city.name)
                    },
                    label = { Text(city.name, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandCyan,
                        selectedLabelColor = DarkBackground,
                        containerColor = DarkSurfaceCard,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) BrandCyan else DarkBorder
                    )
                )
            }
        }

        if (isLoading && weather == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandCyan)
            }
        } else {
            weather?.let { w ->
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Main Weather Hero
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .testTag("weather_hero_card"),
                            color = DarkSurfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandCyan.copy(alpha = 0.4f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF0F2B48),
                                                Color(0xFF091424)
                                            )
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = BrandCyan,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = w.city,
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Text(
                                            text = "Bangladesh",
                                            color = BrandGold,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "${w.temp.toInt()}°C",
                                                color = Color.White,
                                                fontSize = 48.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                text = w.condition,
                                                color = BrandCyan,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "H: ${w.maxTemp.toInt()}°C  •  L: ${w.minTemp.toInt()}°C",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 12.sp
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(76.dp)
                                                .clip(CircleShape)
                                                .background(BrandCyan.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getWeatherIcon(w.conditionCode),
                                                contentDescription = w.condition,
                                                tint = BrandGold,
                                                modifier = Modifier.size(46.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Metrics: Humidity & Wind
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        WeatherMetric(
                                            icon = Icons.Default.WaterDrop,
                                            label = "Humidity",
                                            value = "${w.humidity}%",
                                            color = BrandCyan
                                        )
                                        WeatherMetric(
                                            icon = Icons.Default.Air,
                                            label = "Wind Speed",
                                            value = "${w.windSpeed} km/h",
                                            color = BrandGreen
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 7-Day Forecast Section
                    item {
                        Text(
                            text = "7-Day Forecast",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(w.dailyList) { daily ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            color = DarkSurfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = daily.dayName,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.width(80.dp)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = getWeatherIcon(daily.conditionCode),
                                        contentDescription = daily.condition,
                                        tint = BrandGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = daily.condition,
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${daily.maxTemp.toInt()}°",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${daily.minTemp.toInt()}°",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
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
fun WeatherMetric(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, color = TextSecondary, fontSize = 10.sp)
            Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun getWeatherIcon(code: Int): ImageVector {
    return when (code) {
        0 -> Icons.Default.WbSunny
        1, 2, 3 -> Icons.Default.Cloud
        61, 63, 65, 80, 81, 82 -> Icons.Default.WaterDrop
        95, 96, 99 -> Icons.Default.Thunderstorm
        else -> Icons.Default.Cloud
    }
}
