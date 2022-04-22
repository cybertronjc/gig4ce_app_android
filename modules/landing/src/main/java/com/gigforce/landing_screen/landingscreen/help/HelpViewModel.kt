package com.gigforce.landing_screen.landingscreen.help

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.extensions.getOrThrow
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

data class HelpSupport(val number : String?="")

class HelpViewModel : ViewModel() {

    private val _helpVideos = MutableLiveData<ArrayList<HelpVideo>>()
    val helpVideos : LiveData<ArrayList<HelpVideo>> = _helpVideos

    private val _helpAndSupportMobileNumber = MutableLiveData<HelpSupport>()
    val helpAndSupportMobileNumber:LiveData<HelpSupport> = _helpAndSupportMobileNumber
    init {
        getHelpAndSupportMobileNumber()
    }
    private fun getHelpAndSupportMobileNumber() = viewModelScope.launch{
        val helpSupport = FirebaseFirestore.getInstance().collection("Configuration").document("help_support").getOrThrow()
        val helpSupportObject = helpSupport.toObject(HelpSupport::class.java)
        helpSupportObject?.let {
            _helpAndSupportMobileNumber.value = it
        }
    }

    fun getTopHelpVideos(){

        val vid1 = HelpVideo(
            id = UUID.randomUUID().toString(),
            videoTitle = "[Hindi] 1. Gigforce App का Overview",
            videoLength = 212,
            videoYoutubeId = "FbiyRe49wjY"
        )

        val vid2 = HelpVideo(
            id = UUID.randomUUID().toString(),
            videoTitle = "[Hindi] 2. App में अपनी पसंदीदा भाषा कैसे चुनें",
            videoLength = 35,
            videoYoutubeId = "PnnhrIZCWeA"
        )

        _helpVideos.value = arrayListOf(vid1,vid2)
    }

    fun getAllHelpVideos(){

        val vid1 = HelpVideo(
            id = UUID.randomUUID().toString(),
            videoTitle = "[Hindi] 1. Gigforce App का Overview",
            videoLength = 212,
            videoYoutubeId = "FbiyRe49wjY"
        )

        val vid2 = HelpVideo(
            id = UUID.randomUUID().toString(),
            videoTitle = "[Hindi] 2. App में अपनी पसंदीदा भाषा कैसे चुनें",
            videoLength = 35,
            videoYoutubeId = "PnnhrIZCWeA"
        )


        val vid3 = HelpVideo(
            id = UUID.randomUUID().toString(),
            videoTitle = "[Hindi] 3. How to upload a profile photo",
            videoLength = 26,
            videoYoutubeId = "usZgFZga7xE"
        )

        val vid4 = HelpVideo(
            id = UUID.randomUUID().toString(),
            videoTitle = "[Hindi] 4. How to update Hashtags and Headline in Gigforce App",
            videoLength = 43,
            videoYoutubeId = "ZmVMCduB96I"
        )

        val vid5 = HelpVideo(
            id = UUID.randomUUID().toString(),
            videoTitle = "[Hindi] 5. How to Update your Educational Experiences",
            videoLength = 63,
            videoYoutubeId = "-ldRk7xrO6Q"
        )

        val vid6 = HelpVideo(
            id = UUID.randomUUID().toString(),
            videoTitle = "[Hindi] 6. How to add Work Experience in your Profile on Gigforce App",
            videoLength = 44,
            videoYoutubeId = "uRBQd0rNdjk"
        )


        val vid7 = HelpVideo(
            id = UUID.randomUUID().toString(),
            videoTitle = "[Hindi] 7. How to add Date and Time Preference in Gigforce App",
            videoLength = 45,
            videoYoutubeId = "AValFSkvkFE"
        )

        val vid8 = HelpVideo(
            id = UUID.randomUUID().toString(),
            videoTitle = "[Hindi] 8. How to add your Work From Home Preference",
            videoLength = 37,
            videoYoutubeId = "A7_lLdHIOac"
        )

        val vid9 = HelpVideo(
            id = UUID.randomUUID().toString(),
            videoTitle = "[Hindi] 9. How to set Monthly Salary Goal in Gigforce App",
            videoLength = 42,
            videoYoutubeId = "xKd_xy7DYQg"
        )

        val vid10 = HelpVideo(
            id = UUID.randomUUID().toString(),
            videoTitle = "[Hindi] 10. How to set Monthly Contract Goal in Gigforce App",
            videoLength = 36,
            videoYoutubeId = "-xCRjrOltzQ"
        )

        val vid11 = HelpVideo(
            id = UUID.randomUUID().toString(),
            videoTitle = "[Hindi] 11. How to upload your Verification documents",
            videoLength = 248,
            videoYoutubeId = "8QZCJ3NMsy4"
        )

        val vid12 = HelpVideo(
            id = UUID.randomUUID().toString(),
            videoTitle = "[Hindi] 12. How to mark attendance for a gig",
            videoLength = 84,
            videoYoutubeId = "zhouZra1Lck"
        )

        val vid13 = HelpVideo(
            id = UUID.randomUUID().toString(),
            videoTitle = "[Hindi] 13. Calendar और Home Screen के बीच कैसे स्विच करें",
            videoLength = 43,
            videoYoutubeId = "AjF50egitnA"
        )

        _helpVideos.value = arrayListOf(
            vid1,
            vid2,
            vid3,
            vid4,
            vid5,
            vid6,
            vid7,
            vid8,
            vid9,
            vid10,
            vid11,
            vid12,
            vid13
        )
    }

}