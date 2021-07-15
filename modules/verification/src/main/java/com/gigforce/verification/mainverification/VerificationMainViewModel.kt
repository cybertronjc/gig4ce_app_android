package com.gigforce.verification.mainverification

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.SimpleCardDVM
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.verification.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerificationMainViewModel @Inject constructor (private val iBuildConfigVM: IBuildConfigVM) : ViewModel() {
    var _allDocumentsData = MutableLiveData<List<SimpleCardDVM>>()
    var allDocumentsData : LiveData<List<SimpleCardDVM>> = _allDocumentsData

    var _allDocumentsVerified = MutableLiveData<Boolean>()
    var allDocumentsVerified : LiveData<Boolean> = _allDocumentsVerified

    val verificationKycRepo = VerificationKycRepo(iBuildConfigVM)

    init {
        getAllDocuments()
    }

    private fun getAllDocuments() {

        var allDocs = ArrayList<SimpleCardDVM>()
        allDocs.add(SimpleCardDVM("PAN Card","Tab to select", R.drawable.ic_badge_black_24dp,"verification/pancardimageupload", false))
        allDocs.add(SimpleCardDVM("Aadhaar Card","Tab to select",R.drawable.ic_account_box_black_24dp, "verification/aadhaarcardimageupload", false))
        allDocs.add(SimpleCardDVM("Driving licence","Tab to select",R.drawable.ic_directions_car_black_24dp, "verification/drivinglicenseimageupload", false))
        allDocs.add(SimpleCardDVM("Bank Details","Tab to select",R.drawable.ic_account_balance_black_24dp, "verification/bank_account_fragment", false))
        _allDocumentsData.value = allDocs

        verificationKycRepo.db.collection("Verification").document(verificationKycRepo.getUID()).addSnapshotListener { value, error ->
            value?.data?.let {
                val doc = value.toObject(VerificationBaseModel::class.java)
                var allDocs = ArrayList<SimpleCardDVM>()
                allDocs.add(SimpleCardDVM("PAN Card","Tab to select", R.drawable.ic_badge_black_24dp,"verification/pancardimageupload", doc?.pan_card?.verified))
                allDocs.add(SimpleCardDVM("Aadhaar Card","Tab to select",R.drawable.ic_account_box_black_24dp, "verification/aadhaarcardimageupload", doc?.aadhar_card?.verified))
                allDocs.add(SimpleCardDVM("Driving licence","Tab to select",R.drawable.ic_directions_car_black_24dp, "verification/drivinglicenseimageupload", doc?.driving_license?.verified))
                allDocs.add(SimpleCardDVM("Bank Details","Tab to select",R.drawable.ic_account_balance_black_24dp, "verification/bank_account_fragment", doc?.bank_details?.verified))
                _allDocumentsData.value = allDocs
                doc?.let {
                    var allVerified = true
                     it.bank_details?.verified?: run {
                         allVerified = false
                     }
                    it.pan_card?.verified?: run {
                        allVerified = false
                    }
                    it.aadhar_card?.verified?: run {
                        allVerified = false
                    }
                    it.driving_license?.verified?: run {
                        allVerified = false
                    }
                    Log.d("allverified", allVerified.toString())

                    if (allVerified) {          //oberve only when true
                        _allDocumentsVerified.value = true
                    }
                }

            }
        }


    }

}