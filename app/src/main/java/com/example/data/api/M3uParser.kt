package com.example.data.api

import com.example.data.model.TvChannel
import java.io.BufferedReader
import java.io.StringReader
import java.util.UUID

object M3uParser {
    fun parse(m3uContent: String): List<TvChannel> {
        val channels = mutableListOf<TvChannel>()
        val reader = BufferedReader(StringReader(m3uContent))
        var currentTvgName = ""
        var currentLogo = ""
        var currentGroup = "General"
        var currentId = ""
        var currentDisplayName = ""

        var line: String? = reader.readLine()
        while (line != null) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#EXTINF:", ignoreCase = true)) {
                // Parse attributes
                currentId = extractAttr(trimmed, "tvg-id").ifEmpty { UUID.randomUUID().toString() }
                currentTvgName = extractAttr(trimmed, "tvg-name")
                currentLogo = extractAttr(trimmed, "tvg-logo")
                currentGroup = extractAttr(trimmed, "group-title").ifEmpty { "General" }

                // The display name is after the last comma
                val commaIndex = trimmed.lastIndexOf(',')
                currentDisplayName = if (commaIndex != -1 && commaIndex < trimmed.length - 1) {
                    trimmed.substring(commaIndex + 1).trim()
                } else {
                    currentTvgName.ifEmpty { "Live Channel" }
                }
            } else if (trimmed.isNotBlank() && !trimmed.startsWith("#")) {
                // This is the stream URL
                if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
                    val name = if (currentDisplayName.isNotBlank()) currentDisplayName else currentTvgName.ifEmpty { "Channel" }
                    channels.add(
                        TvChannel(
                            id = currentId.ifEmpty { UUID.randomUUID().toString() },
                            name = cleanChannelName(name),
                            logo = currentLogo,
                            groupTitle = normalizeGroup(currentGroup),
                            streamUrl = trimmed
                        )
                    )
                }
                // Reset
                currentTvgName = ""
                currentLogo = ""
                currentGroup = "General"
                currentId = ""
                currentDisplayName = ""
            }
            line = reader.readLine()
        }
        return channels
    }

    private fun extractAttr(line: String, attr: String): String {
        val pattern = """$attr="([^"]*)"""".toRegex(RegexOption.IGNORE_CASE)
        val match = pattern.find(line)
        return match?.groupValues?.getOrNull(1) ?: ""
    }

    private fun cleanChannelName(raw: String): String {
        return raw.replace(Regex("""^\s*(MIXER\s*\|\s*|FB\s*\|\s*)"""), "").trim()
    }

    private fun normalizeGroup(group: String): String {
        val lower = group.trim().lowercase()
        return when {
            lower.contains("bangla") || lower.contains("akash") -> "Bangla"
            lower.contains("sport") -> "Sports"
            lower.contains("news") -> "News"
            lower.contains("movie") || lower.contains("cinema") -> "Movies"
            lower.contains("kid") || lower.contains("cartoon") -> "Kids"
            lower.contains("music") -> "Music"
            lower.contains("promo") -> "Featured"
            else -> group.trim().ifEmpty { "Entertainment" }
        }
    }
}
