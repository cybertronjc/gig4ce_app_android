package com.gigforce.app.domain.models.tl_workspace

enum class TLWorkspaceHomeSection constructor(
    private val sectionId: String
) {
    ACTIVITY_TRACKER("activity_tracker"),
    UPCOMING_GIGERS("upcoming_gigers"),
    PAYOUT("payout"),
    COMPLIANCE_PENDING("compliance_pending"),
    RETENTION("retention"),
    SELECTIONS("selections");

    fun getSectionId(): String {
        return sectionId
    }


    companion object {

        fun fromId(
            sectionId: String
        ): TLWorkspaceHomeSection {
            values().forEach {
                if (it.sectionId == sectionId.trim().lowercase())
                    return it
            }

            throw IllegalArgumentException("$sectionId matched no TLWorkspaceHomeSection")
        }
    }
}