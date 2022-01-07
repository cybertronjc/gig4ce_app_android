package com.gigforce.common_ui.dynamic_fields.data

object VerificationStatus {

    const val NOT_UPLOADED = ""
    const val UNDER_PROCESSING = "started"
    const val VERIFIED = "completed"
    const val REJECTED = "failed"
    const val UNKNOWN = "unknown"


    fun getStatusStringFromServerString(
        serverStatusString: String?
    ): String {

        return when (serverStatusString) {
            null, "" -> NOT_UPLOADED
            "started", "processing", "validated", "validation_failed", "verification_pending" -> UNDER_PROCESSING
            "verified", "completed" -> VERIFIED
            "rejected", "failed" -> REJECTED
            else -> UNKNOWN
        }
    }
}