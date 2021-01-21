package com.gigforce.giger_app.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.viewdatamodels.*
import com.gigforce.giger_app.R

class LandingViewModel : ViewModel() {
    private var _allLandingData: MutableLiveData<ArrayList<Any>> = MutableLiveData()
    val allLandingData: LiveData<ArrayList<Any>> = _allLandingData
    private fun getAllItems(): ArrayList<Any> {
        var arrayList = ArrayList<Any>()
        arrayList.add(
            StandardActionLightPinkCardDVM(
                R.drawable.ic_happy_announcement,
                "What we do ?",
                "Let's talk about what's a gig and how do you start working as a giger at Gigforce.",
                "Play Now",
                "Skip"
            )
        )
        arrayList.add(
            StandardActionCardDVM(
                R.drawable.ic_tip,
                "Gigforce Tip",
                "Having  an experience can help you start earning fast",
                "Update Now",
                "No Experience"
            )
        )


        arrayList.add(
            StandardActionLblueDVM(
                R.drawable.ic_ambassador_icon,
                "Join Us as Ambassador",
                "More you create profiles , more you earn .",
                "Join Now",
                ""
            )
        )
        arrayList.add(
            StandardActionLipstickCardDVM(
                R.drawable.my_interest_icon,
                "My Interest",
                "Explore Interesting Gigs to start Earning ",
                "",
                ""
            )
        )
        arrayList.add(FeatureLayoutDVM("", "Explore Gig", getExploreGig()))
        arrayList.add(
            StandardActionGreyCardDVM(
                R.drawable.ic_set_preference,
                "Set Your Preferences",
                "Becoming a verified Giger for higher chances of getting recruited faster",
                "Complete Now",
                ""
            )
        )
        arrayList.add(FeatureLayoutDVM(R.drawable.learning_icon, "Learning", getFeaturedItems()))
        arrayList.add(
            StandardActionGreyCardDVM(
                R.drawable.ic_complete_verification_icon,
                "Complete your Verfication",
                "Becoming a verified Giger for higher chances of getting recruited faster",
                "Complete Now",
                ""
            )
        )


        arrayList.add(
            VideoInfoLayoutDVM(
                R.drawable.ic_help_icon,
                "Help",
                getVideoItems(),
                "Load more"
            )
        )
        return arrayList
    }

    private fun getExploreGig(): List<Any> {
        var exploreGigItems = ArrayList<Any>()
        exploreGigItems.add(
            FeatureItemDVM(
                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/client_activation%2F21North%2F21N_cover_pic.jpg?alt=media&token=52dc3fbd-22e4-4ffa-a2d3-2e61915e0291",
                "21North",
                ""
            )
        )
        return exploreGigItems
    }

    private fun getFeaturedItems(): List<Any> {
        var featureItems = ArrayList<Any>()
        featureItems.add(
            FeatureItemDVM(
                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/gig4ce-files-bucket%2Fbehavioral_skills%2Fcourse_behavioural_skills.jpg?alt=media&token=b6d54c0a-0e9a-4f0d-8134-83b96c8aa64a",
                "Behavioral Skills",
                "Level 1"
            )
        )
        featureItems.add(
            FeatureItemDVM(
                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/gig4ce-files-bucket%2Fbehavioral_skills%2Fbehave_2a.jpg?alt=media&token=6022178a-87c5-4b18-8091-76b5889ed79f",
                "Behavioral Skills",
                "Level 2"
            )
        )
        return featureItems
    }

    private fun getVideoItems(): ArrayList<Any> {
        var videoItemsList = ArrayList<Any>()
        videoItemsList.add(
            VideoItemCardDVM(
                "https://i3.ytimg.com/vi/FbiyRe49wjY/hqdefault.jpg",
                "[Hindi] overview",
                true,
                "03:32"
            )
        )
        videoItemsList.add(
            VideoItemCardDVM(
                "https://www.youtube.com/watch?v=PnnhrIZCWeA",
                "[Hindi] language",
                true,
                "00:35"
            )
        )
        return videoItemsList
    }

    init {
        _allLandingData.postValue(getAllItems())
    }

}