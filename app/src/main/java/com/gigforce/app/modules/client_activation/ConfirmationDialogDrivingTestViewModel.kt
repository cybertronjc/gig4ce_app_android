package com.gigforce.app.modules.client_activation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.modules.client_activation.models.PartnerSchoolDetails
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ConfirmationDialogDrivingTestViewModel : ViewModel() {
    val repository = ConfirmationDialogDrivingTestRepository()


    private val _observableJpApplication: MutableLiveData<Boolean> = MutableLiveData()
    val observableJpApplication: MutableLiveData<Boolean> = _observableJpApplication

    fun apply(mWorkOrderID: String, partnerDetails: PartnerSchoolDetails, date: String, slot: String, drivingLicenseCheck: Boolean) = viewModelScope.launch {


        val model = getJPApplication(mWorkOrderID)
        model.partnerSchoolDetails = partnerDetails
        model.slotBooked = true
        model.selectedDate = date
        model.selectedTime = slot
        model.subDLChequeInSameCentre = drivingLicenseCheck;

        if (model.id.isEmpty()) {
            repository.db.collection("JP_Applications").document().set(model).addOnCompleteListener {
                if (it.isSuccessful) {
                    observableJpApplication.value = true
                }
            }
        } else {
            repository.db.collection("JP_Applications").document(model.id).set(model).addOnCompleteListener {
                if (it.isSuccessful) {
                    observableJpApplication.value = true
                }
            }
        }


    }

    suspend fun getJPApplication(workOrderID: String): JpApplication {
        val items = repository.db.collection("JP_Applications").whereEqualTo("jpid", workOrderID).whereEqualTo("gigerId", repository.getUID()).get()
                .await()
        if (items.documents.isNullOrEmpty()) {
            return JpApplication(JPId = workOrderID, gigerId = repository.getUID())
        }
        val toObject = items.toObjects(JpApplication::class.java).get(0)
        toObject.id = items.documents[0].id
        return toObject
    }

}