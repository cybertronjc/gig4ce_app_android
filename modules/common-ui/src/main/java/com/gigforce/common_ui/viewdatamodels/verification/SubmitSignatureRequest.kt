package com.gigforce.common_ui.viewdatamodels.verification

import com.google.gson.annotations.SerializedName

data class SubmitSignatureRequest(

    @SerializedName("signatureFirebasePath")
    val signatureFirebasePath : String,

    @SerializedName("signatureImageFullUrl")
    val signatureImageFullUrl : String,

    @SerializedName("backgroundRemoved")
    val backgroundRemoved : Boolean = false
)
