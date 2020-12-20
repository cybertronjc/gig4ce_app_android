package com.gigforce.app.modules.client_activation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.client_activation.models.DrivingCertificate
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.modules.client_activation.models.PartnerSchoolDetails
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class ConfirmationDialogDrivingTestViewModel : ViewModel() {
    val repository = ConfirmationDialogDrivingTestRepository()


    private val _observableJpApplication: MutableLiveData<Boolean> = MutableLiveData()
    val observableJpApplication: MutableLiveData<Boolean> = _observableJpApplication

    fun apply(
            mJobProfileId: String,
            partnerDetails: PartnerSchoolDetails,
            date: String,
            slot: String,
            drivingLicenseCheck: Boolean, type: String, title: String
    ) = viewModelScope.launch {


        setInJPApplication(
                mJobProfileId,
                type,
                title,
                partnerDetails,
                date,
                slot,
                drivingLicenseCheck
        )

    }


    suspend fun setInJPApplication(
            workOrderID: String,
            type: String,
            title: String,
            partnerDetails: PartnerSchoolDetails,
            date: String,
            slot: String,
            drivingLicenseCheck: Boolean
    ) {
        val items = repository.getCollectionReference().whereEqualTo("jpid", workOrderID)
                .whereEqualTo("gigerId", repository.getUID()).get()
                .await()
        val submissions = repository.getCollectionReference().document(items.documents[0].id)
                .collection("Submissions").whereEqualTo("stepId", workOrderID).whereEqualTo(
                        "title", title
                ).whereEqualTo("type", type).get().await()


        if (submissions?.documents.isNullOrEmpty()) {
            repository.db.collection("JP_Applications")
                    .document(items.documents[0].id).collection("Submissions")
                    .document().set(
                            mapOf(
                                    "title" to title,
                                    "type" to type,
                                    "stepId" to workOrderID,
                                    "insertedOn" to Date(),
                                    "certificate" to DrivingCertificate(
                                            partnerSchoolDetails = partnerDetails,
                                            slotDate = date,
                                            slotTime = slot,
                                            subDLChequeInSameCentre = drivingLicenseCheck,
                                            slotBooked = true

                                    )

                            )
                    ).addOnCompleteListener { complete ->
                        run {

                            if (complete.isSuccessful) {
                                val jpApplication =
                                        items.toObjects(JpApplication::class.java)[0]
                                jpApplication.activation.forEach { draft ->
                                    if (draft.title == title || draft.type == "onsite_document") {
                                        draft.isDone = false
                                        draft.isSlotBooked = true
                                        draft.status = "Slot Booked"
                                    }
                                }
                                repository.db.collection("JP_Applications")
                                        .document(items.documents[0].id)
                                        .update("activation", jpApplication.activation)
                                        .addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                observableJpApplication.value = true

                                            }
                                        }
                            }
                        }
                    }
        } else {
            repository.db.collection("JP_Applications")
                    .document(items?.documents!![0].id)
                    .collection("Submissions")
                    .document(submissions?.documents?.get(0)?.id!!)
                    .update(
                            "certificate", DrivingCertificate(
                            partnerSchoolDetails = partnerDetails,
                            slotDate = date,
                            slotTime = slot,
                            subDLChequeInSameCentre = drivingLicenseCheck,
                            slotBooked = true

                    )
                    )
                    .addOnCompleteListener { complete ->
                        if (complete.isSuccessful) {
                            val jpApplication =
                                    items.toObjects(JpApplication::class.java)[0]
                            jpApplication.activation.forEach { draft ->
                                if (draft.title == title) {
                                    draft.isDone = false
                                    draft.isSlotBooked = true
                                    draft.status = "Slot Booked"

                                }
                            }
                            repository.db.collection("JP_Applications")
                                    .document(items.documents[0].id)
                                    .update("activation", jpApplication.activation)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            observableJpApplication.value = true
                                        }
                                    }
                        }
                    }
        }


    }
}
