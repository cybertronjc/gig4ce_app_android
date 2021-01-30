package com.gigforce.giger_app.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.viewdatamodels.*
import com.gigforce.giger_app.R

class LandingViewModel(
    private val state: SavedStateHandle
) : ViewModel() {
    var isInitialized = false
    val allLandingData: MutableLiveData<ArrayList<Any>>
        get() {return state.getLiveData("allLandingData",ArrayList<Any>())}

    private fun setLandingData(data:ArrayList<Any>){
        state.set("allLandingData", data)
    }

    //    val allLandingData: LiveData<ArrayList<Any>> = _allLandingData
    private var _allLandingData: LiveData<ArrayList<Any>> = allLandingData

    private fun getAllItems(): ArrayList<Any> {
        val arrayList = ArrayList<Any>()
        arrayList.add(
            StandardActionCardDVM(
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
            StandardActionCardDVM(
                R.drawable.ic_ambassador_icon,
                "Join Us as Ambassador",
                "More you create profiles , more you earn .",
                "Join Now",
                ""
            )
        )
        arrayList.add(
            StandardActionCardDVM(
                R.drawable.my_interest_icon,
                "My Interest",
                "Explore Interesting Gigs to start Earning ",
                "",
                ""
            )
        )
        arrayList.add(FeatureLayoutDVM("", "Explore Gig", getExploreGig()))
        arrayList.add(
            StandardActionCardDVM(
                R.drawable.ic_set_preference,
                "Set Your Preferences",
                "Becoming a verified Giger for higher chances of getting recruited faster",
                "Complete Now",
                ""
            )
        )
        arrayList.add(FeatureLayoutDVM(R.drawable.learning_icon, "Learning", getFeaturedItems()))
        arrayList.add(
            StandardActionCardDVM(
                R.drawable.ic_complete_verification_icon,
                "Complete your Verfication",
                "Becoming a verified Giger for higher chances of getting recruited faster",
                "Complete Now",
                ""
            )
        )


        arrayList.add(
            FeatureLayoutDVM(
                R.drawable.ic_help_icon,
                "Help",
                getVideoItems()
            )
        )

        arrayList.add(FeatureLayoutDVM("", "Feature", getFeatureItems1()))

        return arrayList
    }

    private fun getFeatureItems1(): List<Any> {
        val featureItems = ArrayList<Any>()
        featureItems.add(FeatureItemCard2DVM(R.drawable.ic_tip, "My Gig"))
        featureItems.add(FeatureItemCard2DVM(R.drawable.ic_tip, "Wallet"))
        featureItems.add(FeatureItemCard2DVM(R.drawable.ic_tip, "Learning"))
        featureItems.add(FeatureItemCard2DVM(R.drawable.ic_tip, "Chat"))
        featureItems.add(FeatureItemCard2DVM(R.drawable.ic_tip, "Explore"))
        featureItems.add(FeatureItemCard2DVM(R.drawable.ic_tip, "Profile", navPath = "profile"))
        featureItems.add(FeatureItemCard2DVM(R.drawable.ic_tip, "Verification"))
        featureItems.add(FeatureItemCard2DVM(R.drawable.ic_tip, "Settings", "setting"))
        return featureItems
    }

    private fun getExploreGig(): List<Any> {
        val exploreGigItems = ArrayList<Any>()
        exploreGigItems.add(
            FeatureItemCardDVM(
                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/client_activation%2F21North%2F21N_cover_pic.jpg?alt=media&token=52dc3fbd-22e4-4ffa-a2d3-2e61915e0291",
                "21North",
                ""
            )
        )

        exploreGigItems.add(
            FeatureItemCardDVM(
                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/client_activation%2F21North%2F21N_cover_pic.jpg?alt=media&token=52dc3fbd-22e4-4ffa-a2d3-2e61915e0291",
                "21North",
                ""
            )
        )

        return exploreGigItems
    }

    private fun getFeaturedItems(): List<Any> {
        val featureItems = ArrayList<Any>()
        featureItems.add(
            FeatureItemCardDVM(
                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/gig4ce-files-bucket%2Fbehavioral_skills%2Fcourse_behavioural_skills.jpg?alt=media&token=b6d54c0a-0e9a-4f0d-8134-83b96c8aa64a",
                "Behavioral Skills",
                "Level 1"
            )
        )
        featureItems.add(
            FeatureItemCardDVM(
                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/gig4ce-files-bucket%2Fbehavioral_skills%2Fbehave_2a.jpg?alt=media&token=6022178a-87c5-4b18-8091-76b5889ed79f",
                "Behavioral Skills",
                "Level 2"
            )
        )
        return featureItems
    }

    private fun getVideoItems(): ArrayList<Any> {
        val videoItemsList = ArrayList<Any>()
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
        // _allLandingData.postValue(getAllItems())
        if(!isInitialized) {
            setLandingData(getAllItems())
            this.allLandingData.postValue(getAllItems())
            isInitialized = true
        }

    }

}