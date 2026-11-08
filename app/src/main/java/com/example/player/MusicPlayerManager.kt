package com.example.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.model.MusicTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MusicPlayerState(
    val currentTrack: MusicTrack? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<MusicTrack> = emptyList(),
    val currentIndex: Int = -1
)

class MusicPlayerManager(context: Context) {

    private val player = ExoPlayer.Builder(context.applicationContext).build()
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _state = MutableStateFlow(MusicPlayerState())
    val state: StateFlow<MusicPlayerState> = _state.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(isPlaying = isPlaying)
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    progressJob?.cancel()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val buffering = playbackState == Player.STATE_BUFFERING
                val duration = if (player.duration > 0) player.duration else 0L
                _state.value = _state.value.copy(
                    isBuffering = buffering,
                    durationMs = duration
                )

                if (playbackState == Player.STATE_ENDED) {
                    next()
                }
            }
        })
    }

    fun playTrack(track: MusicTrack, playlistQueue: List<MusicTrack> = emptyList()) {
        val queue = if (playlistQueue.isNotEmpty()) playlistQueue else listOf(track)
        val index = queue.indexOfFirst { it.id == track.id }.let { if (it >= 0) it else 0 }

        _state.value = _state.value.copy(
            currentTrack = track,
            queue = queue,
            currentIndex = index,
            isBuffering = true
        )

        try {
            val mediaItem = MediaItem.fromUri(track.streamUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _state.value = _state.value.copy(currentPositionMs = positionMs)
    }

    fun next() {
        val s = _state.value
        if (s.queue.isNotEmpty() && s.currentIndex + 1 < s.queue.size) {
            val nextTrack = s.queue[s.currentIndex + 1]
            playTrack(nextTrack, s.queue)
        }
    }

    fun previous() {
        val s = _state.value
        if (s.queue.isNotEmpty() && s.currentIndex - 1 >= 0) {
            val prevTrack = s.queue[s.currentIndex - 1]
            playTrack(prevTrack, s.queue)
        }
    }

    fun stop() {
        player.stop()
        progressJob?.cancel()
        _state.value = _state.value.copy(isPlaying = false, currentTrack = null)
    }

    fun release() {
        progressJob?.cancel()
        player.release()
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    val pos = player.currentPosition
                    val dur = if (player.duration > 0) player.duration else _state.value.durationMs
                    _state.value = _state.value.copy(
                        currentPositionMs = pos,
                        durationMs = dur
                    )
                }
                delay(500)
            }
        }
    }
}
