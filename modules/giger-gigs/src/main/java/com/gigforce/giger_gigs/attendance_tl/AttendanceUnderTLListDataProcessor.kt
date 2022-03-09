package com.gigforce.giger_gigs.attendance_tl

import com.gigforce.common_ui.datamodels.attendance.GigAttendanceApiModel
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData

object AttendanceUnderTLListDataProcessor {

    fun processAttendanceListAndFilters(
        attendance: List<GigAttendanceApiModel>,
        collapsedBusiness: List<String>,
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
                            businessName = "",
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
                                gigerAttendanceUnderManagerViewModel
                            )
                        )
                    }
                }
            }
    }

    fun filterAttendanceList(
        attendance: List<GigAttendanceApiModel>,
        currentlySelectedStatus: String?,
        currentlySearchTerm: String?
    ): List<GigAttendanceApiModel> {
       return attendance.run {

           if(!currentlySearchTerm.isNullOrBlank()){
               this.filter {

                   it.gigerName?.contains(currentlySearchTerm,true) ?: false ||
                   it.jobProfile?.contains(currentlySearchTerm,true) ?: false
               }
           }

           this.filter {
               true
           }
        }
    }

    private fun mapPayoutsToPayoutItemView(
        attendances: List<GigAttendanceApiModel>,
        viewModel: GigerAttendanceUnderManagerViewModel
    ): Collection<AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData> =
        attendances.sortedBy {
            it.gigerName
        }.map {
            AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData(
                status = "",
                statusTextColorCode = "",
                statusBackgroundColorCode = "",
                gigerImage = it.gigerName ?: "",
                gigId = "",
                gigerId = "",
                gigerName = "",
                gigerDesignation = "",
                markedByText = "",
                lastActiveText = "",
                hasAttendanceConflict = false,
                gigerAttendanceStatus = "",
                viewModel = viewModel
            )
        }


}