package com.example.player

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext

/** Full screen YouTube playback. Replaces the old dialog so rotation and fullscreen behave. */
@Composable
fun YouTubePlayerScreen(
    videoId: String,
    title: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var landscape by remember { mutableStateOf(true) }
    val html = remember(videoId) { youTubeEmbedHtml(videoId) }

    PlayerWindowEffect(landscape)

    fun openInYouTube() {
        val app = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(app)
        } catch (e: ActivityNotFoundException) {
            val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(web)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        PlayerTopBar(
            title = title,
            subtitle = "YouTube",
            isLive = false,
            visible = true,
            onClose = onClose,
            actions = {
                PlayerIconButton(
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                    description = "Open in YouTube",
                    onClick = { openInYouTube() }
                )
                PlayerIconButton(
                    icon = Icons.Default.ScreenRotation,
                    description = if (landscape) "Portrait" else "Landscape",
                    onClick = { landscape = !landscape }
                )
            }
        )
        WebStreamPlayer(
            url = "https://www.youtube.com/embed/$videoId",
            embedHtml = html,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}
