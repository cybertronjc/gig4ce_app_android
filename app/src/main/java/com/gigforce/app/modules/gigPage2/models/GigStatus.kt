package com.gigforce.app.modules.gigPage2.models

import com.gigforce.app.core.toLocalDateTime
import com.gigforce.app.modules.gigPage.models.Gig
import java.time.LocalDateTime

enum class GigStatus constructor(
        private val string: String
) {
    UPCOMING("upcoming"),
    DECLINED("declined"),
    CANCELLED("cancelled"),
    ONGOING("ongoing"),
    PENDING("pending"),
    NO_SHOW("noshow"),
    COMPLETED("completed"),
    MISSED("missed");


    fun getStatusCapitalized(): String {
        return this.string.capitalize()
    }

    fun getStatus(gig: Gig): GigStatus {

        if (gig.gigStatus.isBlank())
            throw IllegalArgumentException("GigStatus : Status cannot be empty")

        if (gig.gigStatus == CANCELLED.string) {
            return CANCELLED
        }

        if (gig.gigStatus == DECLINED.string) {
            return DECLINED
        }

        val currentTime = LocalDateTime.now()
        if (currentTime.isAfter(gig.endDateTime.toLocalDateTime())
                && !gig.isCheckInMarked()
        ) {
            return MISSED
        }

        if (gig.isCheckInAndCheckOutMarked()) {
            return COMPLETED //todo discuss case where user had halfday
        }

        if (gig.isCheckInMarked() &&
                currentTime.isBefore(gig.checkOutAfterTime.toLocalDateTime())) {
            return ONGOING
        }

        if (currentTime.isBefore(gig.endDateTime.toLocalDateTime())) {
            return NO_SHOW
        }

        if (currentTime.isBefore(gig.checkInBeforeTime.toLocalDateTime())) {
            return UPCOMING
        }

        throw IllegalArgumentException("GigStatus : Status Supplied doesn't match with any")
    }
}