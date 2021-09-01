package com.gigforce.common_ui.viewdatamodels

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.gigforce.common_ui.R
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.extensions.toLocalDateTime
//import com.gigforce.giger_gigs.R
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

    @StringRes
    fun getStatusStringRes() : Int = when(this.string){
        "upcoming" -> R.string.upcoming_common_ui
        "declined" -> R.string.declined_common_ui
        "cancelled" ->R.string.cancelled_common_ui
        "ongoing" -> R.string.ongoing_common_ui
        "pending" ->R.string.pending_common_ui
        "noshow" ->R.string.noshow_common_ui
        "completed" ->R.string.completed_gig_common_ui
        "missed" -> R.string.missed_common_ui
        else -> R.string.status_common_ui
    }

    fun getStatusString(): String {
        return this.string
    }

    @DrawableRes
    fun getIconForStatus(): Int {
        return when (this) {
            UPCOMING -> R.drawable.ic_gig_upoming
            DECLINED -> R.drawable.ic_gig_declined_cancelled
            CANCELLED -> R.drawable.ic_gig_declined_cancelled
            ONGOING -> R.drawable.ic_gig_ongoing
            PENDING -> R.drawable.ic_gig_upoming
            NO_SHOW -> R.drawable.ic_gig_declined_cancelled
            COMPLETED -> R.drawable.ic_gig_ongoing
            MISSED -> R.drawable.ic_gig_missed
        }
    }

    @ColorRes
    fun getColorForStatus(): Int {
        return when (this) {
            UPCOMING -> R.color.gig_timer_upcoming_pink
            DECLINED -> R.color.gig_timer_declined_red
            CANCELLED -> R.color.gig_timer_declined_red
            ONGOING -> R.color.gig_timer_ongoing_green
            PENDING -> R.color.gig_timer_upcoming_pink
            NO_SHOW -> R.color.gig_timer_declined_red
            COMPLETED -> R.color.gig_timer_ongoing_green
            MISSED -> R.color.gig_timer_declined_red
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