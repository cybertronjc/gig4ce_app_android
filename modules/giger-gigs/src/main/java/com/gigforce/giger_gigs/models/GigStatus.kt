package com.gigforce.giger_gigs.models

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.gigforce.app.R
import com.gigforce.core.extensions.toLocalDateTime
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

    fun getStatusString(): String {
        return this.string
    }

    @DrawableRes
    fun getIconForStatus(): Int {
        return when (this) {
            GigStatus.UPCOMING -> R.drawable.ic_gig_upoming
            GigStatus.DECLINED -> R.drawable.ic_gig_declined_cancelled
            GigStatus.CANCELLED -> R.drawable.ic_gig_declined_cancelled
            GigStatus.ONGOING -> R.drawable.ic_gig_ongoing
            GigStatus.PENDING -> R.drawable.ic_gig_upoming
            GigStatus.NO_SHOW -> R.drawable.ic_gig_declined_cancelled
            GigStatus.COMPLETED -> R.drawable.ic_gig_ongoing
            GigStatus.MISSED -> R.drawable.ic_gig_missed
        }
    }

    @ColorRes
    fun getColorForStatus(): Int {
        return when (this) {
            GigStatus.UPCOMING -> R.color.gig_timer_upcoming_pink
            GigStatus.DECLINED -> R.color.gig_timer_declined_red
            GigStatus.CANCELLED -> R.color.gig_timer_declined_red
            GigStatus.ONGOING -> R.color.gig_timer_ongoing_green
            GigStatus.PENDING -> R.color.gig_timer_upcoming_pink
            GigStatus.NO_SHOW -> R.color.gig_timer_declined_red
            GigStatus.COMPLETED -> R.color.gig_timer_ongoing_green
            GigStatus.MISSED -> R.color.gig_timer_declined_red
        }
    }


    companion object {

        fun fromGig(gig: Gig): GigStatus {

            return if (gig.openNewGig()) {
                getGigStatus(gig)
            } else {
                getGigStatusLegacy(gig)
            }
        }

        private fun getGigStatusLegacy(gig: Gig): GigStatus {

            return if (gig.isPresentGig()) {
                if (gig.isCheckInMarked()) {
                    ONGOING
                } else {
                    PENDING
                }
            } else if (gig.isPastGig()) {
                if (gig.isCheckInOrCheckOutMarked()) {
                    COMPLETED
                } else {
                    MISSED
                }
            } else if (gig.isUpcomingGig()) {
                UPCOMING
            } else {
                throw IllegalArgumentException("GigStatus : Status Supplied doesn't match with any")
            }
        }

        private fun getGigStatus(gig: Gig): GigStatus {
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

            if (currentTime.isAfter(gig.checkInBeforeTime.toLocalDateTime()) &&
                    gig.isCheckInMarked()
            ) {
                return ONGOING
            }

            if (currentTime.isAfter(gig.checkInBeforeTime.toLocalDateTime()) &&
                    currentTime.isBefore(gig.checkInAfterTime.toLocalDateTime()) &&
                    gig.isCheckInMarked().not()
            ) {
                return PENDING
            }

            if (currentTime.isAfter(gig.checkInAfterTime.toLocalDateTime()) &&
                    currentTime.isBefore(gig.endDateTime.toLocalDateTime()) &&
                    gig.isCheckInMarked().not()
            ) {
                return NO_SHOW
            }

            throw IllegalArgumentException("GigStatus : Status Supplied doesn't match with any")
        }
    }
}