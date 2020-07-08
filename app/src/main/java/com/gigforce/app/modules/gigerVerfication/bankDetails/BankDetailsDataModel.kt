package com.gigforce.app.modules.gigerVerfication.bankDetails

import androidx.annotation.Keep
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel

@Keep
data class BankDetailsDataModel(
    val userHasPassBook : Boolean?,
    val passbookImagePath: String?,
    val verified: Boolean,
    val ifscCode: String?,
    val accountNo: String?
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