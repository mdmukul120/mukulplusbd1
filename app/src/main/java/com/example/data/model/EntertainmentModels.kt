package com.example.data.model

data class TvChannel(
    val id: String,
    val name: String,
    val logo: String,
    val groupTitle: String,
    val streamUrl: String
)

data class SportMatch(
    val id: String,
    val name: String,
    val image: String,
    val sport: String,
    val status: String,
    val startTime: String,
    val league: String,
    val servers: List<StreamServer>
)

data class StreamServer(
    val id: Int,
    val name: String,
    val url: String
)

data class StreamFeedItem(
    val id: String,
    val title: String,
    val sport: String,
    val league: String,
    val status: String,
    val isLive: Boolean,
    val homeTeam: String,
    val awayTeam: String,
    val heroImage: String,
    val streamUrl: String
)

data class OttMovieItem(
    val slug: String,
    val kind: String,
    val title: String,
    val year: Int,
    val qualityTag: String,
    val poster: String,
    val language: String
)

data class OttCategory(
    val slug: String,
    val name: String,
    val bnName: String,
    val count: Int
)

data class OttMovieDetail(
    val slug: String,
    val title: String,
    val poster: String,
    val screenshots: List<String>,
    val description: String,
    val genre: String,
    val language: String,
    val quality: String,
    val resolution: String,
    val released: String,
    val watchUrl: String,
    val downloads: List<DownloadLink>
)

data class DownloadLink(
    val label: String,
    val url: String
)

data class BongoCategory(
    val key: String,
    val title: String,
    val items: List<BongoItem>
)

data class BongoItem(
    val id: String,
    val title: String,
    val thumbnail: String,
    val landscape: String,
    val category: String,
    val watchUrl: String,
    val premium: Boolean
)

data class CtghallMovie(
    val id: Int,
    val title: String,
    val originalTitle: String,
    val year: Int,
    val posterPath: String,
    val onlineRating: Double,
    val libraryName: String
) {
    val fullPosterUrl: String
        get() = if (posterPath.isNotBlank()) {
            if (posterPath.startsWith("http")) posterPath
            else "https://image.tmdb.org/t/p/w500$posterPath"
        } else ""
}

data class CtghallDetail(
    val id: Int,
    val title: String,
    val year: Int,
    val posterPath: String,
    val filePath: String,
    val overview: String,
    val streamUrl: String
)

data class MusicTrack(
    val id: String,
    val title: String,
    val album: String,
    val artists: String,
    val durationSec: Int,
    val imageUrl: String,
    val streamUrl: String,
    val downloadUrl: String
)

data class MusicPlaylist(
    val id: String,
    val name: String,
    val imageUrl: String,
    val type: String
)

data class WeatherForecast(
    val city: String,
    val temp: Double,
    val condition: String,
    val conditionCode: Int,
    val humidity: Int,
    val windSpeed: Double,
    val maxTemp: Double,
    val minTemp: Double,
    val dailyList: List<DailyWeatherItem>
)

data class DailyWeatherItem(
    val dayName: String,
    val condition: String,
    val conditionCode: Int,
    val maxTemp: Double,
    val minTemp: Double
)

data class YouTubeVideo(
    val id: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val category: String
)
