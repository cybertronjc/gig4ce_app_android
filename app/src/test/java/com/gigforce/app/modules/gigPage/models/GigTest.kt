package com.gigforce.app.modules.gigPage.models

import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import org.junit.Test
import java.util.Date

class GigTest {

    @Test
    fun generateGigJson() {



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
            gigerId = "gigerId",
            title = "Sales Executive",
            startDate = Timestamp.now(),
            duration = 3.1F,
            gigAmount = 10000.0,
            gigRating = 4.5F,
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




        print(Gson().toJson(gig))
    }

}