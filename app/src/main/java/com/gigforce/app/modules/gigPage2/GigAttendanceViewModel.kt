package com.gigforce.app.modules.gigPage2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.gigPage.GigsRepository
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage.models.GigRegularisationRequest
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.Lse
import com.gigforce.app.utils.getOrThrow
import com.gigforce.app.utils.updateOrThrow
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.time.LocalDateTime

enum class AttendanceTabOptions {
    ALL, WORKED, OFF;
}

class GigAttendanceViewModel constructor(
    private val gigsRepository: GigsRepository = GigsRepository(),
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
) : ViewModel() {

    private var optionSelected: AttendanceTabOptions = AttendanceTabOptions.ALL
    private var monthGigs : List<Gig>? = null

    fun filterGigs(optionSelected: AttendanceTabOptions){
        this.optionSelected = optionSelected

        if(monthGigs != null){
            
        }
    }

    private fun extractGigs(querySnapshot: QuerySnapshot): MutableList<Gig> {
        val userGigs: MutableList<Gig> = mutableListOf()
        querySnapshot.documents.forEach { t ->
            t.toObject(Gig::class.java)?.let {
                it.gigId = t.id
                userGigs.add(it)
            }
        }
        return userGigs
    }


    private val _monthlyGigs = MutableLiveData<Lce<List<Gig>>>()
    val monthlyGigs: LiveData<Lce<List<Gig>>> get() = _monthlyGigs

    fun getGigsForMonth(companyName: String, month: Int, year: Int) = viewModelScope.launch {

        val monthStart = LocalDateTime.of(year, month, 1, 0, 0)
        val monthEnd = monthStart.plusMonths(1).withDayOfMonth(1).minusDays(1);

        try {
            _monthlyGigs.value = Lce.loading()
            val querySnap = gigsRepository
                .getCurrentUserGigs()
//                .whereGreaterThan("startDateTime", monthStart)
//                .whereLessThan("startDateTime", monthEnd)
                .whereEqualTo("companyName", companyName)
                .getOrThrow()

            val gigs = extractGigs(querySnap)
            _monthlyGigs.value = Lce.content(gigs)
        } catch (e: Exception) {
            _monthlyGigs.value = Lce.error(e.message!!)
        }
    }

    private val _requestAttendanceRegularisation = MutableLiveData<Lse>()
    val requestAttendanceRegularisation: LiveData<Lse> get() = _requestAttendanceRegularisation

    fun requestRegularisation(
        gigId: String,
        punchInTime: Timestamp,
        punchOutTime: Timestamp
    ) = viewModelScope.launch {
        _requestAttendanceRegularisation.value = Lse.loading()

        try {
            val gigRegularisationRequest = GigRegularisationRequest().apply {
                checkInTime = punchInTime
                checkOutTime = punchOutTime
                requestedOn = Timestamp.now()
            }

            gigsRepository.getCollectionReference()
                .document(gigId)
                .updateOrThrow("regularisationRequest", gigRegularisationRequest)

            _requestAttendanceRegularisation.value = Lse.success()
        } catch (e: Exception) {
            _requestAttendanceRegularisation.value =
                Lse.error(e.message ?: "Unable to submit regularisation attendance")
        }
    }
}