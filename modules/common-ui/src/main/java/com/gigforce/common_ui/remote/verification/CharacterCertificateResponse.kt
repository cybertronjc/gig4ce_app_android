package com.gigforce.common_ui.remote.verification

import com.gigforce.core.datamodels.verification.CharacterCertificateDataModel

data class CharacterCertificateResponse(
    val status: Boolean = false,

    val message: String = "",

    val data: CharacterCertificateDataModel? = null
) {
}