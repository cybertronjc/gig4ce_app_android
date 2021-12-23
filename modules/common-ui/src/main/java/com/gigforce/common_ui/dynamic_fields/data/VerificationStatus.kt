package com.gigforce.common_ui.dynamic_fields.data

object VerificationStatus {

    const val NOT_UPLOADED = ""
    const val UNDER_PROCESSING = "started"
    const val VERIFIED = "completed"
    const val REJECTED = "failed"


    fun getStatusStringFromServerString(
        serverStatusString: String?
    ): String {

        return when (serverStatusString) {
            "started", "processing", "validated", "validation_failed", "verification_pending" -> UNDER_PROCESSING
            "verified", "completed" -> VERIFIED
            "rejected", "failed" -> REJECTED
            else -> NOT_UPLOADED
        }
    }
}