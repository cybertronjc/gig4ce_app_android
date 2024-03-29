package com.gigforce.giger_app.vm

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.gig.GigsRepository
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.giger_app.repo.IUpcomingGigInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpcomingGigsViewModel @Inject constructor(
    private val repository: IUpcomingGigInfoRepository,
    private val gigsRepository: GigsRepository,
    private val logger : GigforceLogger
    ) :
    ViewModel() {

    private var _data: MutableLiveData<List<Gig>> = MutableLiveData()
    var data: LiveData<List<Gig>> = _data
    var state: Parcelable? = null

    init {
        loadData()
    }

    private fun loadData() = viewModelScope.launch{
        try {
            gigsRepository.getUpcomingGigs()
                .collect {
                    _data.value = it
                }
        }catch (e:Exception){
            logger.e(
                "UpcomingGigsViewModel",
                "while getting upcomin gigs",
                e
            )
        }
    }

}