package com.gigforce.app.tl_work_space.activity_tacker.attendance_list

import com.gigforce.app.data.repositoriesImpl.gigs.AttendanceStatus
import com.gigforce.app.data.repositoriesImpl.gigs.GigAttendanceApiModel
import com.gigforce.app.tl_work_space.activity_tacker.models.AttendanceRecyclerItemData
import com.gigforce.app.tl_work_space.activity_tacker.models.AttendanceTabData

object AttendanceUnderTLListDataProcessor {


    fun processAttendanceListAndFilters(
        attendance: List<GigAttendanceApiModel>,
        collapsedBusiness: List<String>,
        currentMarkingAttendanceForGigs: MutableSet<String>,
        currentlySelectedStatus: String,
        currentlySearchTerm: String?,
        prepareAttendanceStatusAndCount: Boolean,
        gigerAttendanceUnderManagerViewModel: GigerAttendanceUnderManagerViewModel,
        tabsMaster: List<AttendanceTabData>
    ): Pair<List<AttendanceRecyclerItemData>, List<AttendanceTabData>?> {


        val attendanceFilteredBySearchTerm = filterAttendanceBySearchTerm(
            attendance,
            currentlySearchTerm
        )

        val tabStatusWithCount = if (prepareAttendanceStatusAndCount) {
            prepareAttendanceTabsCountList(
                tabsMaster,
                attendanceFilteredBySearchTerm,
                currentlySelectedStatus
            )
        } else {
            null
        }

        val businessToAttendanceGroup = filterAttendanceByStatus(
            attendanceFilteredBySearchTerm,
            currentlySelectedStatus,
        ).sortedBy {
            it.getBusinessNameNN()
        }.groupBy {
            it.getBusinessNameNN()
        }

        val attendanceListForView = prepareAttendanceListForView(
            businessToAttendanceGroup,
            gigerAttendanceUnderManagerViewModel,
            collapsedBusiness,
            currentlySelectedStatus,
            currentMarkingAttendanceForGigs,
        )
        return attendanceListForView to tabStatusWithCount
    }

    private fun prepareAttendanceListForView(
        businessToAttendanceGroup: Map<String, List<GigAttendanceApiModel>>,
        gigerAttendanceUnderManagerViewModel: GigerAttendanceUnderManagerViewModel,
        collapsedBusiness: List<String>,
        currentlySelectedStatus: String,
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
                        viewModel = gigerAttendanceUnderManagerViewModel,
                        currentlySelectedStatus = currentlySelectedStatus
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
        tabsMaster: List<AttendanceTabData>,
        attendance: List<GigAttendanceApiModel>,
        currentlySelectedStatus: String
    ): List<AttendanceTabData> {

        var enabled = 0
        var active = 0
        var inactive = 0

        attendance.forEach {
            enabled++

            if(it.getFinalAttendanceStatus() == AttendanceStatus.PRESENT){
                active++
            } else{
                inactive++
            }
        }

        return tabsMaster.onEach { currentWorkingOnItem ->

            if (currentWorkingOnItem.id == StatusFilters.ENABLED) {
                currentWorkingOnItem.value = enabled
            } else if (currentWorkingOnItem.id == StatusFilters.ACTIVE) {
                currentWorkingOnItem.value = active
            } else if (currentWorkingOnItem.id == StatusFilters.INACTIVE) {
                currentWorkingOnItem.value = inactive
            }

            currentWorkingOnItem.selected  = currentWorkingOnItem.id == currentlySelectedStatus
        }
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