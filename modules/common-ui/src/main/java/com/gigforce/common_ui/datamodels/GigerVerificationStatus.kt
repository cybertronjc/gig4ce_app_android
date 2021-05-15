package com.gigforce.common_ui.datamodels

import com.gigforce.common_ui.R
import com.gigforce.core.datamodels.verification.*

data class GigerVerificationStatus(
    val selfieVideoUploaded: Boolean = false,
    val selfieVideoDataModel: SelfieVideoDataModel? = null,
    val panCardDetailsUploaded: Boolean = false,
    val panCardDetails: PanCardDataModel? = null,
    val aadharCardDetailsUploaded: Boolean = false,
    val aadharCardDataModel: AadharCardDataModel? = null,
    val dlCardDetailsUploaded: Boolean = false,
    val drivingLicenseDataModel: DrivingLicenseDataModel? = null,
    val bankDetailsUploaded: Boolean = false,
    val bankUploadDetailsDataModel: BankDetailsDataModel? = null,
    val everyDocumentUploaded: Boolean = false


) {
    fun getColorCodeForStatus(statusCode: Int): Int {
        return when (statusCode) {
            STATUS_DOCUMENT_RECEIVED_BY_3RD_PARTY, STATUS_DOCUMENT_UPLOADED, STATUS_DOCUMENT_PROCESSING -> R.color.yellow
            STATUS_VERIFIED -> R.color.green
            STATUS_VERIFICATION_FAILED -> R.color.app_red
            else -> R.color.yellow
        }
    }

    companion object {
        const val STATUS_VERIFIED = 2
        const val STATUS_VERIFICATION_FAILED = 3
        const val STATUS_DOCUMENT_RECEIVED_BY_3RD_PARTY = 0
        const val STATUS_DOCUMENT_PROCESSING = 1
        const val STATUS_DOCUMENT_UPLOADED = -1
    }
}