package com.example.android.slideshow

import model.Feed
import org.junit.Test

import org.junit.Assert.*
import service.Slideshow
import java.time.LocalDateTime

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class SlideshowTest {
    @Test
    fun toStringResultTest () {
        var mySlideshow = Slideshow
        mySlideshow.addFeed(Feed("img 1", "desc 1", LocalDateTime.now(), "img1", "Graz"))
        mySlideshow.addFeed(Feed("img 2", "desc 2", LocalDateTime.now(), "img2", null))
        mySlideshow.addFeed(Feed("img 3", "desc 3", LocalDateTime.now(), "img3", "Kapfenberg"))

        assertEquals("Slides: Feed img 1 and Feed img 2 and Feed img 3", mySlideshow.toString())
    }
}
