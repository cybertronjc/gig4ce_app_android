package com.gigforce.verification.mainverification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.viewdatamodels.SimpleCardDVM
import com.gigforce.verification.R

class VerificationMainViewModel : ViewModel() {
    var _allDocumentsData = MutableLiveData<List<SimpleCardDVM>>()
    var allDocumentsData : LiveData<List<SimpleCardDVM>> = _allDocumentsData

    init {
        getAllDocuments()
    }

    private fun getAllDocuments() {
        var allDocs = ArrayList<SimpleCardDVM>()
        allDocs.add(SimpleCardDVM("PAN Card","Takes about 45 seconds", R.drawable.ic_badge_black_24dp,"verification/pancardimageupload" ))
        allDocs.add(SimpleCardDVM("Aadhaar Card","Takes about 45 seconds",R.drawable.ic_account_box_black_24dp, "verification/aadhaarcardimageupload"))
        allDocs.add(SimpleCardDVM("Driving licence","Takes about 45 seconds",R.drawable.ic_directions_car_black_24dp, "verification/drivinglicenseimageupload"))
        allDocs.add(SimpleCardDVM("Bank Details","Takes about 45 seconds",R.drawable.ic_account_balance_black_24dp, "verification/bank_account_fragment"))
        _allDocumentsData.value = allDocs
    }
}