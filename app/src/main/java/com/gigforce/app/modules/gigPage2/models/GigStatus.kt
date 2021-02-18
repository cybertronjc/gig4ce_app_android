package com.gigforce.app.modules.gigPage2.models

import androidx.annotation.DrawableRes
import com.gigforce.app.R
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
    NO_SHOW("no_show"),
    COMPLETED("completed"),
    MISSED("missed");


    fun getStatusCapitalized(): String {
        return this.string.capitalize()
    }

    @DrawableRes
    fun getIconForStatus(): Int{
        return when (this) {
            GigStatus.UPCOMING -> R.drawable.ic_status_pending
            GigStatus.DECLINED -> R.drawable.ic_status_pending
            GigStatus.CANCELLED -> R.drawable.ic_status_pending
            GigStatus.ONGOING -> R.drawable.ic_status_pending
            GigStatus.PENDING -> R.drawable.ic_status_pending
            GigStatus.NO_SHOW -> R.drawable.ic_status_pending
            GigStatus.COMPLETED -> R.drawable.ic_status_pending
            GigStatus.MISSED -> R.drawable.ic_status_pending
        }
    }


    companion object {

        fun fromGig(gig: Gig): GigStatus {

            if (gig.gigStatus.isBlank())
                throw IllegalArgumentException("GigStatus : Status cannot be empty")

            if (gig.gigStatus == CANCELLED.string) {
                return CANCELLED
            }

            if (gig.gigStatus == DECLINED.string) {
                return DECLINED
            }

            val currentTime = LocalDateTime.now()

            if (currentTime.isBefore(gig.checkInBeforeTime.toLocalDateTime())) {
                return UPCOMING
            }

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

            if (currentTime.isAfter(gig.checkInBeforeTime.toLocalDateTime()) &&
                    currentTime.isBefore(gig.checkInAfterTime.toLocalDateTime()) &&
                    gig.isCheckInMarked().not()
            ) {
                return PENDING
            }

            if (currentTime.isBefore(gig.checkInAfterTime.toLocalDateTime()) &&
                    currentTime.isBefore(gig.endDateTime.toLocalDateTime()) &&
                    gig.isCheckInMarked().not()
            ) {
                return NO_SHOW
            }



            throw IllegalArgumentException("GigStatus : Status Supplied doesn't match with any")
        }
    }
}