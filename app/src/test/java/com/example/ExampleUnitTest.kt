package com.example

import com.example.data.api.M3uParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class M3uParserUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testM3uParsing() {
    val sampleM3u = """
      #EXTM3U
      #EXTINF:-1 tvg-id="ch1" tvg-name="Ekushey TV" tvg-logo="https://example.com/logo.png" group-title="Bangla",Ekushey TV
      http://stream.example.com/live.m3u8
      #EXTINF:-1 tvg-id="ch2" tvg-name="FIFA+" tvg-logo="https://example.com/fifa.png" group-title="Sports",FIFA+
      https://stream.example.com/fifa.m3u8
    """.trimIndent()

    val channels = M3uParser.parse(sampleM3u)
    assertEquals(2, channels.size)
    assertEquals("Ekushey TV", channels[0].name)
    assertEquals("Bangla", channels[0].groupTitle)
    assertTrue(channels[0].streamUrl.startsWith("http"))
    assertEquals("FIFA+", channels[1].name)
    assertEquals("Sports", channels[1].groupTitle)
  }
}
