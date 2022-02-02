package com.gigforce.verification.mainverification

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.navigation.signature.SignatureNavigation
import com.gigforce.common_ui.viewdatamodels.SimpleCardDVM
import com.gigforce.core.datamodels.verification.AadhaarDetailsDataModel
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.verification.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


object VACCINESTATUSES{
    const val UPLOADED = "uploaded"
    const val UPLOADED_FAILED = "uploading_failed"
    const val VALIDATED = "validated"
    const val VALIDATION_FAILED = "validation_failed"
    const val VERIFIED = "verified"
    const val REJECTED = "rejected"

}

@HiltViewModel
class VerificationMainViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
//    private val iBuildConfigVM: IBuildConfigVM,
    private val verificationKycRepo : VerificationKycRepo
) : ViewModel() {

    var _allDocumentsData = MutableLiveData<List<SimpleCardDVM>>()
    var allDocumentsData: LiveData<List<SimpleCardDVM>> = _allDocumentsData

    var _allDocumentsVerified = MutableLiveData<Boolean>()
    var allDocumentsVerified: LiveData<Boolean> = _allDocumentsVerified

//    val verificationKycRepo = VerificationKycRepo(iBuildConfigVM)
    val TAP_TO_SELECT = "Tap to select"

    init {
        getAllDocuments()
    }

    var latestVerificationDoc: VerificationBaseModel? = null

    private fun getAllDocuments() {

        var allDocs = ArrayList<SimpleCardDVM>()
        allDocs.add(
            SimpleCardDVM(
                appContext.getString(R.string.pan_card),
                appContext.getString(R.string.pending_status_veri),
                R.drawable.ic_badge_black_24dp,
                "verification/pancardimageupload",
                false
            )
        )
        allDocs.add(
            SimpleCardDVM(
                appContext.getString(R.string.driving_license),
                appContext.getString(R.string.pending_status_veri),
                R.drawable.ic_directions_car_black_24dp,
                "verification/drivinglicenseimageupload",
                false
            )
        )
        allDocs.add(
            SimpleCardDVM(
                appContext.getString(R.string.bank_details),
                appContext.getString(R.string.pending_status_veri),
                R.drawable.ic_account_balance_black_24dp,
                "verification/bank_account_fragment",
                false
            )
        )
//        allDocs.add(SimpleCardDVM(appContext.getString(R.string.aadhar_card), appContext.getString(R.string.tap_to_select), R.drawable.ic_account_box_black_24dp, "verification/aadhaarcardimageupload", false))
        allDocs.add(
            SimpleCardDVM(
                appContext.getString(R.string.aadhar_card_detail_veri),
                appContext.getString(R.string.pending_status_veri),
                R.drawable.ic_account_box_black_24dp,
                "verification/AadharDetailInfoFragment",
                false
            )
        )

        allDocs.add(
            SimpleCardDVM(
                "Signature",
                appContext.getString(R.string.pending_status_veri),
                R.drawable.ic_account_box_black_24dp,
                SignatureNavigation.DESTINATION_CAPTURE_SIGNATURE,
                false
            )
        )

        allDocs.add(
            SimpleCardDVM(
                title = appContext.getString(R.string.covid_vaccination_certificate_veri),
                appContext.getString(R.string.pending_status_veri),
                R.drawable.ic_account_box_black_24dp,
                "verification/VaccineMainFragment",
                false
            )
        )

        _allDocumentsData.value = allDocs

        verificationKycRepo.db.collection("Verification").document(verificationKycRepo.getUID())
            .addSnapshotListener { value, error ->
                value?.data?.let {
                    val doc = value.toObject(VerificationBaseModel::class.java)
                    latestVerificationDoc = doc
                    var allDocs = ArrayList<SimpleCardDVM>()
                    allDocs.add(
                        SimpleCardDVM(
                            title = appContext.getString(R.string.pan_card),
                            subtitle = getSubString(doc?.pan_card?.verified, doc?.pan_card?.status),
                            image = R.drawable.ic_badge_black_24dp,
                            navpath = "verification/pancardimageupload",
                            color = getSubStringColor(doc?.pan_card?.verified,
                                doc?.pan_card?.status
                            )
                        )
                    )
                    allDocs.add(
                        SimpleCardDVM(
                            title = appContext.getString(R.string.driving_license),
                            subtitle = getSubString( doc?.driving_license?.verified,
                                doc?.driving_license?.status
                            ),
                            image = R.drawable.ic_directions_car_black_24dp,
                            navpath = "verification/drivinglicenseimageupload",
                            color = getSubStringColor(doc?.driving_license?.verified,
                                doc?.driving_license?.status
                            )
                        )
                    )
                    allDocs.add(
                        SimpleCardDVM(
                            title = appContext.getString(R.string.bank_details),
                            subtitle = getSubString(null,
                                doc?.bank_details?.status
                            ),
                            image = R.drawable.ic_account_balance_black_24dp,
                            navpath = "verification/bank_account_fragment",
                            color = getSubStringColor(doc?.bank_details?.verified,
                                doc?.bank_details?.status
                            )
                        )
                    )
//                allDocs.add(SimpleCardDVM(title = appContext.getString(R.string.aadhar_card), subtitle = getSubString(doc?.aadhar_card?.verified, ""), image = R.drawable.ic_account_box_black_24dp, navpath = "verification/aadhaarcardimageupload", color = getSubStringColor(doc?.aadhar_card?.verified, "")))
                    allDocs.add(
                        SimpleCardDVM(
                            title = appContext.getString(R.string.aadhar_card_detail_veri),
                            subtitle = if (doc?.aadhaar_card_questionnaire?.verified == true) appContext.getString(
                                R.string.submitted_status_veri
                            ) else appContext.getString(R.string.pending_status_veri),
                            image = R.drawable.ic_account_box_black_24dp,
                            navpath = "verification/AadharDetailInfoFragment",
                            color = if (doc?.aadhaar_card_questionnaire?.verified == true) "GREEN" else "RED"
                        )
                    )

                    allDocs.add(

                        SimpleCardDVM(
                            title ="Signature",
                            subtitle = if (doc?.signature?.signaturePathOnFirebase != null) appContext.getString(
                                R.string.submitted_status_veri
                            ) else appContext.getString(R.string.tap_to_select),
                            image = R.drawable.ic_account_box_black_24dp,
                            navpath= SignatureNavigation.DESTINATION_CAPTURE_SIGNATURE,
                            color = if (doc?.signature?.signaturePathOnFirebase != null) "GREEN" else ""
                        )
                    )


//                    val vaccineNavPath = doc?.vaccination?.let { "verification/CovidCertificateStatusFragment"  }?:"verification/AskUserForVaccineBS"
                    allDocs.add(
                        SimpleCardDVM(
                            title = appContext.getString(R.string.covid_vaccination_certificate_veri),
                            subtitle = getSubString(null,
                                doc?.vaccination?.status
                            ),
                            image = R.drawable.ic_account_box_black_24dp,
                            navpath = "verification/VaccineMainFragment",
                            color = getSubStringColor(null,
                                doc?.vaccination?.status
                            )
                        )
                    )

                    _allDocumentsData.value = allDocs
                    doc?.let {
                        var allVerified = true
                        it.bank_details?.verified ?: run {
                            allVerified = false
                        }
                        it.pan_card?.verified ?: run {
                            allVerified = false
                        }
                        it.aadhar_card?.verified ?: run {
                            allVerified = false
                        }
                        it.driving_license?.verified ?: run {
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

    private fun isSubmitted(aadhaarCardQuestionnaire: AadhaarDetailsDataModel?): Boolean {
        return !aadhaarCardQuestionnaire?.dateOfBirth.isNullOrEmpty() && !aadhaarCardQuestionnaire?.name.isNullOrEmpty() && !aadhaarCardQuestionnaire?.aadhaarCardNo.isNullOrEmpty()
    }

    fun getSubString(isVerified: Boolean? = false, status: String? = ""): String {
//        vaccination status
//        started
//        processing
//        verification_pending/ validation_failed
//        verified/failed

        if (status.equals("verified")) return appContext.getString(R.string.verified_status_veri)//"Verified"
        if (status.equals("verification_pending")) return appContext.getString(R.string.confirmation_pending_veri)//"confirmation pending"
        if (status.equals("started") || status.equals("processing") || status.equals("validated")) return appContext.getString(R.string.inprogress_veri)//"in progress"
        if (status.equals("validation_failed")) return appContext.getString(R.string.failed_status_veri)//"Failed"

        if (isVerified == true) return appContext.getString(R.string.verified_status_veri)//"Verified"
        if (status?.equals("started") == true) return appContext.getString(R.string.inprogress_veri)//"in progress"
        if (status?.equals("failed") == true) return appContext.getString(R.string.failed_status_veri)//"Failed"
        return appContext.getString(R.string.pending_status_veri)
    }

    fun getSubStringColor(isVerified: Boolean? = false, status: String? = ""): String {
        // for bank account
        if (status.equals("verified")) return "GREEN"
        if (status.equals("started") || status.equals("processing") || status.equals("validated") || status.equals("verification_pending")) return "YELLOW"
        if (status.equals("validation_failed")) return "RED"

        //for other
        if (isVerified == true) return "GREEN"
        if (status?.equals("started") == true) return "YELLOW"
        if (status?.equals("failed") == true) return "RED"
        return "RED"
    }

    fun isAllDocVerified(): Boolean {
        return latestVerificationDoc?.bank_details?.verified == true && latestVerificationDoc?.pan_card?.verified == true && latestVerificationDoc?.aadhar_card?.verified == true && latestVerificationDoc?.driving_license?.verified == true
    }

}