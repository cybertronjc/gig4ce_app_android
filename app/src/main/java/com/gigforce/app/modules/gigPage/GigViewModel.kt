package com.gigforce.app.modules.gigPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.gigPage.models.*
import java.util.*

class GigViewModel constructor(
    private val gigRepository: GigRepository = GigRepository()
) : ViewModel() {

    private val _gigDetails = MutableLiveData<Gig>()
    val gigDetails: LiveData<Gig> get() = _gigDetails

    fun getPresentGig(gigId: String) {

        val loc  = GigLocationDetails(
            latitude = 34.2332,
            longitude = 43.2323,
            fullAddress = "fulll Addresss"
        )

        val attendance = GigAttendance(
            checkInMarked = true,
            checkInLat = 34.2323,
            checkInLong = 56.233,
            checkInImage = "Image_name.jpg",
            checkOutMarked = true,
            checkOutLat = 34.2323,
            checkOutLong = 56.233,
            checkOutImage = "Image_name.jpg"
        )

        val details = GigDetails(
            startTime = Date(),
            endTime = Date(),
            wage = "34 Per Hour",
            shiftDuration = "5 Hours",
            address = "Some Address"
        )

        val gigContactDetails = GigContactDetails(
            contactName = "Name",
            mobileNo = "9798948823"
        )

        val gig = Gig(
            gigId = "GIG00001",
            title = "Sales Executive",
            startDate = Date(),
            companyName = "Company XYZ",
            contactNo = "9892849832948",
            gigType = "On Site",
            gigHighLights = listOf(
                "Provide Advice On Purchase",
                "Communication with clients and customers about their experience with a products and services."
            ),
            gigRequirements = listOf(
                "Req 1",
                "Req 2"
            ),
            gigLocationDetails = loc,
            gigDetails = details,
            attendance = attendance,
            gigContactDetails = gigContactDetails
        )

        _gigDetails.value = gig
    }
}