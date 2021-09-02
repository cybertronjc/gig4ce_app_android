package com.gigforce.common_ui.viewdatamodels.leadManagement

import android.content.Context
import androidx.annotation.StringRes
import com.gigforce.common_ui.R

enum class JoiningStatus constructor(
    private val string: String
) {
    SIGN_UP_PENDING("sign_up_pending"),
    APPLICATION_PENDING("application_pending"),
    JOINING_PENDING("joining_pending"),
    JOINED("joined");

    fun getStatusString(): String {
        return this.string
    }

    fun getStatusFormattedString(): String {
        return when (this) {
            SIGN_UP_PENDING -> "Signup Pending"
            APPLICATION_PENDING -> "Application Pending"
            JOINING_PENDING -> "Joining Pending"
            JOINED -> "Joined"
        }
    }


    @StringRes
    fun getStatusFormattedStringRes(): Int {
        return when (this) {
            SIGN_UP_PENDING -> R.string.signup_pending
            APPLICATION_PENDING -> R.string.application_pending
            JOINING_PENDING -> R.string.joining_pending
            JOINED -> R.string.joined
        }
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

    @StringRes
    fun getOverallStatusStringRes(): Int {
        return if (this == JOINED)
            R.string.joined
        else
            R.string.joining_pending
    }

    companion object {

        fun fromValue(status: String): JoiningStatus {
            for (value in values()) {
                if (value.getStatusString() == status) {
                    return value
                }
            }

            throw IllegalStateException("joining status doesnot match : $status")
        }
    }
}
