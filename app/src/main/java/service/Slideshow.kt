package service

import model.Feed

object Slideshow {
    private var slides = mutableListOf<Feed>()
    private var filteredSlides = mutableListOf<Feed>()
    private var currentIndex:Int = -1

    private var activeFilters = mutableListOf<Filter>()
    private var activeSortOption:SortOption = SortOption.TITLE

    fun getNextSlide() : Feed{
        return when (hasNext()) {
            true -> filteredSlides[++currentIndex]
            false -> {
                resetSlideShow() // restart mechanism of slideshow
                getNextSlide()
            }
        }
    }

    fun getPreviousSlide() : Feed{
        return when (hasPrevious()) {
            true -> filteredSlides[--currentIndex]
            false -> filteredSlides[currentIndex]
        }
    }

    private fun hasNext() : Boolean{
        return (currentIndex + 1) < filteredSlides.count()
    }

    private fun hasPrevious():  Boolean{
        return (currentIndex - 1 > -1) && filteredSlides.isNotEmpty()
    }

    private fun resetSlideShow(){
        currentIndex = -1
    }

    fun addFeed(newFeed : Feed){
        slides.add(newFeed)
        refillFilteredList()
    }

    fun getCurrentIndex() : Int{
        return currentIndex + 1
    }

    fun getTotalImageCount() : Int {
        return filteredSlides.count()
    }

    fun setFilter(newFilter : Filter){
        activeFilters.add(newFilter)
        refillFilteredList()
    }

    fun removeFilter(newFilter : Filter){
        activeFilters.remove(newFilter)
        refillFilteredList()
    }

    private fun refillFilteredList(){
        filteredSlides.clear()

        if(activeFilters.isEmpty()){
            filteredSlides = slides.toMutableList()
        }else {
            slides.forEach { slide ->
                activeFilters.forEach { filter ->
                    when (filter) {
                        Filter.DESCRIPTION -> {
                            if (slide.description.isNotEmpty())
                                filteredSlides.add(slide)
                        }
                    }
                }
            }
        }
        sortImages(activeSortOption)
        resetSlideShow()
    }

    fun shuffleImages(){
        filteredSlides.shuffle()
        resetSlideShow()
    }

    fun sortImages(sortOption : SortOption){
        activeSortOption = sortOption
        when(sortOption){
            SortOption.ID -> filteredSlides.sortBy { slide -> slide.imageUrl }
            SortOption.TIME -> filteredSlides.sortBy { slide -> slide.timestamp }
            SortOption.TITLE -> filteredSlides.sortBy { slide -> slide.title }
        }
        resetSlideShow()
    }

    override fun toString(): String {
        val sortedFeeds = slides.sortedBy { it.title }
        return sortedFeeds.joinToString(" and ","Slides: ")
    }
}

enum class Filter {
    DESCRIPTION
}

enum class SortOption {
    TITLE,
    ID,
    TIME
}