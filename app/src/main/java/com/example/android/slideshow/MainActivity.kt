package com.example.android.slideshow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.widget.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import model.Feed
import service.Filter
import service.SharedPreference
import service.Slideshow
import service.SortOption
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    val url : String = "https://api.myjson.com/bins/1cve3s" // Api link here
    val lastSlideIndex : String = "LAST_SLIDE_INDEX"

    var sharedPreference: SharedPreference? = null
    var feedTextView:TextView? = null
    var feedImageView:ImageView? = null
    var cbDescFilter:CheckBox? = null
    var btShuffle:Button? = null
    var rbGroup:RadioGroup? = null

    var progressBar:ProgressBar? = null

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
        // Thread policy for internet access
        val SDK_INT = android.os.Build.VERSION.SDK_INT
        if (SDK_INT > 8) {
            val policy = StrictMode.ThreadPolicy.Builder()
                .permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addStaticDemoSlideshowData()
        totalSlideCount = Slideshow.getTotalImageCount()

        // Restore saved data
        sharedPreference = SharedPreference(this)
        onRestoreLastIndex()

        feedTextView = findViewById(R.id.feedTitle)
        feedImageView = findViewById(R.id.imageView5)
        cbDescFilter = findViewById(R.id.cbDescFilter)
        btShuffle = findViewById(R.id.btShuffle)
        rbGroup = findViewById(R.id.rbGroup)
        progressBar = findViewById(R.id.progressBar)

        addEventListener()
        showNextFeed()

        // Co-routine call
        GlobalScope.launch {
            addWebHostedDemoSlideshowData()
            totalSlideCount = Slideshow.getTotalImageCount()
        }
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
        progressBar?.visibility = View.VISIBLE
        // Co-routine call
        GlobalScope.launch { showProgressBar() }

        feedTextView?.text = newFeed.title
        currentImageDescription = newFeed.description
        currentFeedNumber = Slideshow.getCurrentImageNumber()

        val resId = resources.getIdentifier(newFeed.imageUrl, "drawable", packageName)
        feedImageView?.setImageResource(resId)

        // Save current image index
        sharedPreference?.save(lastSlideIndex, Slideshow.getCurrentImageIndex())
    }

   suspend fun showProgressBar() {
       delay(5000)
       runOnUiThread {
           progressBar?.visibility = View.INVISIBLE
       }
    }

    private fun addStaticDemoSlideshowData(){
        Slideshow.addFeed(Feed("Cows", "", LocalDateTime.now(), "img4", null))
        Slideshow.addFeed(Feed("Cliffs of Moher", "An awesome view", LocalDateTime.now(), "img5", null))
    }

    private fun addWebHostedDemoSlideshowData() {
        var slides = loadApiData()
        if(slides.isNotEmpty()){
            slides.forEach { slide ->
                Slideshow.addFeed(slide)
            }
        }
    }

    private fun loadApiData() : List<Feed> {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()

            val jsonString = connection.inputStream.use { conn ->
                conn.reader().use { reader -> reader.readText() }
            }
            deserializeJsonString(jsonString)
        } catch (ex : Exception) {
            print("Error while accessing API $url. The following error occurred: $ex")
            listOf()
        }
    }

    private fun deserializeJsonString(resultText : String) : List<Feed> {
        return try {
            val objectType = object : TypeToken<List<Feed>>() {}.type
            Gson().fromJson<List<Feed>>(resultText, objectType)

        } catch(ex : java.lang.Exception){
            print("Unable to deserialize Json. The following error occurred: $ex")
            listOf()
        }
    }
}
