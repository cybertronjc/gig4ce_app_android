package com.gigforce.common_ui.viewdatamodels.verification.signature

data class SignatureUploadResponse(
    val signatureFirebasePath : String,
    val signatureFullUrl : String,
    val backgroundRemoved : Boolean
)
