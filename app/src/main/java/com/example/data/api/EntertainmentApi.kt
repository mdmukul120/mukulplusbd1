package com.example.data.api

import com.example.data.model.BongoCategory
import com.example.data.model.BongoItem
import com.example.data.model.CtghallDetail
import com.example.data.model.CtghallMovie
import com.example.data.model.DailyWeatherItem
import com.example.data.model.DownloadLink
import com.example.data.model.MusicPlaylist
import com.example.data.model.MusicTrack
import com.example.data.model.OttCategory
import com.example.data.model.OttMovieDetail
import com.example.data.model.OttMovieItem
import com.example.data.model.SportMatch
import com.example.data.model.StreamFeedItem
import com.example.data.model.StreamServer
import com.example.data.model.TvChannel
import com.example.data.model.WeatherForecast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class EntertainmentApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private fun newRequest(url: String): Request {
        return Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .build()
    }

    private suspend fun getJson(url: String): String = withContext(Dispatchers.IO) {
        val request = newRequest(url)
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code} for $url")
            response.body?.string() ?: ""
        }
    }

    // 1. Mukul Sports - Matches
    suspend fun getLiveMatches(): List<SportMatch> = withContext(Dispatchers.IO) {
        try {
            val jsonStr = getJson("https://mukulsport1.vercel.app/api/matches")
            val root = JSONObject(jsonStr)
            val matchesArray = root.optJSONArray("matches") ?: JSONArray()
            val list = mutableListOf<SportMatch>()
            for (i in 0 until matchesArray.length()) {
                val obj = matchesArray.getJSONObject(i)
                val serversArr = obj.optJSONArray("servers") ?: JSONArray()
                val servers = mutableListOf<StreamServer>()
                for (j in 0 until serversArr.length()) {
                    val sObj = serversArr.getJSONObject(j)
                    servers.add(
                        StreamServer(
                            id = sObj.optInt("id", j + 1),
                            name = sObj.optString("name", "Server ${j + 1}"),
                            url = sObj.optString("url", "")
                        )
                    )
                }
                list.add(
                    SportMatch(
                        id = obj.optString("id", "match-$i"),
                        name = obj.optString("name", "Live Match"),
                        image = obj.optString("image", ""),
                        sport = obj.optString("sport", "SPORTS"),
                        status = obj.optString("status", "live"),
                        startTime = obj.optString("startTime", ""),
                        league = obj.optString("league", "Tournament"),
                        servers = servers
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 2. Mukul Sports - Streams Feed
    suspend fun getStreamFeeds(): List<StreamFeedItem> = withContext(Dispatchers.IO) {
        try {
            val jsonStr = getJson("https://mukulsport.vercel.app/api/feed/streams.json")
            val root = JSONObject(jsonStr)
            val liveArr = root.optJSONArray("live") ?: JSONArray()
            val list = mutableListOf<StreamFeedItem>()
            for (i in 0 until liveArr.length()) {
                val obj = liveArr.getJSONObject(i)
                val home = obj.optJSONObject("home_team")?.optString("name") ?: ""
                val away = obj.optJSONObject("away_team")?.optString("name") ?: ""
                var streamUrl = obj.optString("stream_url", "")
                if (streamUrl.isBlank()) {
                    val streamArray = obj.optJSONArray("streams")
                    if (streamArray != null && streamArray.length() > 0) {
                        streamUrl = streamArray.getJSONObject(0).optString("url", "")
                    }
                }
                list.add(
                    StreamFeedItem(
                        id = obj.optString("id", "stream-$i"),
                        title = obj.optString("title", "$home vs $away"),
                        sport = obj.optString("sport", "Sports"),
                        league = obj.optString("league", ""),
                        status = obj.optString("status", "LIVE"),
                        isLive = obj.optBoolean("is_live", true),
                        homeTeam = home,
                        awayTeam = away,
                        heroImage = obj.optString("hero_image", ""),
                        streamUrl = streamUrl
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 3. Mukul OTT - Movies & Series
    suspend fun getOttMovies(page: Int = 1): List<OttMovieItem> = withContext(Dispatchers.IO) {
        try {
            val jsonStr = getJson("https://mukul-ott.ai.studio/api/movies?page=$page")
            val root = JSONObject(jsonStr)
            val itemsArr = root.optJSONArray("items") ?: JSONArray()
            val list = mutableListOf<OttMovieItem>()
            for (i in 0 until itemsArr.length()) {
                val obj = itemsArr.getJSONObject(i)
                list.add(
                    OttMovieItem(
                        slug = obj.optString("slug", ""),
                        kind = obj.optString("kind", "movie"),
                        title = obj.optString("title", ""),
                        year = obj.optInt("year", 2026),
                        qualityTag = obj.optString("quality_tag", "HD"),
                        poster = obj.optString("poster", ""),
                        language = obj.optString("language", "Bangla")
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Mukul OTT Categories
    suspend fun getOttCategories(): List<OttCategory> = withContext(Dispatchers.IO) {
        try {
            val jsonStr = getJson("https://mukul-ott.ai.studio/api/categories")
            val root = JSONObject(jsonStr)
            val arr = root.optJSONArray("categories") ?: JSONArray()
            val list = mutableListOf<OttCategory>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    OttCategory(
                        slug = obj.optString("slug", ""),
                        name = obj.optString("name", ""),
                        bnName = obj.optString("bnName", ""),
                        count = obj.optInt("count", 0)
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Search OTT Movies
    suspend fun searchOttMovies(query: String): List<OttMovieItem> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            val jsonStr = getJson("https://mukul-ott.ai.studio/api/search?q=$encoded")
            val root = JSONObject(jsonStr)
            val arr = root.optJSONArray("items") ?: JSONArray()
            val list = mutableListOf<OttMovieItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    OttMovieItem(
                        slug = obj.optString("slug", ""),
                        kind = obj.optString("kind", "movie"),
                        title = obj.optString("title", ""),
                        year = obj.optInt("year", 2026),
                        qualityTag = obj.optString("quality_tag", "HD"),
                        poster = obj.optString("poster", ""),
                        language = obj.optString("language", "Bangla")
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // OTT Movie Detail
    suspend fun getOttMovieDetail(slug: String): OttMovieDetail? = withContext(Dispatchers.IO) {
        try {
            val jsonStr = getJson("https://mukul-ott.ai.studio/api/movie/$slug")
            val obj = JSONObject(jsonStr)
            val screenshotsArr = obj.optJSONArray("screenshots") ?: JSONArray()
            val screenshots = mutableListOf<String>()
            for (i in 0 until screenshotsArr.length()) {
                screenshots.add(screenshotsArr.getString(i))
            }
            val downloadsArr = obj.optJSONArray("downloads") ?: JSONArray()
            val downloads = mutableListOf<DownloadLink>()
            for (i in 0 until downloadsArr.length()) {
                val dObj = downloadsArr.getJSONObject(i)
                downloads.add(
                    DownloadLink(
                        label = dObj.optString("label", "Download Option ${i + 1}"),
                        url = dObj.optString("url", "")
                    )
                )
            }
            OttMovieDetail(
                slug = obj.optString("slug", slug),
                title = obj.optString("title", "Movie"),
                poster = obj.optString("poster", ""),
                screenshots = screenshots,
                description = obj.optString("description", ""),
                genre = obj.optString("genre", ""),
                language = obj.optString("language", "Bangla"),
                quality = obj.optString("quality", "WEB-DL"),
                resolution = obj.optString("resolution", "1080p"),
                released = obj.optString("released", ""),
                watchUrl = obj.optString("watch_url", ""),
                downloads = downloads
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 4. Bongo Scraping (Drama & Movies)
    suspend fun getBongoCategories(): List<BongoCategory> = withContext(Dispatchers.IO) {
        try {
            val jsonStr = getJson("https://hamyra-api.mdibrahimkhalil516.workers.dev/bongo/scrape")
            val root = JSONObject(jsonStr)
            val catArr = root.optJSONArray("categories") ?: JSONArray()
            val list = mutableListOf<BongoCategory>()
            for (i in 0 until catArr.length()) {
                val catObj = catArr.getJSONObject(i)
                val itemsArr = catObj.optJSONArray("items") ?: JSONArray()
                val items = mutableListOf<BongoItem>()
                for (j in 0 until itemsArr.length()) {
                    val itemObj = itemsArr.getJSONObject(j)
                    items.add(
                        BongoItem(
                            id = itemObj.optString("id", ""),
                            title = itemObj.optString("title", ""),
                            thumbnail = itemObj.optString("thumbnail", ""),
                            landscape = itemObj.optString("landscape", ""),
                            category = itemObj.optString("category", ""),
                            watchUrl = itemObj.optString("watchUrl", ""),
                            premium = itemObj.optBoolean("premium", false)
                        )
                    )
                }
                list.add(
                    BongoCategory(
                        key = catObj.optString("key", "cat_$i"),
                        title = catObj.optString("title", "Bongo"),
                        items = items
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 5. Ctghall Movies
    suspend fun getCtghallMovies(): List<CtghallMovie> = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.ctghall.com/api/movies?library=1&fields=id,title,original_title,year,poster_path,release_date,rating,online_rating&sort=createdAt&sort_order=DESC"
            val jsonStr = getJson(url)
            val root = JSONObject(jsonStr)
            val dataArr = root.optJSONArray("data") ?: JSONArray()
            val list = mutableListOf<CtghallMovie>()
            for (i in 0 until dataArr.length()) {
                val obj = dataArr.getJSONObject(i)
                val libName = obj.optJSONObject("Library")?.optString("name") ?: "Movies"
                list.add(
                    CtghallMovie(
                        id = obj.optInt("id", 0),
                        title = obj.optString("title", ""),
                        originalTitle = obj.optString("original_title", ""),
                        year = obj.optInt("year", 2026),
                        posterPath = obj.optString("poster_path", ""),
                        onlineRating = obj.optDouble("online_rating", 7.0),
                        libraryName = libName
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Ctghall Search
    suspend fun searchCtghall(query: String): List<CtghallMovie> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            val jsonStr = getJson("https://www.ctghall.com/api/search?search=$encoded")
            val root = JSONObject(jsonStr)
            val dataArr = root.optJSONArray("data") ?: JSONArray()
            val list = mutableListOf<CtghallMovie>()
            for (i in 0 until dataArr.length()) {
                val obj = dataArr.getJSONObject(i)
                list.add(
                    CtghallMovie(
                        id = obj.optInt("id", 0),
                        title = obj.optString("title", ""),
                        originalTitle = obj.optString("original_title", ""),
                        year = obj.optInt("year", 0),
                        posterPath = obj.optString("poster_path", ""),
                        onlineRating = obj.optDouble("online_rating", 0.0),
                        libraryName = obj.optJSONObject("Library")?.optString("name") ?: "Movie"
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Ctghall Detail
    suspend fun getCtghallDetail(id: Int): CtghallDetail? = withContext(Dispatchers.IO) {
        try {
            val jsonStr = getJson("https://www.ctghall.com/api/movies/$id")
            val obj = JSONObject(jsonStr)
            val relUrl = obj.optString("url", "")
            val streamUrl = if (relUrl.startsWith("http")) relUrl else "https://www.ctghall.com$relUrl"
            CtghallDetail(
                id = obj.optInt("id", id),
                title = obj.optString("title", ""),
                year = obj.optInt("year", 0),
                posterPath = obj.optString("poster_path", ""),
                filePath = obj.optString("file_path", ""),
                overview = obj.optString("overview", ""),
                streamUrl = streamUrl
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 6. Live TV IPTV M3U Playlist
    suspend fun getLiveTvChannels(): List<TvChannel> = withContext(Dispatchers.IO) {
        try {
            val m3u = getJson("https://mukul-ott.ai.studio/api/iptv/live.m3u")
            val parsed = M3uParser.parse(m3u)
            if (parsed.isNotEmpty()) parsed else fallbackChannels()
        } catch (e: Exception) {
            e.printStackTrace()
            fallbackChannels()
        }
    }

    private fun fallbackChannels(): List<TvChannel> {
        return listOf(
            TvChannel("ekushey", "Ekushey TV", "https://s4.gifyu.com/images/image534fa27d7683f33d.png", "Bangla", "http://210.4.72.204/hls-live/livepkgr/_definst_/liveevent/livestream3.m3u8"),
            TvChannel("deshebideshe", "Deshe Bideshe", "https://raw.githubusercontent.com/time2shine/IPTV-logos/main/logos/deshe_bideshe.png", "Bangla", "https://dbcanada.sonarbanglatv.com/deshebideshe/dbtv/index.m3u8"),
            TvChannel("fifa_plus", "FIFA+", "https://s3.aynaott.com/storage/dc25a8382341855cd9847537971d869c", "Sports", "https://4397879b.wurl.com/master/f36d25e7e52f1ba8d7e56eb859c636563214f541/UmFrdXRlblRWLWRlX0ZJRkFQbHVzR2VybWFuX0hMUw/playlist.m3u8"),
            TvChannel("sports_first", "Sports First TV", "http://dds.oncast.me/img/logo/sp6.png", "Sports", "https://d4ddgdmj1cvnm.cloudfront.net/scheduler/scheduleMaster/409.m3u8"),
            TvChannel("playz_tv", "PlayZ TV", "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEgyniKyW9pUz1OKx5bgzLASwGVSuP0e7hX9FxVMTJMHxhu8X0tpucgSplBZgM8pCYrJJH0P2_dTC1-wzp4mMUU4sKnOzghGPCwWdbYOOa4jTyhpr7ydNj-UK-bc56IMsk2H3WZJ-SzSZIk0dTpyABCFR2_zjC2_c86W1pv7odFBT_Y-hyJs62g-3zCJkPGd/s1024/1000398131.png", "Featured", "https://playztv.pages.dev/promo/master.m3u8")
        )
    }

    // 7. Music Search & Playlists (Music45 JioSaavn API)
    suspend fun searchSongs(query: String): List<MusicTrack> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            val jsonStr = getJson("https://music45-api.vercel.app/api/search/songs?query=$encoded")
            val root = JSONObject(jsonStr)
            val dataObj = root.optJSONObject("data") ?: JSONObject()
            val resultsArr = dataObj.optJSONArray("results") ?: JSONArray()
            val list = mutableListOf<MusicTrack>()
            for (i in 0 until resultsArr.length()) {
                val obj = resultsArr.getJSONObject(i)
                val imagesArr = obj.optJSONArray("image") ?: JSONArray()
                var img = ""
                for (k in imagesArr.length() - 1 downTo 0) {
                    val candidate = imagesArr.getJSONObject(k).optString("url")
                    if (candidate.isNotBlank()) {
                        img = candidate
                        break
                    }
                }
                val downloadUrlsArr = obj.optJSONArray("downloadUrl") ?: JSONArray()
                var streamUrl = ""
                var highestKbpsUrl = ""
                for (k in downloadUrlsArr.length() - 1 downTo 0) {
                    val u = downloadUrlsArr.getJSONObject(k).optString("url")
                    if (u.isNotBlank()) {
                        highestKbpsUrl = u
                        break
                    }
                }
                streamUrl = highestKbpsUrl

                val artistsArr = obj.optJSONObject("artists")?.optJSONArray("primary")
                val artistStr = if (artistsArr != null && artistsArr.length() > 0) {
                    artistsArr.getJSONObject(0).optString("name")
                } else obj.optString("label", "Artist")

                list.add(
                    MusicTrack(
                        id = obj.optString("id", "track-$i"),
                        title = obj.optString("name", "Song Title").replace("&quot;", "\"").replace("&#039;", "'"),
                        album = obj.optJSONObject("album")?.optString("name", "") ?: "",
                        artists = artistStr,
                        durationSec = obj.optInt("duration", 0),
                        imageUrl = img,
                        streamUrl = streamUrl,
                        downloadUrl = streamUrl
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun searchPlaylists(query: String): List<MusicPlaylist> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            val jsonStr = getJson("https://music45-api.vercel.app/api/search/playlists?query=$encoded&limit=20")
            val root = JSONObject(jsonStr)
            val dataObj = root.optJSONObject("data") ?: JSONObject()
            val resultsArr = dataObj.optJSONArray("results") ?: JSONArray()
            val list = mutableListOf<MusicPlaylist>()
            for (i in 0 until resultsArr.length()) {
                val obj = resultsArr.getJSONObject(i)
                val imgArr = obj.optJSONArray("image") ?: JSONArray()
                var img = ""
                if (imgArr.length() > 0) {
                    img = imgArr.getJSONObject(imgArr.length() - 1).optString("url", "")
                }
                list.add(
                    MusicPlaylist(
                        id = obj.optString("id", ""),
                        name = obj.optString("name", "Playlist").replace("&quot;", "\""),
                        imageUrl = img,
                        type = obj.optString("type", "playlist")
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getPlaylistTracks(playlistId: String): List<MusicTrack> = withContext(Dispatchers.IO) {
        try {
            val jsonStr = getJson("https://music45-api.vercel.app/api/playlists?id=$playlistId&page=1&limit=30")
            val root = JSONObject(jsonStr)
            val dataObj = root.optJSONObject("data") ?: JSONObject()
            val songsArr = dataObj.optJSONArray("songs") ?: JSONArray()
            val list = mutableListOf<MusicTrack>()
            for (i in 0 until songsArr.length()) {
                val obj = songsArr.getJSONObject(i)
                val imagesArr = obj.optJSONArray("image") ?: JSONArray()
                var img = ""
                if (imagesArr.length() > 0) {
                    img = imagesArr.getJSONObject(imagesArr.length() - 1).optString("url")
                }
                val downloadUrlsArr = obj.optJSONArray("downloadUrl") ?: JSONArray()
                var streamUrl = ""
                if (downloadUrlsArr.length() > 0) {
                    streamUrl = downloadUrlsArr.getJSONObject(downloadUrlsArr.length() - 1).optString("url")
                }
                list.add(
                    MusicTrack(
                        id = obj.optString("id", "track-$i"),
                        title = obj.optString("name", "Track").replace("&quot;", "\""),
                        album = obj.optJSONObject("album")?.optString("name", "") ?: "",
                        artists = obj.optString("label", "Artist"),
                        durationSec = obj.optInt("duration", 0),
                        imageUrl = img,
                        streamUrl = streamUrl,
                        downloadUrl = streamUrl
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 8. Open-Meteo Weather
    suspend fun getWeather(lat: Double = 23.8103, lon: Double = 90.4125, cityName: String = "Dhaka"): WeatherForecast = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto"
            val jsonStr = getJson(url)
            val root = JSONObject(jsonStr)
            val current = root.getJSONObject("current")
            val temp = current.optDouble("temperature_2m", 28.0)
            val humidity = current.optInt("relative_humidity_2m", 65)
            val wind = current.optDouble("wind_speed_10m", 12.0)
            val code = current.optInt("weather_code", 0)

            val daily = root.optJSONObject("daily")
            val times = daily?.optJSONArray("time") ?: JSONArray()
            val maxTemps = daily?.optJSONArray("temperature_2m_max") ?: JSONArray()
            val minTemps = daily?.optJSONArray("temperature_2m_min") ?: JSONArray()
            val codes = daily?.optJSONArray("weather_code") ?: JSONArray()

            val dailyList = mutableListOf<DailyWeatherItem>()
            val daysOfWeek = listOf("Today", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            for (i in 0 until minOf(7, times.length())) {
                val dCode = codes.optInt(i, 0)
                val label = if (i == 0) "Today" else times.optString(i).takeLast(5)
                dailyList.add(
                    DailyWeatherItem(
                        dayName = label,
                        condition = weatherCodeToDescription(dCode),
                        conditionCode = dCode,
                        maxTemp = maxTemps.optDouble(i, temp + 3),
                        minTemp = minTemps.optDouble(i, temp - 4)
                    )
                )
            }

            WeatherForecast(
                city = cityName,
                temp = temp,
                condition = weatherCodeToDescription(code),
                conditionCode = code,
                humidity = humidity,
                windSpeed = wind,
                maxTemp = if (dailyList.isNotEmpty()) dailyList[0].maxTemp else temp + 2,
                minTemp = if (dailyList.isNotEmpty()) dailyList[0].minTemp else temp - 4,
                dailyList = dailyList
            )
        } catch (e: Exception) {
            e.printStackTrace()
            WeatherForecast(
                city = cityName,
                temp = 28.0,
                condition = "Partly Cloudy",
                conditionCode = 2,
                humidity = 70,
                windSpeed = 11.5,
                maxTemp = 31.0,
                minTemp = 24.0,
                dailyList = listOf(
                    DailyWeatherItem("Today", "Clear", 0, 31.0, 24.0),
                    DailyWeatherItem("Tomorrow", "Sunny", 1, 32.0, 25.0)
                )
            )
        }
    }

    private fun weatherCodeToDescription(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1, 2, 3 -> "Partly Cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain Shower"
            71, 73, 75 -> "Light Snow"
            80, 81, 82 -> "Heavy Rain"
            95, 96, 99 -> "Thunderstorm"
            else -> "Pleasant"
        }
    }
}
