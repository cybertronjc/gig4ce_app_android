package com.gigforce.giger_gigs.attendance_tl.attendance_list

import com.gigforce.common_ui.datamodels.attendance.GigAttendanceApiModel
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData

object AttendanceUnderTLListDataProcessor {

    fun processAttendanceListAndFilters(
        attendance: List<GigAttendanceApiModel>,
        collapsedBusiness: List<String>,
        currentMarkingAttendanceForGigs: MutableSet<String>,
        currentlySelectedStatus: String,
        currentlySearchTerm: String?,
        gigerAttendanceUnderManagerViewModel: GigerAttendanceUnderManagerViewModel
    ): List<AttendanceRecyclerItemData> {

        val filteredAttendance = filterAttendanceList(
            attendance,
            currentlySelectedStatus,
            currentlySearchTerm
        )

        val businessToAttendanceGroup = filteredAttendance
            .sortedByDescending { it.getBusinessNameNN() }
            .groupBy {
                it.getBusinessNameNN()
            }

        return mutableListOf<AttendanceRecyclerItemData>()
            .apply {

                businessToAttendanceGroup.forEach { (businessName, attendance) ->
                    val attendanceHeader =
                        AttendanceRecyclerItemData.AttendanceBusinessHeaderItemData(
                            businessName = businessName,
                            enabledCount = attendance.count(),
                            activeCount = 0,
                            inActiveCount = 0,
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
                            mapPayoutsToPayoutItemView(
                                attendance,
                                currentMarkingAttendanceForGigs,
                                gigerAttendanceUnderManagerViewModel
                            )
                        )
                    }
                }
            }
    }

    fun filterAttendanceList(
        attendance: List<GigAttendanceApiModel>,
        currentlySelectedStatus: String,
        currentlySearchTerm: String?
    ): List<GigAttendanceApiModel> {
        return attendance.run {

            if (!currentlySearchTerm.isNullOrBlank()) {
                this.filter {

                    it.gigerName?.contains(currentlySearchTerm, true) ?: false ||
                            it.jobProfile?.contains(currentlySearchTerm, true) ?: false
                }
            }

            this.filter {
                true
            }
        }
    }

    private fun mapPayoutsToPayoutItemView(
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
                gigerImage = it.gigerName ?: "",
                gigId = it.id ?: "",
                gigerId = it.gigerId ?: "",
                gigerName = it.gigerName ?: "N/A",
                gigerDesignation = it.jobProfile ?: "N/A",
                markedByText = it.getMarkedByText(),
                lastActiveText = it.getLastActiveText(),
                hasAttendanceConflict = it.hasAttendanceConflict(),
                gigerAttendanceStatus = "",//todo
                viewModel = viewModel,
                currentlyMarkingAttendanceForThisGig =  currentMarkingAttendanceForGigs.find { gigId -> it.gigerId == gigId } != null,
                businessName = it.businessName ?: "N/A"
            )
        }


}