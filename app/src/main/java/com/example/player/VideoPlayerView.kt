package com.example.player

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.VideoSource
import com.example.ui.components.GradientButton
import com.example.ui.theme.BrandRed
import com.example.ui.theme.GlassFill
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

/** The ways we try to open a link, in order, until one works. */
private enum class Engine { EXO_AUTO, EXO_HLS, WEB }

private fun enginesFor(url: String): List<Engine> {
    val clean = url.substringBefore('#').lowercase()
    val path = clean.substringBefore('?')
    val directExt = listOf(".m3u8", ".mpd", ".mp4", ".mkv", ".webm", ".ts", ".m4v", ".mov", ".mp3", ".aac")
    return when {
        directExt.any { path.endsWith(it) } || ".m3u8" in clean -> listOf(Engine.EXO_AUTO, Engine.WEB)
        path.endsWith(".html") || path.endsWith(".htm") || "/embed" in path || "youtube.com" in path ->
            listOf(Engine.WEB)
        // Unknown link type: normal sniffing, then forced HLS, then the page itself.
        else -> listOf(Engine.EXO_AUTO, Engine.EXO_HLS, Engine.WEB)
    }
}

private fun speedLabel(speed: Float): String =
    if (speed % 1f == 0f) "${speed.toInt()}x" else "${speed}x"

@Composable
fun VideoPlayerScreen(
    title: String,
    streamUrl: String,
    subtitle: String = "",
    sources: List<VideoSource> = emptyList(),
    onClose: () -> Unit
) {
    var currentUrl by remember(streamUrl) { mutableStateOf(streamUrl) }
    var attemptIndex by remember(currentUrl) { mutableIntStateOf(0) }
    var failure by remember(currentUrl) { mutableStateOf<String?>(null) }
    var retryNonce by remember { mutableIntStateOf(0) }
    var landscape by remember { mutableStateOf(true) }

    val engines = remember(currentUrl) { enginesFor(currentUrl) }
    val engine = engines.getOrNull(attemptIndex)

    PlayerWindowEffect(landscape)

    fun fail(message: String) {
        if (attemptIndex < engines.lastIndex) attemptIndex += 1 else failure = message
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // Swallow taps so nothing underneath the player can be pressed by accident.
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        if (failure == null && engine != null) {
            key(currentUrl, engine, retryNonce) {
                if (engine == Engine.WEB) {
                    WebStage(
                        url = currentUrl,
                        title = title,
                        subtitle = subtitle,
                        sources = sources,
                        landscape = landscape,
                        onToggleOrientation = { landscape = !landscape },
                        onSelectSource = { currentUrl = it.url },
                        onClose = onClose,
                        onError = { fail("The page could not be opened.") }
                    )
                } else {
                    ExoStage(
                        url = currentUrl,
                        forceHls = engine == Engine.EXO_HLS,
                        title = title,
                        subtitle = subtitle,
                        sources = sources,
                        landscape = landscape,
                        onToggleOrientation = { landscape = !landscape },
                        onSelectSource = { currentUrl = it.url },
                        onClose = onClose,
                        onFatalError = { message -> fail(message) }
                    )
                }
            }
        } else {
            FailureStage(
                title = title,
                message = failure ?: "This stream is not available.",
                sources = sources,
                currentUrl = currentUrl,
                onSelectSource = { currentUrl = it.url },
                onRetry = {
                    failure = null
                    attemptIndex = 0
                    retryNonce += 1
                },
                onClose = onClose
            )
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
@OptIn(UnstableApi::class)
@Composable
private fun ExoStage(
    url: String,
    forceHls: Boolean,
    title: String,
    subtitle: String,
    sources: List<VideoSource>,
    landscape: Boolean,
    onToggleOrientation: () -> Unit,
    onSelectSource: (VideoSource) -> Unit,
    onClose: () -> Unit,
    onFatalError: (String) -> Unit
) {
    val context = LocalContext.current
    var isBuffering by remember { mutableStateOf(true) }
    var isLive by remember { mutableStateOf(false) }
    var controllerVisible by remember { mutableStateOf(true) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var speed by remember { mutableFloatStateOf(1f) }
    var speedMenu by remember { mutableStateOf(false) }
    var seekFlash by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val player = remember {
        val http = DefaultHttpDataSource.Factory()
            .setUserAgent(PLAYER_USER_AGENT)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(20_000)
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(http))
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .build()
            .apply {
                val builder = MediaItem.Builder().setUri(url)
                if (forceHls) builder.setMimeType(MimeTypes.APPLICATION_M3U8)
                setMediaItem(builder.build())
                playWhenReady = true
                prepare()
            }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) isLive = player.isCurrentMediaItemLive
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) isLive = player.isCurrentMediaItemLive
            }

            override fun onPlayerError(error: PlaybackException) {
                if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                    player.seekToDefaultPosition()
                    player.prepare()
                } else {
                    isBuffering = false
                    onFatalError(error.errorCodeName)
                }
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    LaunchedEffect(player, speed) { player.setPlaybackSpeed(speed) }
    LaunchedEffect(seekFlash) {
        if (seekFlash != null) {
            delay(650)
            seekFlash = null
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { player.pause() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        if (player.isCurrentMediaItemLive) player.seekToDefaultPosition()
        player.play()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val view = PlayerView(ctx)
                view.player = player
                view.useController = true
                view.controllerShowTimeoutMs = 3500
                view.controllerAutoShow = true
                view.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                view.keepScreenOn = true
                view.setBackgroundColor(android.graphics.Color.BLACK)
                view.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                view.setControllerVisibilityListener(
                    PlayerView.ControllerVisibilityListener { visibility ->
                        controllerVisible = visibility == View.VISIBLE
                    }
                )
                val detector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDown(e: MotionEvent): Boolean = true

                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        if (view.isControllerFullyVisible) view.hideController() else view.showController()
                        return true
                    }

                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        if (!player.isCurrentMediaItemSeekable) return true
                        val tick = (System.nanoTime() and 0xFFFFFF).toInt()
                        if (e.x < view.width / 2f) {
                            player.seekBack()
                            seekFlash = -1 to tick
                        } else {
                            player.seekForward()
                            seekFlash = 1 to tick
                        }
                        return true
                    }
                })
                view.setOnTouchListener { _, event -> detector.onTouchEvent(event) }
                view
            },
            update = { view -> view.resizeMode = resizeMode }
        )

        // Double tap feedback
        seekFlash?.let { (direction, _) ->
            Box(
                modifier = Modifier
                    .align(if (direction < 0) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 56.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000))
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Text(
                    text = if (direction < 0) "-10s" else "+10s",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isBuffering) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandRed, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
            }
        }

        PlayerTopBar(
            title = title,
            subtitle = subtitle,
            isLive = isLive,
            visible = controllerVisible,
            onClose = onClose,
            modifier = Modifier.align(Alignment.TopCenter),
            sources = sources,
            currentUrl = url,
            onSelectSource = onSelectSource,
            actions = {
                if (!isLive) {
                    Box {
                        PlayerTextPill(text = speedLabel(speed), onClick = { speedMenu = true })
                        DropdownMenu(expanded = speedMenu, onDismissRequest = { speedMenu = false }) {
                            listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (option == 1f) "Normal" else speedLabel(option),
                                            fontWeight = if (option == speed) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        speed = option
                                        speedMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
                PlayerIconButton(
                    icon = Icons.Default.AspectRatio,
                    description = "Fit or fill screen",
                    onClick = {
                        resizeMode = if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        } else {
                            AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    }
                )
                PlayerIconButton(
                    icon = Icons.Default.ScreenRotation,
                    description = if (landscape) "Portrait" else "Landscape",
                    onClick = onToggleOrientation
                )
            }
        )
    }
}

@Composable
private fun WebStage(
    url: String,
    title: String,
    subtitle: String,
    sources: List<VideoSource>,
    landscape: Boolean,
    onToggleOrientation: () -> Unit,
    onSelectSource: (VideoSource) -> Unit,
    onClose: () -> Unit,
    onError: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        PlayerTopBar(
            title = title,
            subtitle = subtitle,
            isLive = false,
            visible = true,
            onClose = onClose,
            sources = sources,
            currentUrl = url,
            onSelectSource = onSelectSource,
            actions = {
                PlayerIconButton(
                    icon = Icons.Default.ScreenRotation,
                    description = if (landscape) "Portrait" else "Landscape",
                    onClick = onToggleOrientation
                )
            }
        )
        WebStreamPlayer(
            url = url,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onMainFrameError = onError
        )
    }
}

@Composable
private fun FailureStage(
    title: String,
    message: String,
    sources: List<VideoSource>,
    currentUrl: String,
    onSelectSource: (VideoSource) -> Unit,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        PlayerTopBar(
            title = title,
            subtitle = "",
            isLive = false,
            visible = true,
            onClose = onClose,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(GlassFill),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "This stream is not playing",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "It may be offline, protected, or blocked on your network. $message",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            if (sources.size > 1) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(text = "Try another server", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                ServerChips(sources = sources, currentUrl = currentUrl, onSelect = onSelectSource)
            }
            Spacer(modifier = Modifier.height(20.dp))
            GradientButton(
                text = "Try again",
                leading = {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = onRetry
            )
        }
    }
}
