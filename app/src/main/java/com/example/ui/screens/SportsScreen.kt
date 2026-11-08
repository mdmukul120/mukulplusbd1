package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.MainViewModel
import com.example.VideoSource
import com.example.data.model.SportMatch
import com.example.data.model.StreamFeedItem
import com.example.ui.components.FilterPill
import com.example.ui.components.GlassPill
import com.example.ui.components.GradientButton
import com.example.ui.components.LiveBadge
import com.example.ui.components.PillTabs
import com.example.ui.components.SectionTitle
import com.example.ui.components.ShimmerBox
import com.example.ui.theme.BrandGold
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private fun SportMatch.isLive(): Boolean = status.equals("live", ignoreCase = true)

private fun sportIcon(sport: String): ImageVector = when {
    sport.contains("cricket", true) -> Icons.Default.SportsCricket
    sport.contains("basket", true) -> Icons.Default.SportsBasketball
    sport.contains("tennis", true) -> Icons.Default.SportsTennis
    else -> Icons.Default.SportsSoccer
}

private val versusRegex = Regex("\\s+(vs\\.?|v)\\s+", RegexOption.IGNORE_CASE)

private fun splitTeams(name: String): Pair<String, String>? {
    val parts = name.split(versusRegex, limit = 2)
    return if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
        parts[0].trim() to parts[1].trim()
    } else {
        null
    }
}

/** Turns an ISO time or an epoch number into a short local time. Falls back to the raw text. */
private fun formatStartTime(raw: String): String {
    if (raw.isBlank()) return ""
    return try {
        val epoch = raw.toLongOrNull()
        val date: Date? = if (epoch != null) {
            Date(if (epoch < 100_000_000_000L) epoch * 1000 else epoch)
        } else {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            parser.timeZone = TimeZone.getTimeZone("UTC")
            parser.parse(raw.take(19))
        }
        if (date == null) raw else SimpleDateFormat("d MMM, h:mm a", Locale.getDefault()).format(date)
    } catch (e: Exception) {
        raw
    }
}

@Composable
fun SportsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val matches by viewModel.matches.collectAsState()
    val streamFeeds by viewModel.streamFeeds.collectAsState()
    val isLoading by viewModel.isSportsLoading.collectAsState()

    var tab by remember { mutableIntStateOf(0) }
    var sportFilter by remember { mutableStateOf("All") }

    val sports = remember(matches) {
        listOf("All") + matches.map { it.sport.trim() }.filter { it.isNotBlank() }.distinct()
    }
    LaunchedEffect(sports) {
        if (sportFilter !in sports) sportFilter = "All"
    }

    val filtered = remember(matches, sportFilter) {
        if (sportFilter == "All") matches else matches.filter { it.sport.trim().equals(sportFilter, true) }
    }
    val liveNow = remember(filtered) { filtered.filter { it.isLive() } }
    val upcoming = remember(filtered) { filtered.filter { !it.isLive() } }
    val hero = liveNow.firstOrNull() ?: upcoming.firstOrNull()
    val moreLive = if (hero != null && hero in liveNow) liveNow.drop(1) else liveNow
    val moreUpcoming = if (hero != null && hero in upcoming) upcoming.drop(1) else upcoming

    fun play(match: SportMatch, serverIndex: Int) {
        val server = match.servers.getOrNull(serverIndex) ?: return
        viewModel.playVideo(
            title = match.name,
            streamUrl = server.url,
            subtitle = "${match.league} - ${server.name}",
            sources = match.servers.map { VideoSource(it.name, it.url) }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Sports", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (liveNow.isNotEmpty()) {
                        LiveBadge(label = "${liveNow.size} LIVE")
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "Cricket, football and more",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
            IconButton(onClick = { viewModel.loadSports() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = TextSecondary
                )
            }
        }

        PillTabs(
            options = listOf("Matches", "Live streams"),
            selectedIndex = tab,
            onSelect = { tab = it },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        val nothingYet = matches.isEmpty() && streamFeeds.isEmpty()
        if (isLoading && nothingYet) {
            SportsSkeleton()
        } else if (tab == 0) {
            if (matches.isEmpty()) {
                SportsEmpty(
                    title = "No matches right now",
                    message = "Matches appear here when they are scheduled or live.",
                    onRefresh = { viewModel.loadSports() }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (sports.size > 2) {
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(sports) { sport ->
                                    FilterPill(
                                        text = sport,
                                        selected = sport == sportFilter,
                                        onClick = { sportFilter = sport }
                                    )
                                }
                            }
                        }
                    }

                    if (hero != null) {
                        item {
                            FeaturedMatchCard(match = hero, onPlay = { index -> play(hero, index) })
                        }
                    }

                    if (moreLive.isNotEmpty()) {
                        item { SectionTitle(title = "Live now", count = moreLive.size) }
                        items(moreLive) { match ->
                            MatchCard(match = match, onPlay = { index -> play(match, index) })
                        }
                    }

                    if (moreUpcoming.isNotEmpty()) {
                        item { SectionTitle(title = "Upcoming", count = moreUpcoming.size) }
                        items(moreUpcoming) { match ->
                            MatchCard(match = match, onPlay = { index -> play(match, index) })
                        }
                    }
                }
            }
        } else {
            if (streamFeeds.isEmpty()) {
                SportsEmpty(
                    title = "No live streams right now",
                    message = "Check back during game hours.",
                    onRefresh = { viewModel.loadSports() }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(streamFeeds) { feed ->
                        StreamFeedCard(
                            feed = feed,
                            onPlay = {
                                if (feed.streamUrl.isNotBlank()) {
                                    viewModel.playVideo(
                                        title = feed.title,
                                        streamUrl = feed.streamUrl,
                                        subtitle = "${feed.league} - ${feed.sport}"
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedMatchCard(match: SportMatch, onPlay: (Int) -> Unit) {
    val shape = RoundedCornerShape(26.dp)
    val teams = remember(match.name) { splitTeams(match.name) }
    val live = match.isLive()
    val hasStream = match.servers.any { it.url.isNotBlank() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(shape)
            .border(1.dp, DarkBorder, shape)
            .background(DarkSurfaceCard)
            .testTag("match_card_${match.id}")
    ) {
        if (match.image.isNotBlank()) {
            AsyncImage(
                model = match.image,
                contentDescription = match.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(Color(0xFF2A1218), Color(0xFF0D111A))))
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0x55000000),
                            0.45f to Color(0x22000000),
                            1.0f to Color(0xF205070C)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (live) {
                LiveBadge()
            } else {
                GlassPill(text = formatStartTime(match.startTime).ifBlank { match.status })
            }
            GlassPill(text = match.sport)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = match.league,
                color = BrandGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            if (teams != null) {
                Text(
                    text = buildAnnotatedString {
                        append(teams.first)
                        withStyle(SpanStyle(color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)) {
                            append("   vs   ")
                        }
                        append(teams.second)
                    },
                    color = Color.White,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = match.name,
                    color = Color.White,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                GradientButton(
                    text = if (live) "Watch live" else "Watch",
                    enabled = hasStream,
                    leading = {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (hasStream) Color.White else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = { onPlay(0) }
                )
                if (match.servers.size > 1) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        match.servers.forEachIndexed { index, server ->
                            GlassPill(text = server.name, onClick = { onPlay(index) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchCard(match: SportMatch, onPlay: (Int) -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    val live = match.isLive()
    val hasStream = match.servers.any { it.url.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DarkSurfaceCard)
            .border(1.dp, DarkBorder, shape)
            .padding(12.dp)
            .testTag("match_card_${match.id}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 96.dp, height = 70.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (match.image.isNotBlank()) {
                    AsyncImage(
                        model = match.image,
                        contentDescription = match.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = sportIcon(match.sport),
                        contentDescription = null,
                        tint = BrandGold,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (live) {
                        LiveBadge()
                    } else {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatStartTime(match.startTime).ifBlank { match.status },
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = match.name,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${match.league} - ${match.sport}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            GradientButton(
                text = "Watch",
                enabled = hasStream,
                leading = {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (hasStream) Color.White else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = { onPlay(0) }
            )
            if (match.servers.size > 1) {
                Spacer(modifier = Modifier.width(10.dp))
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    match.servers.forEachIndexed { index, server ->
                        GlassPill(text = server.name, textColor = TextPrimary, onClick = { onPlay(index) })
                    }
                }
            } else if (!hasStream) {
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "Stream opens before kickoff", color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun StreamFeedCard(feed: StreamFeedItem, onPlay: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DarkSurfaceCard)
            .border(1.dp, DarkBorder, shape)
            .clickable(onClick = onPlay)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 118.dp, height = 72.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (feed.heroImage.isNotBlank()) {
                AsyncImage(
                    model = feed.heroImage,
                    contentDescription = feed.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = sportIcon(feed.sport),
                    contentDescription = null,
                    tint = BrandGold,
                    modifier = Modifier.size(30.dp)
                )
            }
            if (feed.isLive) {
                LiveBadge(modifier = Modifier.align(Alignment.TopStart).padding(6.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = feed.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = listOf(feed.league, feed.sport).filter { it.isNotBlank() }.joinToString(" - "),
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(Color(0xFFE11D2E), Color(0xFF8F0A16)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play stream",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SportsSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(250.dp), shape = RoundedCornerShape(26.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(20.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(20.dp))
    }
}

@Composable
private fun SportsEmpty(title: String, message: String, onRefresh: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceCard)
                    .border(1.dp, DarkBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsSoccer,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = message, color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(18.dp))
            GradientButton(text = "Refresh", onClick = onRefresh)
        }
    }
}
