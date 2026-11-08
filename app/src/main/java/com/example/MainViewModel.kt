package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.EntertainmentApi
import com.example.data.model.BongoCategory
import com.example.data.model.CtghallMovie
import com.example.data.model.MusicPlaylist
import com.example.data.model.MusicTrack
import com.example.data.model.OttMovieDetail
import com.example.data.model.OttMovieItem
import com.example.data.model.SportMatch
import com.example.data.model.StreamFeedItem
import com.example.data.model.TvChannel
import com.example.data.model.WeatherForecast
import com.example.data.model.YouTubeVideo
import com.example.player.MusicPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ScreenTab {
    HOME,
    LIVE_TV,
    SPORTS,
    MOVIES,
    MUSIC,
    YOUTUBE,
    WEATHER
}

/** One playable option (for example a sports server) that the player can switch between. */
data class VideoSource(
    val label: String,
    val url: String
)

data class ActiveVideoPlayback(
    val title: String,
    val streamUrl: String,
    val subtitle: String = "",
    val sources: List<VideoSource> = emptyList()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val api = EntertainmentApi()
    val musicPlayerManager = MusicPlayerManager(application)

    // Current navigation tab
    private val _currentTab = MutableStateFlow(ScreenTab.HOME)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    // Active full screen video player
    private val _activeVideo = MutableStateFlow<ActiveVideoPlayback?>(null)
    val activeVideo: StateFlow<ActiveVideoPlayback?> = _activeVideo.asStateFlow()

    // Sports State
    private val _matches = MutableStateFlow<List<SportMatch>>(emptyList())
    val matches: StateFlow<List<SportMatch>> = _matches.asStateFlow()

    private val _streamFeeds = MutableStateFlow<List<StreamFeedItem>>(emptyList())
    val streamFeeds: StateFlow<List<StreamFeedItem>> = _streamFeeds.asStateFlow()

    private val _isSportsLoading = MutableStateFlow(false)
    val isSportsLoading: StateFlow<Boolean> = _isSportsLoading.asStateFlow()

    // Movies State
    private val _ottMovies = MutableStateFlow<List<OttMovieItem>>(emptyList())
    val ottMovies: StateFlow<List<OttMovieItem>> = _ottMovies.asStateFlow()

    private val _ctghallMovies = MutableStateFlow<List<CtghallMovie>>(emptyList())
    val ctghallMovies: StateFlow<List<CtghallMovie>> = _ctghallMovies.asStateFlow()

    private val _bongoCategories = MutableStateFlow<List<BongoCategory>>(emptyList())
    val bongoCategories: StateFlow<List<BongoCategory>> = _bongoCategories.asStateFlow()

    private val _selectedOttDetail = MutableStateFlow<OttMovieDetail?>(null)
    val selectedOttDetail: StateFlow<OttMovieDetail?> = _selectedOttDetail.asStateFlow()

    private val _isMoviesLoading = MutableStateFlow(false)
    val isMoviesLoading: StateFlow<Boolean> = _isMoviesLoading.asStateFlow()

    // Live TV State
    private val _tvChannels = MutableStateFlow<List<TvChannel>>(emptyList())
    val tvChannels: StateFlow<List<TvChannel>> = _tvChannels.asStateFlow()

    private val _tvGroups = MutableStateFlow<List<String>>(listOf("All", "Bangla", "Sports", "News", "Movies", "Kids", "Featured"))
    val tvGroups: StateFlow<List<String>> = _tvGroups.asStateFlow()

    private val _selectedTvGroup = MutableStateFlow("All")
    val selectedTvGroup: StateFlow<String> = _selectedTvGroup.asStateFlow()

    private val _isTvLoading = MutableStateFlow(false)
    val isTvLoading: StateFlow<Boolean> = _isTvLoading.asStateFlow()

    // Music State
    private val _musicResults = MutableStateFlow<List<MusicTrack>>(emptyList())
    val musicResults: StateFlow<List<MusicTrack>> = _musicResults.asStateFlow()

    private val _playlists = MutableStateFlow<List<MusicPlaylist>>(emptyList())
    val playlists: StateFlow<List<MusicPlaylist>> = _playlists.asStateFlow()

    private val _isMusicLoading = MutableStateFlow(false)
    val isMusicLoading: StateFlow<Boolean> = _isMusicLoading.asStateFlow()

    // Weather State
    private val _weather = MutableStateFlow<WeatherForecast?>(null)
    val weather: StateFlow<WeatherForecast?> = _weather.asStateFlow()

    private val _isWeatherLoading = MutableStateFlow(false)
    val isWeatherLoading: StateFlow<Boolean> = _isWeatherLoading.asStateFlow()

    // YouTube Curated State
    private val _youtubeVideos = MutableStateFlow<List<YouTubeVideo>>(emptyList())
    val youtubeVideos: StateFlow<List<YouTubeVideo>> = _youtubeVideos.asStateFlow()

    private val _activeYouTubeVideoId = MutableStateFlow<String?>(null)
    val activeYouTubeVideoId: StateFlow<String?> = _activeYouTubeVideoId.asStateFlow()

    init {
        loadAllInitialData()
    }

    fun selectTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun playVideo(
        title: String,
        streamUrl: String,
        subtitle: String = "",
        sources: List<VideoSource> = emptyList()
    ) {
        if (streamUrl.isNotBlank()) {
            // Do not let music play underneath a video.
            if (musicPlayerManager.state.value.isPlaying) musicPlayerManager.togglePlayPause()
            _activeVideo.value = ActiveVideoPlayback(
                title = title,
                streamUrl = streamUrl,
                subtitle = subtitle,
                sources = sources.filter { it.url.isNotBlank() }
            )
        }
    }

    fun closeVideo() {
        _activeVideo.value = null
    }

    fun openYouTubePlayer(videoId: String) {
        _activeYouTubeVideoId.value = videoId
    }

    fun closeYouTubePlayer() {
        _activeYouTubeVideoId.value = null
    }

    fun loadAllInitialData() {
        loadSports()
        loadMovies()
        loadLiveTv()
        loadMusic()
        loadWeather()
        loadYouTubeVideos()
    }

    fun loadSports() {
        viewModelScope.launch {
            _isSportsLoading.value = true
            val m = api.getLiveMatches()
            val feeds = api.getStreamFeeds()
            _matches.value = m
            _streamFeeds.value = feeds
            _isSportsLoading.value = false
        }
    }

    fun loadMovies() {
        viewModelScope.launch {
            _isMoviesLoading.value = true
            val ott = api.getOttMovies(1)
            val ctg = api.getCtghallMovies()
            val bongo = api.getBongoCategories()
            _ottMovies.value = ott
            _ctghallMovies.value = ctg
            _bongoCategories.value = bongo
            _isMoviesLoading.value = false
        }
    }

    fun searchMovies(query: String) {
        if (query.isBlank()) {
            loadMovies()
            return
        }
        viewModelScope.launch {
            _isMoviesLoading.value = true
            val ottSearch = api.searchOttMovies(query)
            val ctgSearch = api.searchCtghall(query)
            _ottMovies.value = ottSearch
            _ctghallMovies.value = ctgSearch
            _isMoviesLoading.value = false
        }
    }

    fun selectOttMovie(slug: String) {
        viewModelScope.launch {
            val detail = api.getOttMovieDetail(slug)
            _selectedOttDetail.value = detail
        }
    }

    fun dismissOttDetail() {
        _selectedOttDetail.value = null
    }

    fun loadLiveTv() {
        viewModelScope.launch {
            _isTvLoading.value = true
            val channels = api.getLiveTvChannels()
            _tvChannels.value = channels
            val groups = mutableListOf("All")
            channels.map { it.groupTitle }.distinct().filter { it.isNotBlank() }.sorted().forEach {
                if (!groups.contains(it)) groups.add(it)
            }
            _tvGroups.value = groups
            _isTvLoading.value = false
        }
    }

    fun setTvGroup(group: String) {
        _selectedTvGroup.value = group
    }

    fun loadMusic() {
        viewModelScope.launch {
            _isMusicLoading.value = true
            val pLists = api.searchPlaylists("Hindi top hits")
            val songs = api.searchSongs("Top Trending")
            _playlists.value = pLists
            _musicResults.value = if (songs.isNotEmpty()) songs else api.searchSongs("Arijit Singh")
            _isMusicLoading.value = false
        }
    }

    fun searchMusic(query: String) {
        if (query.isBlank()) {
            loadMusic()
            return
        }
        viewModelScope.launch {
            _isMusicLoading.value = true
            val songs = api.searchSongs(query)
            _musicResults.value = songs
            _isMusicLoading.value = false
        }
    }

    fun loadPlaylistTracks(playlistId: String) {
        viewModelScope.launch {
            _isMusicLoading.value = true
            val tracks = api.getPlaylistTracks(playlistId)
            if (tracks.isNotEmpty()) {
                _musicResults.value = tracks
            }
            _isMusicLoading.value = false
        }
    }

    fun loadWeather(lat: Double = 23.8103, lon: Double = 90.4125, cityName: String = "Dhaka") {
        viewModelScope.launch {
            _isWeatherLoading.value = true
            val w = api.getWeather(lat, lon, cityName)
            _weather.value = w
            _isWeatherLoading.value = false
        }
    }

    private fun loadYouTubeVideos() {
        // Curated Bangla & International Entertainment, Sports & Highlights
        _youtubeVideos.value = listOf(
            YouTubeVideo(
                id = "5qap5aO4i9A",
                title = "Lofi Bangla Melodies | Relaxing Hits Collection",
                channelTitle = "Bangla Hits",
                thumbnailUrl = "https://img.youtube.com/vi/5qap5aO4i9A/hqdefault.jpg",
                category = "Music"
            ),
            YouTubeVideo(
                id = "kJQP7kiw5Fk",
                title = "Luis Fonsi - Despacito ft. Daddy Yankee",
                channelTitle = "Luis Fonsi",
                thumbnailUrl = "https://img.youtube.com/vi/kJQP7kiw5Fk/hqdefault.jpg",
                category = "Music"
            ),
            YouTubeVideo(
                id = "9bZkp7q19f0",
                title = "PSY - GANGNAM STYLE (강남스타일) M/V",
                channelTitle = "officialpsy",
                thumbnailUrl = "https://img.youtube.com/vi/9bZkp7q19f0/hqdefault.jpg",
                category = "Music"
            ),
            YouTubeVideo(
                id = "fJ9rUzIMcZQ",
                title = "Queen - Bohemian Rhapsody (Official Video Remastered)",
                channelTitle = "Queen Official",
                thumbnailUrl = "https://img.youtube.com/vi/fJ9rUzIMcZQ/hqdefault.jpg",
                category = "Classics"
            ),
            YouTubeVideo(
                id = "OPf0YbXqDm0",
                title = "Mark Ronson - Uptown Funk (Official Video) ft. Bruno Mars",
                channelTitle = "MarkRonsonVEVO",
                thumbnailUrl = "https://img.youtube.com/vi/OPf0YbXqDm0/hqdefault.jpg",
                category = "Trending"
            ),
            YouTubeVideo(
                id = "JGwWNGJdvx8",
                title = "Ed Sheeran - Shape of You (Official Music Video)",
                channelTitle = "Ed Sheeran",
                thumbnailUrl = "https://img.youtube.com/vi/JGwWNGJdvx8/hqdefault.jpg",
                category = "Hits"
            )
        )
    }

    fun searchYouTube(query: String) {
        if (query.isBlank()) return
        // Form a searchable fallback video list with user's query
        val dummyList = listOf(
            YouTubeVideo(
                id = "kJQP7kiw5Fk",
                title = "Result for '$query' • Top Trending",
                channelTitle = "Mukul+ Search",
                thumbnailUrl = "https://img.youtube.com/vi/kJQP7kiw5Fk/hqdefault.jpg",
                category = "Search"
            ),
            YouTubeVideo(
                id = "OPf0YbXqDm0",
                title = "$query - Official HD Broadcast",
                channelTitle = "Entertainment Plus",
                thumbnailUrl = "https://img.youtube.com/vi/OPf0YbXqDm0/hqdefault.jpg",
                category = "Search"
            )
        )
        _youtubeVideos.value = dummyList
    }

    override fun onCleared() {
        super.onCleared()
        musicPlayerManager.release()
    }
}
