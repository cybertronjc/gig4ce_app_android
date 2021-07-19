package com.gigforce.common_ui.viewdatamodels.leadManagement

enum class JoiningStatus constructor(
    private val string: String
) {
    SIGN_UP_PENDING("sign_up_pending"),
    APPLICATION_PENDING("application_pending"),
    JOINING_PENDING("joining_pending"),
    JOINED("joined");

    fun getStatusCapitalized(): String {
        return this.string.capitalize()
    }

    fun getStatusString(): String {
        return this.string
    }

    /**
     * returns overall status
     * - Joined
     * - Joining Pending
     */
    fun getOverallStatusString(): String {
        return if (this == JOINED)
            "Joined"
        else
            "Joining Pending"
    }
}
