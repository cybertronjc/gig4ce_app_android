package com.gigforce.app.modules.client_activation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.client_activation.models.DrivingCertificate
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.modules.client_activation.models.PartnerSchoolDetails
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ConfirmationDialogDrivingTestViewModel : ViewModel() {
    val repository = ConfirmationDialogDrivingTestRepository()


    private val _observableJpApplication: MutableLiveData<Boolean> = MutableLiveData()
    val observableJpApplication: MutableLiveData<Boolean> = _observableJpApplication

    fun apply(
        mWorkOrderID: String,
        partnerDetails: PartnerSchoolDetails,
        date: String,
        slot: String,
        drivingLicenseCheck: Boolean, type: String, title: String
    ) = viewModelScope.launch {


        setInJPApplication(
            mWorkOrderID,
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
            .collection("submissions").whereEqualTo("stepId", workOrderID).whereEqualTo(
                "title", title
            ).whereEqualTo("type", type).get().await()


        if (submissions?.documents.isNullOrEmpty()) {
            repository.db.collection("JP_Applications")
                .document(items.documents[0].id).collection("submissions")
                .document().set(
                    mapOf(
                        "title" to title,
                        "type" to type,
                        "stepId" to workOrderID,
                        "certificate" to DrivingCertificate(
                            partnerSchoolDetails = partnerDetails,
                            selectedDate = date,
                            selectedTime = slot,
                            subDLChequeInSameCentre = drivingLicenseCheck,
                            slotBooked = true
                        )

                    )
                ).addOnCompleteListener { complete ->
                    run {

                        if (complete.isSuccessful) {
                            val jpApplication =
                                items.toObjects(JpApplication::class.java)[0]
                            jpApplication.process.forEach { draft ->
                                if (draft.title == title) {
                                    draft.isDone = true
                                }
                            }
                            repository.db.collection("JP_Applications")
                                .document(items.documents[0].id)
                                .update("process", jpApplication.process)
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
                .collection("submissions")
                .document(submissions?.documents?.get(0)?.id!!)
                .update(
                    "certificate", DrivingCertificate(
                        partnerSchoolDetails = partnerDetails,
                        selectedDate = date,
                        selectedTime = slot,
                        subDLChequeInSameCentre = drivingLicenseCheck,
                        slotBooked = true

                    )
                )
                .addOnCompleteListener { complete ->
                    if (complete.isSuccessful) {
                        val jpApplication =
                            items.toObjects(JpApplication::class.java)[0]
                        jpApplication.process.forEach { draft ->
                            if (draft.title == title) {
                                draft.isDone = true
                            }
                        }
                        repository.db.collection("JP_Applications")
                            .document(items.documents[0].id)
                            .update("process", jpApplication.process)
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
