package com.gigforce.app.modules.gigerVerfication

import androidx.annotation.Keep
import com.gigforce.app.modules.gigerVerfication.panCard.PanCardDataModel

@Keep
data class DefaultEntryModel(
    val pan_card: PanCardDataModel = PanCardDataModel(
        userHasPanCard = false,
        panCardImagePath = null,
        verified = false,
        panCardNo = null
    )
)
