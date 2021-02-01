package com.gigforce.giger_app.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.viewdatamodels.*
import com.gigforce.giger_app.MainSectionDVM
import com.gigforce.giger_app.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
        private val homeCardsFBRepository: IHomeCardsFBRepository
) : ViewModel() {
    var isInitialized = false
    var allLandingData: MutableLiveData<List<Any>> = MutableLiveData<List<Any>>()

    private var _allLandingData: LiveData<List<Any>> = allLandingData

    fun getAllItems(): ArrayList<Any> {
        val arrayList = ArrayList<Any>()
        arrayList.add(
                StandardActionCardDVM(
                        R.drawable.ic_happy_announcement,
                        "What we do ?",
                        "Let's talk about what's a gig and how do you start working as a giger at Gigforce.",
                        "Play Now"
                )
        )
        arrayList.add(
                StandardActionCardDVM(
                        R.drawable.ic_tip,
                        "Gigforce Tip",
                        "Having  an experience can help you start earning fast",
                        "Update Now"
                )
        )


        arrayList.add(
                StandardActionCardDVM(
                        R.drawable.ic_ambassador_icon,
                        "Join Us as Ambassador",
                        "More you create profiles , more you earn .",
                        "Join Now"
                )
        )
        arrayList.add(
                StandardActionCardDVM(
                        R.drawable.my_interest_icon,
                        "My Interest",
                        "Explore Interesting Gigs to start Earning ",
                        ""
                )
        )
        arrayList.add(FeatureLayoutDVM("", "Explore Gig", getExploreGig()))
        arrayList.add(
                StandardActionCardDVM(
                        R.drawable.ic_set_preference,
                        "Set Your Preferences",
                        "Becoming a verified Giger for higher chances of getting recruited faster",
                        "Complete Now"
                )
        )
        arrayList.add(FeatureLayoutDVM(R.drawable.learning_icon, "Learning", getFeaturedItems()))
        arrayList.add(
                StandardActionCardDVM(
                        R.drawable.ic_complete_verification_icon,
                        "Complete your Verfication",
                        "Becoming a verified Giger for higher chances of getting recruited faster",
                        "Complete Now"
                )
        )

        arrayList.add(
                FeatureLayoutDVM(
                        R.drawable.ic_help_icon,
                        "Help",
                        getVideoItems()
                )
        )

        arrayList.add(MainSectionDVM("sec_main_nav"))

        return arrayList
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
        homeCardsFBRepository.getData().observeForever {
            it?.let {

                this.allLandingData.value = it
            }
        }
    }


}

interface IHomeCardsFBRepository {
    fun getData(): LiveData<List<Any>>
    fun loadData()
}


class HomeCardsFBRepository @Inject constructor() : IHomeCardsFBRepository {
    private var data: MutableLiveData<List<Any>> = MutableLiveData()
    val collectionName = "AppConfigs_Home"

    init {
        loadData()
    }

    fun getFirebaseReference() {
        FirebaseFirestore.getInstance()
                .collection(collectionName)
                .orderBy("index")
                .addSnapshotListener { value, error ->
                    value?.documents?.let {
                        val allData = ArrayList<Any>()
                        for (item in it) {
                            handleDataSnapshot(item)?.let { allData.add(it) }
                        }
                        data.value = allData
                    }
                }
    }

    fun handleDataSnapshot(snapshot: DocumentSnapshot): Any? {
        val type = snapshot.get("type") as? String ?: ""
        when (type) {
            "sec_action" -> {
                val title: String = snapshot.get("title") as? String ?: "-"
                val desc: String = snapshot.get("desc") as? String ?: "-"
                val imageUrl: String? = snapshot.get("imageUrl") as? String
                val actionP: Map<String, String>? = snapshot.get("action1") as? Map<String, String>
                        ?: null

                val action1 = ActionButton(title = actionP?.get("title"), navPath = actionP?.get("navPath"))
                val bgcolor = Integer.valueOf(snapshot.get("bgcolor")?.toString()?:"0") //Todo Integer value issue
                val marginRequired = snapshot.get("marginRequired") as? Boolean?:false
                return StandardActionCardDVM(null,
                        title = title,
                        subtitle = desc,
                        imageUrl = imageUrl,
                        action1 = action1,
                        bgcolor = bgcolor,
                        marginRequired = marginRequired
                )
            }
            "sec_main_nav" -> {
                return MainSectionDVM(type = type)
            }
            else -> return null
        }
    }

    override fun getData(): LiveData<List<Any>> {
        return data
    }

    override fun loadData() {
        getFirebaseReference()
    }

}