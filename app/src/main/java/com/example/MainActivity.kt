package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.player.VideoPlayerScreen
import com.example.player.YouTubePlayerScreen
import com.example.ui.components.MiniMusicPlayerBar
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveTvScreen
import com.example.ui.screens.MoviesScreen
import com.example.ui.screens.MusicScreen
import com.example.ui.screens.SportsScreen
import com.example.ui.screens.WeatherScreen
import com.example.ui.screens.YouTubeScreen
import com.example.ui.theme.BrandRed
import com.example.ui.theme.BrandRedDeep
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MukulPlusTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MukulPlusTheme {
                MukulPlusApp()
            }
        }
    }
}

sealed class NavItem(
    val tab: ScreenTab,
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    object Home : NavItem(ScreenTab.HOME, "Home", Icons.Default.Home, "nav_home")
    object LiveTv : NavItem(ScreenTab.LIVE_TV, "Live TV", Icons.Default.LiveTv, "nav_livetv")
    object Sports : NavItem(ScreenTab.SPORTS, "Sports", Icons.Default.SportsCricket, "nav_sports")
    object Movies : NavItem(ScreenTab.MOVIES, "Movies", Icons.Default.Movie, "nav_movies")
    object Music : NavItem(ScreenTab.MUSIC, "Music", Icons.Default.MusicNote, "nav_music")
    object YouTube : NavItem(ScreenTab.YOUTUBE, "YouTube", Icons.Default.Subscriptions, "nav_youtube")
    object Weather : NavItem(ScreenTab.WEATHER, "Weather", Icons.Default.Cloud, "nav_weather")
}

@Composable
fun MukulPlusApp(viewModel: MainViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val activeVideo by viewModel.activeVideo.collectAsState()
    val activeYouTubeId by viewModel.activeYouTubeVideoId.collectAsState()
    val youtubeVideos by viewModel.youtubeVideos.collectAsState()
    val musicState by viewModel.musicPlayerManager.state.collectAsState()

    // Handle back button
    BackHandler(enabled = activeVideo != null || activeYouTubeId != null || currentTab != ScreenTab.HOME) {
        if (activeVideo != null) {
            viewModel.closeVideo()
        } else if (activeYouTubeId != null) {
            viewModel.closeYouTubePlayer()
        } else if (currentTab != ScreenTab.HOME) {
            viewModel.selectTab(ScreenTab.HOME)
        }
    }

    val navItems = listOf(
        NavItem.Home,
        NavItem.LiveTv,
        NavItem.Sports,
        NavItem.Movies,
        NavItem.Music,
        NavItem.YouTube,
        NavItem.Weather
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Mini Music Player Bar (Floats right above NavigationBar)
                    if (musicState.currentTrack != null) {
                        MiniMusicPlayerBar(
                            state = musicState,
                            manager = viewModel.musicPlayerManager
                        )
                    }

                    // Bottom Navigation Bar
                    MukulBottomBar(
                        items = navItems,
                        current = currentTab,
                        onSelect = { viewModel.selectTab(it) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(DarkBackground)
            ) {
                when (currentTab) {
                    ScreenTab.HOME -> HomeScreen(viewModel = viewModel)
                    ScreenTab.LIVE_TV -> LiveTvScreen(viewModel = viewModel)
                    ScreenTab.SPORTS -> SportsScreen(viewModel = viewModel)
                    ScreenTab.MOVIES -> MoviesScreen(viewModel = viewModel)
                    ScreenTab.MUSIC -> MusicScreen(viewModel = viewModel)
                    ScreenTab.YOUTUBE -> YouTubeScreen(viewModel = viewModel)
                    ScreenTab.WEATHER -> WeatherScreen(viewModel = viewModel)
                }
            }
        }

        // Full Screen Video Player
        activeVideo?.let { video ->
            VideoPlayerScreen(
                title = video.title,
                streamUrl = video.streamUrl,
                subtitle = video.subtitle,
                sources = video.sources,
                onClose = { viewModel.closeVideo() }
            )
        }

        // Full Screen YouTube Player
        activeYouTubeId?.let { videoId ->
            YouTubePlayerScreen(
                videoId = videoId,
                title = youtubeVideos.firstOrNull { it.id == videoId }?.title ?: "YouTube",
                onClose = { viewModel.closeYouTubePlayer() }
            )
        }
    }
}

/** Slim bottom bar: icons only, the selected tab expands into a labelled pill. */
@Composable
private fun MukulBottomBar(
    items: List<NavItem>,
    current: ScreenTab,
    onSelect: (ScreenTab) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(DarkSurface)) {
        HorizontalDivider(color = DarkBorder, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items.forEach { item ->
                val selected = current == item.tab
                val itemWeight by animateFloatAsState(if (selected) 2f else 1f, label = "navWeight")
                val tint by animateColorAsState(if (selected) Color.White else TextSecondary, label = "navTint")
                Box(
                    modifier = Modifier
                        .weight(itemWeight)
                        .padding(vertical = 10.dp, horizontal = 2.dp)
                        .clip(RoundedCornerShape(50))
                        .then(
                            if (selected) {
                                Modifier.background(Brush.horizontalGradient(listOf(BrandRed, BrandRedDeep)))
                            } else {
                                Modifier
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(item.tab) }
                        )
                        .testTag(item.testTag),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                        if (selected) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}
