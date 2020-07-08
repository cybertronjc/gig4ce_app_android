package com.gigforce.app.modules.gigerVerfication

import androidx.annotation.Keep
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel

@Keep
data class VerificationStatus3rdPartyStatus(
    val app_data_sync: Boolean
) : BaseFirestoreDataModel(TABLE_NAME){

    companion object {
        const val TABLE_NAME = "3rdparty_verification"
        const val KEY_NAME_APP_DATA_SYNC = "app_data_sync"
    }
}