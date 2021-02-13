package com.gigforce.client_activation.client_activation.models

import androidx.annotation.Keep
import com.gigforce.core.core.base.basefirestore.BaseFirestoreDataModel

@Keep
data class BankDetailsDataModel(
    val userHasPassBook : Boolean? = null,
    val passbookImagePath: String? = null,
    val verified: Boolean = false,
    val ifscCode: String? = null,
    val bankName: String? = null,
    val accountNo: String? = null,
    val state :Int = -1,
    val verifiedString : String? = null
) : BaseFirestoreDataModel(TABLE_NAME) {

    companion object {
        const val TABLE_NAME = "bank_details"
        const val KEY_USER_HAS_PASSBOOK = "userHasPassBook"
        const val KEY_NAME_PASSBOOK_IMAGE_PATH = "passbookImagePath"
        const val KEY_NAME_VERIFIED = "verified"
        const val KEY_NAME_IFSC = "ifscCode"
        const val KEY_NAME_ACCOUNT_NO = "accountNo"
    }
}