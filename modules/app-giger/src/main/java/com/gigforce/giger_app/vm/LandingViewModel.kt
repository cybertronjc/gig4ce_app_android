package com.gigforce.giger_app.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import com.gigforce.common_ui.viewdatamodels.StandardActionCardDVM
import com.gigforce.common_ui.viewdatamodels.VideoItemCardDVM
import com.gigforce.giger_app.MainSectionDVM
import com.gigforce.giger_app.R
import com.gigforce.giger_app.repo.IHomeCardsFBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val homeCardsFBRepository: IHomeCardsFBRepository
) : ViewModel() {
    private var allLandingData: MutableLiveData<List<Any>> = MutableLiveData<List<Any>>()

    var _allLandingData: LiveData<List<Any>> = allLandingData

    fun getAllItems(): ArrayList<Any> {
        val arrayList = ArrayList<Any>()
        arrayList.add(
            FeatureLayoutDVM(
                R.drawable.ic_help_icon,
                "Help",
                getVideoItems()
            )
        )

        return arrayList
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