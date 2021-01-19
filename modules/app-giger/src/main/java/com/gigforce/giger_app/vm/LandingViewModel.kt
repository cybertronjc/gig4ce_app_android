package com.gigforce.giger_app.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.viewdatamodels.TipActionCardDVM
import com.gigforce.common_ui.viewdatamodels.VideoInfoDVM
import com.gigforce.giger_app.R

class LandingViewModel : ViewModel() {
    private var _allLandingData: MutableLiveData<ArrayList<Any>> = MutableLiveData()
    val allLandingData: LiveData<ArrayList<Any>> = _allLandingData
    private fun getAllItems(): ArrayList<Any> {
        var arrayList = ArrayList<Any>()
        arrayList.add(TipActionCardDVM(R.drawable.btn_clear,"What we do ?", "Lorem Ipsum is simply dummy text of the printing and", "Play Now","Skip"))
        arrayList.add(TipActionCardDVM(R.drawable.image_placeholder,"Gigforce Tip", "Having  an experience can help you start earning fast", "Update Now","No Experience"))
        arrayList.add(TipActionCardDVM(R.drawable.common_google_signin_btn_icon_dark_focused,"My Interest", "Explore Interesting Gigs to start Earning ", "",""))
        arrayList.add(TipActionCardDVM(null, "Set Your Preferences","Becoming a verified Giger for higher chances of getting recruited faster", "Complete Now",""))
        arrayList.add(TipActionCardDVM("https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/gig4ce-files-bucket%2FAPP%20LOGO.png?alt=media&token=ac5c3fe6-96e9-4f04-b671-d6edcc25afa8","Complete your Verfication", "Becoming a verified Giger for higher chances of getting recruited faster", "Complete Now",""))
        arrayList.add(VideoInfoDVM("Help"))
        return arrayList
    }

    init {
        _allLandingData.postValue(getAllItems())
    }

}