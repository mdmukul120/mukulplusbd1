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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.MainViewModel
import com.example.ScreenTab
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LiveRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val matches by viewModel.matches.collectAsState()
    val ottMovies by viewModel.ottMovies.collectAsState()
    val bongoCategories by viewModel.bongoCategories.collectAsState()
    val tvChannels by viewModel.tvChannels.collectAsState()
    val musicResults by viewModel.musicResults.collectAsState()
    val weather by viewModel.weather.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top App Bar / Brand Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(BrandRed, Color(0xFF99000B))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M+",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Mukul+",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "BD",
                                color = BrandRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                        Text(
                            text = "Streaming Hub",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Weather pill
                weather?.let { w ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.selectTab(ScreenTab.WEATHER) }
                            .testTag("home_weather_badge"),
                        color = DarkSurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = "Weather",
                                tint = BrandCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${w.temp.toInt()}°C ${w.city}",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Live Match Hero Banner
        if (matches.isNotEmpty()) {
            val featured = matches.first()
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    color = DarkSurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandRed.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (featured.image.isNotBlank()) {
                            AsyncImage(
                                model = featured.image,
                                contentDescription = featured.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                        )
                                    )
                            )
                        }

                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0xCC000000), Color(0xF5000000))
                                    )
                                )
                        )

                        // Live Tag
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(LiveRed)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "● LIVE MATCH",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = featured.sport,
                                color = BrandGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Content at bottom
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = featured.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = featured.league,
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row {
                                if (featured.servers.isNotEmpty()) {
                                    val firstServer = featured.servers.first()
                                    ElevatedButton(
                                        onClick = {
                                            viewModel.playVideo(
                                                title = featured.name,
                                                streamUrl = firstServer.url,
                                                subtitle = "${featured.league} • ${firstServer.name}",
                                                sources = featured.servers.map {
                                                    com.example.VideoSource(it.name, it.url)
                                                }
                                            )
                                        },
                                        colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = BrandRed,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Watch Stream", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                ElevatedButton(
                                    onClick = { viewModel.selectTab(ScreenTab.SPORTS) },
                                    colors = ButtonDefaults.elevatedButtonColors(
                                        containerColor = DarkSurfaceVariant,
                                        contentColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = "All Matches", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Category Nav Icons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickNavIcon(
                    title = "Live TV",
                    icon = Icons.Default.LiveTv,
                    color = BrandRed,
                    onClick = { viewModel.selectTab(ScreenTab.LIVE_TV) }
                )
                QuickNavIcon(
                    title = "Sports",
                    icon = Icons.Default.SportsCricket,
                    color = BrandGold,
                    onClick = { viewModel.selectTab(ScreenTab.SPORTS) }
                )
                QuickNavIcon(
                    title = "Movies",
                    icon = Icons.Default.Movie,
                    color = BrandCyan,
                    onClick = { viewModel.selectTab(ScreenTab.MOVIES) }
                )
                QuickNavIcon(
                    title = "Music",
                    icon = Icons.Default.MusicNote,
                    color = BrandGreen,
                    onClick = { viewModel.selectTab(ScreenTab.MUSIC) }
                )
                QuickNavIcon(
                    title = "YouTube",
                    icon = Icons.Default.Subscriptions,
                    color = Color(0xFFFF0000),
                    onClick = { viewModel.selectTab(ScreenTab.YOUTUBE) }
                )
                QuickNavIcon(
                    title = "Weather",
                    icon = Icons.Default.Cloud,
                    color = Color(0xFF38BDF8),
                    onClick = { viewModel.selectTab(ScreenTab.WEATHER) }
                )
            }
        }

        // Section: Mukul OTT Movies & Series
        item {
            SectionHeader(
                title = "Bengali Movies & Series",
                subtitle = "Mukul OTT Trending Releases",
                onSeeAll = { viewModel.selectTab(ScreenTab.MOVIES) }
            )
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ottMovies.take(12)) { movie ->
                    Surface(
                        modifier = Modifier
                            .width(135.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.selectOttMovie(movie.slug)
                            },
                        color = DarkSurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column {
                            Box {
                                AsyncImage(
                                    model = movie.poster.ifEmpty { "https://via.placeholder.com/150x220" },
                                    contentDescription = movie.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xCC000000))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .align(Alignment.TopEnd)
                                ) {
                                    Text(
                                        text = movie.qualityTag,
                                        color = BrandGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = movie.title,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${movie.year} • ${movie.language}",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Popular Live TV Channels
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                title = "Live TV Channels",
                subtitle = "900+ Bangladesh & Global IPTV",
                onSeeAll = { viewModel.selectTab(ScreenTab.LIVE_TV) }
            )
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tvChannels.take(15)) { channel ->
                    Surface(
                        modifier = Modifier
                            .width(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.playVideo(
                                    title = channel.name,
                                    streamUrl = channel.streamUrl,
                                    subtitle = "Live • ${channel.groupTitle}"
                                )
                            },
                        color = DarkSurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                if (channel.logo.isNotBlank()) {
                                    AsyncImage(
                                        model = channel.logo,
                                        contentDescription = channel.name,
                                        modifier = Modifier.size(46.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = null,
                                        tint = BrandCyan,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = channel.name,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = channel.groupTitle,
                                color = BrandRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Section: Bongo Drama & Films
        if (bongoCategories.isNotEmpty()) {
            val bongoDrama = bongoCategories.first()
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = bongoDrama.title,
                    subtitle = "Bongo Natok & Telefilms",
                    onSeeAll = { viewModel.selectTab(ScreenTab.MOVIES) }
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bongoDrama.items.take(10)) { item ->
                        Surface(
                            modifier = Modifier
                                .width(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (item.watchUrl.isNotBlank()) {
                                        viewModel.playVideo(
                                            title = item.title,
                                            streamUrl = item.watchUrl,
                                            subtitle = "Bongo • ${item.category}"
                                        )
                                    }
                                },
                            color = DarkSurfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Column {
                                AsyncImage(
                                    model = item.thumbnail.ifEmpty { item.landscape },
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = item.title,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (item.premium) "★ Premium" else "Free Natok",
                                        color = if (item.premium) BrandGold else BrandGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Music Quick Listen
        if (musicResults.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "Trending Music",
                    subtitle = "High Quality 320kbps Streaming",
                    onSeeAll = { viewModel.selectTab(ScreenTab.MUSIC) }
                )
            }

            items(musicResults.take(4)) { track ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            viewModel.musicPlayerManager.playTrack(track, musicResults)
                        },
                    color = DarkSurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = track.imageUrl.ifEmpty { "https://via.placeholder.com/100" },
                            contentDescription = track.title,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.artists,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.musicPlayerManager.playTrack(track, musicResults)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = BrandGold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        Text(
            text = "View All →",
            color = BrandRed,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable { onSeeAll() }
                .padding(4.dp)
        )
    }
}

@Composable
fun QuickNavIcon(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
