package com.gigforce.app.modules.help

data class HelpVideo(
    val id : String,
    val videoTitle : String,
    val videoLength : Int,
    val videoYoutubeId : String
) {

    fun getThumbNailUrl() : String{
        return "https://i3.ytimg.com/vi/$videoYoutubeId/hqdefault.jpg"
    }

    fun getFullVideoPath () : String{
        return "https://www.youtube.com/watch?v=$videoYoutubeId"
    }
}