package com.gigforce.verification.mainverification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.viewdatamodels.SimpleCardDVM

class VerificationMainViewModel : ViewModel() {
    var _allDocumentsData = MutableLiveData<List<SimpleCardDVM>>()
    var allDocumentsData : LiveData<List<SimpleCardDVM>> = _allDocumentsData

    init {
        getAllDocuments()
    }

    private fun getAllDocuments() {
        var allDocs = ArrayList<SimpleCardDVM>()
        allDocs.add(SimpleCardDVM("PAN Card","Takes about 45 seconds",""))
        allDocs.add(SimpleCardDVM("Aadhaar Card","Takes about 45 seconds",""))
        allDocs.add(SimpleCardDVM("Driving licence","Takes about 45 seconds",""))
        allDocs.add(SimpleCardDVM("Bank Details","Takes about 45 seconds",""))
        _allDocumentsData.value = allDocs
    }
}