package com.example.android.slideshow

import model.Feed
import org.junit.Test

import org.junit.Assert.*
import java.time.LocalDateTime

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class SingleFeedTest {
    @Test
    fun singleFeedToString() {
        val sunset = Feed("The beautiful sunset","",  LocalDateTime.now(), "img1", null)
        assertEquals("Feed The beautiful sunset", sunset.toString())
    }
}
