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
    val TAP_TO_SELECT = "Tap to select"
    init {
        getAllDocuments()
    }

    private fun getAllDocuments() {

        var allDocs = ArrayList<SimpleCardDVM>()
        allDocs.add(SimpleCardDVM("PAN Card","Tap to select", R.drawable.ic_badge_black_24dp,"verification/pancardimageupload",false))
        allDocs.add(SimpleCardDVM("Driving licence","Tap to select",R.drawable.ic_directions_car_black_24dp, "verification/drivinglicenseimageupload", false))
        allDocs.add(SimpleCardDVM("Bank Details","Tap to select",R.drawable.ic_account_balance_black_24dp, "verification/bank_account_fragment", false))
        allDocs.add(SimpleCardDVM("Aadhaar Card","Tap to select",R.drawable.ic_account_box_black_24dp, "verification/aadhaarcardimageupload", false))
        _allDocumentsData.value = allDocs

        verificationKycRepo.db.collection("Verification").document(verificationKycRepo.getUID()).addSnapshotListener { value, error ->
            value?.data?.let {
                val doc = value.toObject(VerificationBaseModel::class.java)
                var allDocs = ArrayList<SimpleCardDVM>()
                allDocs.add(SimpleCardDVM(title = "PAN Card",subtitle = getSubString(doc?.pan_card?.verified,doc?.pan_card?.status), image = R.drawable.ic_badge_black_24dp,navpath = "verification/pancardimageupload", color = getSubStringColor(doc?.pan_card?.verified,doc?.pan_card?.status)))
                allDocs.add(SimpleCardDVM(title = "Driving licence",subtitle = getSubString(doc?.driving_license?.verified,doc?.driving_license?.status),image = R.drawable.ic_directions_car_black_24dp, navpath = "verification/drivinglicenseimageupload", color = getSubStringColor(doc?.driving_license?.verified,doc?.driving_license?.status)))
                allDocs.add(SimpleCardDVM(title = "Bank Details",subtitle = getSubString(doc?.bank_details?.verified,doc?.bank_details?.status),image = R.drawable.ic_account_balance_black_24dp, navpath = "verification/bank_account_fragment", color = getSubStringColor(doc?.bank_details?.verified,doc?.bank_details?.status)))
                allDocs.add(SimpleCardDVM(title = "Aadhaar Card",subtitle = getSubString(doc?.aadhar_card?.verified,""),image = R.drawable.ic_account_box_black_24dp, navpath = "verification/aadhaarcardimageupload", color = getSubStringColor(doc?.aadhar_card?.verified,"")))
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

    fun getSubString(isVerified : Boolean ? =false, status : String? = "") : String{
            if(isVerified == true)return "Verified"
            if(status?.equals("started") == true) return "Pending"
            if(status?.equals("failed") == true) return "Failed"
            return TAP_TO_SELECT
    }

    fun getSubStringColor(isVerified : Boolean ? =false, status : String? = "") : String{
        if(isVerified == true)return "GREEN"
        if(status?.equals("started") == true) return "YELLOW"
        if(status?.equals("failed") == true) return "RED"
        return ""
    }

}