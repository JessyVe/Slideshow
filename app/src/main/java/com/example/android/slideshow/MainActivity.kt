package com.example.android.slideshow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import model.Feed
import service.Filter
import service.SharedPreference
import service.Slideshow
import service.SortOption
import java.io.Console
import java.lang.Exception
import java.time.LocalDateTime
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    val lastSlideIndex : String = "LAST_SLIDE_INDEX"

    var sharedPreference: SharedPreference? = null
    var feedTextView:TextView? = null
    var feedImageView:ImageView? = null
    var cbDescFilter:CheckBox? = null
    var btShuffle:Button? = null
    var rbGroup:RadioGroup? = null

    var totalSlideCount:Int = 0

    var currentFeedNumber:Int by Delegates.observable(0){
        _, _, newFeedNumber ->
        findViewById<TextView>(R.id.feedNumber).text = "$newFeedNumber of $totalSlideCount"
    }

    var currentImageDescription:String by Delegates.observable(""){
            _, _, newDescription ->
        findViewById<TextView>(R.id.tvImageDesc).text = newDescription
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addSomeDemoSlideshowData()
        totalSlideCount = Slideshow.getTotalImageCount()

        // Restore saved data
        sharedPreference = SharedPreference(this)
        onRestoreLastIndex()

        feedTextView = findViewById(R.id.feedTitle)
        feedImageView = findViewById(R.id.imageView5)
        cbDescFilter = findViewById(R.id.cbDescFilter)
        btShuffle = findViewById(R.id.btShuffle)
        rbGroup = findViewById(R.id.rbGroup)

        addEventListener()
        showNextFeed()
    }

    private fun onRestoreLastIndex() {
        // Subtract 1 from index, because always the next slide is called not the current
        val index : Int? = sharedPreference?.getValueInt(lastSlideIndex)
        if(index == null) {
            Slideshow.setCurrentImageIndex(0 - 1)
        }
        else {
            Slideshow.setCurrentImageIndex(index - 1)
            Toast.makeText(applicationContext,"Welcome back!\nSlideshow started at image #" + (index + 1), Toast.LENGTH_LONG).show()
        }
    }

    private fun addEventListener() {
        feedImageView?.setOnClickListener { showNextFeed() }
        feedTextView?.setOnClickListener { showPreviousFeed() }

        cbDescFilter?.setOnCheckedChangeListener { _, isChecked ->
            toggleFilter(Filter.DESCRIPTION, isChecked)
        }

        btShuffle?.setOnClickListener { shuffleImages() }
        rbGroup?.setOnCheckedChangeListener { _, checkedId -> sortImages(checkedId) }
    }

    private fun toggleFilter(filter : Filter, isCheckBox: Boolean) {
        when(isCheckBox){
            true -> Slideshow.setFilter(filter)
            false -> Slideshow.removeFilter(filter)
        }
        totalSlideCount = Slideshow.getTotalImageCount()
        showNextFeed()
    }

    private fun shuffleImages() {
        Slideshow.shuffleImages()
        showNextFeed()
    }

    private fun sortImages(checkedId : Int){
        val radio: RadioButton = findViewById(checkedId)

        // TODO: Is there a better way? (unable to retrieve manually given Id/String id)
        when(radio.text) {
            "Title" -> Slideshow.sortImages(SortOption.TITLE)
            "Id" -> Slideshow.sortImages(SortOption.ID)
            "Time" -> Slideshow.sortImages(SortOption.TIME)
        }
        showNextFeed()
    }

    private fun showPreviousFeed(){
        displayFeed(Slideshow.getPreviousSlide())
    }

    private fun showNextFeed(){
        displayFeed(Slideshow.getNextSlide())
    }

    private fun displayFeed(newFeed:Feed) {
        feedTextView?.text = newFeed.title

        currentImageDescription = newFeed.description
        currentFeedNumber = Slideshow.getCurrentImageNumber()

        val resId = resources.getIdentifier(newFeed.imageUrl, "drawable", packageName)
        feedImageView?.setImageResource(resId)

        // Save current image index.
        sharedPreference?.save(lastSlideIndex, Slideshow.getCurrentImageIndex())
    }

    private fun addSomeDemoSlideshowData(){
        Slideshow.addFeed(Feed("Coffee", "Developers fuel", LocalDateTime.now(), "img3", "Dublin"))
        Slideshow.addFeed(Feed("On the beach", "Beach of Irland", LocalDateTime.now(), "img2", null))
        Slideshow.addFeed(Feed("Half a penny bridge", "", LocalDateTime.now(), "img1", "Dublin"))
        Slideshow.addFeed(Feed("Cows", "", LocalDateTime.now(), "img4", null))
        Slideshow.addFeed(Feed("Cliffs of Moher", "An awesome view", LocalDateTime.now(), "img5", null))
    }
}
