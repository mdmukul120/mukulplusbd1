package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.MainViewModel
import com.example.data.model.OttMovieDetail
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
import kotlinx.coroutines.launch

@Composable
fun MoviesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val ottMovies by viewModel.ottMovies.collectAsState()
    val ctghallMovies by viewModel.ctghallMovies.collectAsState()
    val bongoCategories by viewModel.bongoCategories.collectAsState()
    val selectedDetail by viewModel.selectedOttDetail.collectAsState()
    val isLoading by viewModel.isMoviesLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Mukul OTT, 1: Ctghall, 2: Bongo
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchMovies(it)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("movie_search_input"),
                placeholder = { Text("Search movies, natok, cinema...", color = TextSecondary, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.searchMovies("")
                        }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceCard,
                    unfocusedContainerColor = DarkSurfaceCard,
                    focusedBorderColor = BrandRed,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { viewModel.loadMovies() }) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = BrandRed)
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = BrandRed,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = BrandRed,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "Mukul OTT",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) TextPrimary else TextSecondary,
                        fontSize = 12.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Ctghall Movies",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) TextPrimary else TextSecondary,
                        fontSize = 12.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        text = "Bongo Natok",
                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 2) TextPrimary else TextSecondary,
                        fontSize = 12.sp
                    )
                }
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandRed)
            }
        } else {
            when (selectedTab) {
                0 -> {
                    // Mukul OTT Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 120.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(ottMovies) { movie ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.selectOttMovie(movie.slug)
                                    }
                                    .testTag("ott_movie_${movie.slug}"),
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
                                                .height(170.dp),
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
                                                fontSize = 9.sp,
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
                1 -> {
                    // Ctghall Movies Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 120.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(ctghallMovies) { movie ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        scope.launch {
                                            val detail = viewModel.api.getCtghallDetail(movie.id)
                                            if (detail != null && detail.streamUrl.isNotBlank()) {
                                                viewModel.playVideo(
                                                    title = detail.title,
                                                    streamUrl = detail.streamUrl,
                                                    subtitle = "Ctghall Movie • ${movie.year}"
                                                )
                                            }
                                        }
                                    },
                                color = DarkSurfaceCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                            ) {
                                Column {
                                    Box {
                                        AsyncImage(
                                            model = movie.fullPosterUrl.ifEmpty { "https://via.placeholder.com/150x220" },
                                            contentDescription = movie.title,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(170.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                        if (movie.onlineRating > 0) {
                                            Row(
                                                modifier = Modifier
                                                    .padding(6.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xCC000000))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    .align(Alignment.TopEnd),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = BrandGold,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "%.1f".format(movie.onlineRating),
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
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
                                            text = "${movie.year} • ${movie.libraryName}",
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Bongo Dramas
                    val allBongoItems = bongoCategories.flatMap { it.items }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 120.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(allBongoItems) { item ->
                            Surface(
                                modifier = Modifier
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
                                            text = if (item.premium) "★ Premium" else "Free",
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
        }
    }

    // OTT Movie Detail Dialog
    selectedDetail?.let { detail ->
        MovieDetailDialog(
            detail = detail,
            onDismiss = { viewModel.dismissOttDetail() },
            onWatch = {
                viewModel.playVideo(
                    title = detail.title,
                    streamUrl = detail.watchUrl,
                    subtitle = "${detail.resolution} • ${detail.quality}"
                )
            },
            onDownload = { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
        )
    }
}

@Composable
fun MovieDetailDialog(
    detail: OttMovieDetail,
    onDismiss: () -> Unit,
    onWatch: () -> Unit,
    onDownload: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = DarkSurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Movie Details",
                        color = BrandGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = detail.poster.ifEmpty { "https://via.placeholder.com/150x220" },
                        contentDescription = detail.title,
                        modifier = Modifier
                            .width(110.dp)
                            .height(150.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = detail.title,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Language: ${detail.language}",
                            color = BrandCyan,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Quality: ${detail.quality} (${detail.resolution})",
                            color = BrandGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (detail.released.isNotBlank()) {
                            Text(
                                text = "Release: ${detail.released}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Watch Button
                ElevatedButton(
                    onClick = onWatch,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = BrandRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Watch Movie Now", fontWeight = FontWeight.Bold)
                }

                // Download Links
                if (detail.downloads.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "DOWNLOAD OPTIONS:",
                        color = BrandGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    detail.downloads.forEach { link ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onDownload(link.url) },
                            color = DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = link.label,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download",
                                    tint = BrandGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Screenshots
                if (detail.screenshots.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "PREVIEW SCREENSHOTS",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(detail.screenshots) { shot ->
                            AsyncImage(
                                model = shot,
                                contentDescription = null,
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}
