package com.gigforce.giger_gigs.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.utils.Lce
import com.gigforce.app.data.repositoriesImpl.gigs.GigersAttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class GigerAttendanceViewModel @Inject constructor(
    private val gigersAttendanceRepository: GigersAttendanceRepository,
    private val logger: GigforceLogger
) : ViewModel() {

    companion object {
        const val TAG = "GigerAttendanceViewModel"
    }

    private val _monthlyGigs = MutableLiveData<Lce<List<Gig>>>()
    val monthlyGigs: LiveData<Lce<List<Gig>>> get() = _monthlyGigs

    fun getGigsForMonth(
        jobProfileId: String,
        gigerId : String?,
        month: Int,
        year: Int
    ) = viewModelScope.launch {
        logger.d(
            TAG,
            "getting monthly attendance for jobProfileId : $jobProfileId,  month : $month, year : $year"
        )

        try {
            _monthlyGigs.value = Lce.loading()
            val attendance = gigersAttendanceRepository
                .getAttendanceMonthly(
                    jobProfileId = jobProfileId,
                    userId = gigerId,
                    month = month,
                    year = year
                )

            _monthlyGigs.value = Lce.content(attendance)
        } catch (e: Exception) {
            logger.e(TAG, "Error while getting attendance", e)

            _monthlyGigs.value = Lce.error(e.message!!)
        }
    }

}
