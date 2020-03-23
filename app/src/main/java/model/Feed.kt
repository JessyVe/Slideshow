package model

import java.time.LocalDateTime

data class Feed(val title : String,
                val description : String,
                val timestamp : LocalDateTime,
                val imageUrl : String = "default",
                val gspLocation : String?)
{
    @delegate:Transient
    val base64ImageString: String by lazy {
        loadTheImageBase64()
    }

    @delegate:Transient
    val sound: String by lazy {
        loadingSoundFromFilesystem()
    }

    private fun loadingSoundFromFilesystem():String{
        print("expensive operation!")
        return "Sound file."
    }

    private fun loadTheImageBase64():String {
        print("expensive operation!")
        return "not implemented yet."
    }

    override fun toString(): String{
        return "Feed $title"
    }
}