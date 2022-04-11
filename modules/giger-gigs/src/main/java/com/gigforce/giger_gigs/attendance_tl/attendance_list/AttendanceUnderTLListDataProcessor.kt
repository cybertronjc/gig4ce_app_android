package com.gigforce.giger_gigs.attendance_tl.attendance_list

import com.gigforce.common_ui.datamodels.attendance.GigAttendanceApiModel
import com.gigforce.common_ui.viewdatamodels.gig.AttendanceStatus
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import com.gigforce.giger_gigs.models.AttendanceStatusAndCountItemData

object AttendanceUnderTLListDataProcessor {


    fun processAttendanceListAndFilters(
        attendance: List<GigAttendanceApiModel>,
        collapsedBusiness: List<String>,
        currentMarkingAttendanceForGigs: MutableSet<String>,
        currentlySelectedStatus: String,
        currentlySearchTerm: String?,
        prepareAttendanceStatusAndCount: Boolean,
        gigerAttendanceUnderManagerViewModel: GigerAttendanceUnderManagerViewModel
    ): Pair<List<AttendanceRecyclerItemData>, List<AttendanceStatusAndCountItemData>?> {


        val attendanceFilteredBySearchTerm = filterAttendanceBySearchTerm(
            attendance,
            currentlySearchTerm
        )

        val tabStatusWithCount = if (prepareAttendanceStatusAndCount) {
            prepareAttendanceTabsCountList(
                attendanceFilteredBySearchTerm,
                currentlySelectedStatus
            )
        } else {
            null
        }

        val businessToAttendanceGroup = filterAttendanceByStatus(
            attendanceFilteredBySearchTerm,
            currentlySelectedStatus,
        ).sortedByDescending {
            it.getBusinessNameNN()
        }.groupBy {
            it.getBusinessNameNN()
        }

        val attendanceListForView = prepareAttendanceListForView(
            businessToAttendanceGroup,
            gigerAttendanceUnderManagerViewModel,
            collapsedBusiness,
            currentMarkingAttendanceForGigs
        )
        return attendanceListForView to tabStatusWithCount
    }

    private fun prepareAttendanceListForView(
        businessToAttendanceGroup: Map<String, List<GigAttendanceApiModel>>,
        gigerAttendanceUnderManagerViewModel: GigerAttendanceUnderManagerViewModel,
        collapsedBusiness: List<String>,
        currentMarkingAttendanceForGigs: MutableSet<String> //Gigs for which currently attendance mark process is going on
    ) = mutableListOf<AttendanceRecyclerItemData>()
        .apply {

            businessToAttendanceGroup.forEach { (businessName, attendance) ->
                val attendanceHeader =
                    AttendanceRecyclerItemData.AttendanceBusinessHeaderItemData(
                        businessName = businessName,
                        enabledCount = attendance.count(),
                        activeCount = attendance.count { it.getFinalAttendanceStatus() == AttendanceStatus.PRESENT },
                        inActiveCount = attendance.count { it.getFinalAttendanceStatus() != AttendanceStatus.PRESENT },
                        expanded = false,
                        viewModel = gigerAttendanceUnderManagerViewModel
                    )

                if (collapsedBusiness.contains(businessName)) {
                    attendanceHeader.expanded = false
                    add(attendanceHeader)
                } else {
                    attendanceHeader.expanded = true
                    add(attendanceHeader)

                    addAll(
                        mapAttendanceApiModelToAttendanceViewModel(
                            attendance,
                            currentMarkingAttendanceForGigs,
                            gigerAttendanceUnderManagerViewModel
                        )
                    )
                }
            }
        }

    private fun filterAttendanceByStatus(
        attendance: List<GigAttendanceApiModel>,
        currentlySelectedStatus: String
    ): List<GigAttendanceApiModel> = if (currentlySelectedStatus == StatusFilters.ENABLED) {
        attendance
    } else {
        attendance.filter {
            if (currentlySelectedStatus == StatusFilters.ACTIVE) {
                AttendanceStatus.PRESENT == it.getFinalAttendanceStatus()
            } else {
                AttendanceStatus.ABSENT == it.getFinalAttendanceStatus() || AttendanceStatus.PENDING == it.getFinalAttendanceStatus()
            }
        }
    }

    private fun filterAttendanceBySearchTerm(
        attendance: List<GigAttendanceApiModel>,
        currentlySearchTerm: String?
    ): List<GigAttendanceApiModel> = if (currentlySearchTerm.isNullOrBlank()) {
        attendance
    } else {
        attendance.filter {
            it.gigerName?.contains(currentlySearchTerm, true) ?: false ||
                    it.jobProfile?.contains(currentlySearchTerm, true) ?: false
        }
    }

    private fun prepareAttendanceTabsCountList(
        attendance: List<GigAttendanceApiModel>,
        currentlySelectedStatus: String
    ): List<AttendanceStatusAndCountItemData> {
        var enabledCount = 0
        var activeCount = 0
        var inactiveCount = 0

        attendance.forEach {
            enabledCount++

            if (it.getFinalAttendanceStatus() == AttendanceStatus.PRESENT) {
                activeCount++
            } else {
                inactiveCount++
            }
        }

        return listOf(
            AttendanceStatusAndCountItemData(
                status = StatusFilters.ENABLED,
                attendanceCount = enabledCount,
                statusSelected = currentlySelectedStatus == StatusFilters.ENABLED
            ),
            AttendanceStatusAndCountItemData(
                status = StatusFilters.ACTIVE,
                attendanceCount = activeCount,
                statusSelected = currentlySelectedStatus == StatusFilters.ACTIVE
            ),
            AttendanceStatusAndCountItemData(
                status = StatusFilters.INACTIVE,
                attendanceCount = inactiveCount,
                statusSelected = currentlySelectedStatus == StatusFilters.INACTIVE
            ),
        )
    }


    private fun mapAttendanceApiModelToAttendanceViewModel(
        attendances: List<GigAttendanceApiModel>,
        currentMarkingAttendanceForGigs: Set<String>,
        viewModel: GigerAttendanceUnderManagerViewModel
    ): Collection<AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData> =
        attendances.sortedBy {
            it.gigerName
        }.map {
            AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData(
                status = it.getFinalAttendanceStatus(),
                statusTextColorCode = it.getFinalStatusTextColorCode(),
                statusBackgroundColorCode = it.getFinalStatusBackgroundColorCode(),
                gigerImage = it.profilePicThumbnail ?: it.profileAvatarName ?: "",
                gigId = it.id ?: "",
                gigerId = it.gigerId ?: "",
                gigerName = it.gigerName ?: "N/A",
                gigerDesignation = it.jobProfile ?: "N/A",
                markedByText = it.getMarkedByText(),
                lastActiveText = it.getLastActiveText(),
                showGigerAttendanceLayout = it.hasUserAndTLMarkedDifferentAttendance(),
                hasAttendanceConflict = it.hasAttendanceConflict(),
                gigerAttendanceStatus = it.getGigerMarkedAttendance(),
                tlMarkedAttendance = it.getTLMarkedAttendance(),
                viewModel = viewModel,
                currentlyMarkingAttendanceForThisGig = currentMarkingAttendanceForGigs.find { gigId -> it.gigerId == gigId } != null,
                businessName = it.businessName ?: "N/A",
                gigerAttendanceMarkingTime = it.gigerAttedance?.checkInTime,
                resolveId = it.resolveAttendanceId,
                hasTLMarkedAttendance = it.hasTLMarkedAttendance(),
                canTLMarkPresent = it.canTLMarkPresent(),
                canTLMarkAbsent = it.canTLMarkAbsent(),
                attendanceType = it.getAttendanceTypeNN()
            )
        }


}