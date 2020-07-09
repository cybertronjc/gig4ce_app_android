package com.gigforce.app.modules.gigPage.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import org.junit.Test
import java.util.*

class GigTest {

    private fun doEntryInDb() {
        FirebaseFirestore.getInstance()
            .collection("Gigs")
            .document("GigID000001")

    }

    @Test
    fun generateGigJson() {

        val instance = Calendar.getInstance()
        instance.set(Calendar.DAY_OF_MONTH,11)

        val loc  = GigLocationDetails(
            latitude = 28.633309,
            longitude = 77.2020263,
            fullAddress = "Tarang shopping complex, \n" +
                    "National Highway - 8, \n" +
                    "DLF Phase 3, Sector 24, \n" +
                    "Gurugram, \n" +
                    "Haryana - 122022( view on Map )\n" +
                    "\n"
        )

        val attendance = GigAttendance(
            checkInMarked = false,
            checkInLat = null,
            checkInLong = null,
            checkInImage = null,
            checkOutMarked = false,
            checkOutLat = null,
            checkOutLong = null,
            checkOutImage = null
        )

        val details = GigDetails(
            wage = "34 Per Hour",
            shiftDuration = "8 Hours",
            address = "arang shopping complex, National Highway - 8"
        )

        val gigContactDetails = GigContactDetails(
            contactName = "Name",
            mobileNo = "9798948823"
        )

        val gig = Gig(
            gigId = "GigID000001",
            gigerId = "wtQzruw02Lbs59KIUUTrZy7B3Wi1",
            title = "Retail Sales Executive",
            startDateTime = Timestamp(instance.time),
            duration = 3.1F,
            gigAmount = 10000.0,
            gigRating = 4.5F,
            companyName = "Procter & Gamble",
            contactNo = "9892849832",
            gigType = "On Site",
            gigHighLights = listOf(
                "Provide Advice On Purchase",
                "Communication with clients and customers about their experience with a products and services."
            ),
            gigRequirements = listOf(
                "Black Shirt",
                "Blue Jeans"
            ),
            gigLocationDetails = loc,
            gigDetails = details,
            attendance = attendance,
            gigContactDetails = gigContactDetails
        )




        print(Gson().toJson(gig))
    }

}