package com.gigforce.app.tl_work_space.compliance_pending

import com.gigforce.app.android_common_utils.extensions.containsIgnoreCase
import com.gigforce.app.domain.models.tl_workspace.compliance.GigersWithPendingComplainceDataItem
import com.gigforce.app.tl_work_space.compliance_pending.models.CompliancePendingScreenData
import com.gigforce.app.tl_work_space.compliance_pending.models.ComplianceStatusData

object ComplianceDataProcessor {

    fun processRawComplianceDataForListForView(
        rawComplianceList: List<GigersWithPendingComplainceDataItem>,
        statusMaster: List<ComplianceStatusData>,
        searchText: String?,
        collapsedBusinessId: List<String>,
        selectedStatus: ComplianceStatusData?,
        viewModel: CompliancePendingViewModel
    ): Pair<List<ComplianceStatusData>, List<CompliancePendingScreenData>> {

        val filteredGigersWithIncompleteCompliance = filterGigersWithSearchString(
            rawComplianceList,
            searchText
        )

        val updatedStatusMaster = updateCountInStatuses(
            statusMaster,
            selectedStatus,
            filteredGigersWithIncompleteCompliance
        )

        val gigersAfterFinalFilter = filterGigersWithStatus(
            selectedStatus,
            filteredGigersWithIncompleteCompliance
        )
        val gigersListForView = mapGigersDataToPresentationModel(
            gigersAfterFinalFilter,
            viewModel,
            collapsedBusinessId
        )

        return updatedStatusMaster to gigersListForView
    }

    private fun mapGigersDataToPresentationModel(
        gigers: List<GigersWithPendingComplainceDataItem>,
        viewModel: CompliancePendingViewModel,
        collapsedBusinessId: List<String>
    ): List<CompliancePendingScreenData> {

        val businessToGigerMap = gigers.filter {
            !it.compliancePending.isNullOrEmpty()
        }.sortedBy {
            it.getBusinessNonNull()
        }.groupBy {
            it.getBusinessNonNull()
        }

        return prepareAttendanceListForView(
            businessToGigerMap = businessToGigerMap,
            viewModel = viewModel,
            collapsedBusiness = collapsedBusinessId
        )
    }

    private fun prepareAttendanceListForView(
        businessToGigerMap: Map<String, List<GigersWithPendingComplainceDataItem>>,
        viewModel: CompliancePendingViewModel,
        collapsedBusiness: List<String>
    ) = mutableListOf<CompliancePendingScreenData>().apply {

        businessToGigerMap.forEach { (businessName, gigerList) ->
            val businessExpanded = !collapsedBusiness.contains(businessName)

            val businessHeader = CompliancePendingScreenData.BusinessItemData(
                businessName = businessName,
                expanded = businessExpanded,
                viewModel = viewModel,
                count = gigerList.count()
            )
            add(businessHeader)

            if (businessExpanded) {

                addAll(
                    mapGigereApiModelToComplianceGigerModel(
                        gigerList,
                        viewModel
                    )
                )
            }
        }
    }

    private fun mapGigereApiModelToComplianceGigerModel(
        gigerList: List<GigersWithPendingComplainceDataItem>,
        viewModel: CompliancePendingViewModel
    ): Collection<CompliancePendingScreenData.GigerItemData> {

        return gigerList.sortedBy {
            it.name
        }.map {
            CompliancePendingScreenData.GigerItemData(
                gigerId = it.gigerId ?: "",
                gigerName = it.name ?: "N/A",
                phoneNumber = it.mobileNumber,
                business = it.getBusinessNonNull(),
                jobProfileId = it.jobProfileId ?: "",
                jobProfile = it.jobProfile,
                profilePicture = it.profileAvatarName,
                profilePictureThumbnail = it.profilePicThumbnail,
                selectionDateString = "",
                warningText = it.warningText,
                viewModel = viewModel
            )
        }
    }

    private fun filterGigersWithStatus(
        selectedStatus: ComplianceStatusData?,
        gigers: List<GigersWithPendingComplainceDataItem>
    ): List<GigersWithPendingComplainceDataItem> {

        if (selectedStatus != null) {
            return gigers.filter {
                it.compliancePending != null && it.compliancePending!!.containsIgnoreCase(
                    selectedStatus.id
                )
            }
        } else {
            return gigers
        }
    }

    private fun updateCountInStatuses(
        statusMaster: List<ComplianceStatusData>,
        selectedStatus: ComplianceStatusData?,
        filteredGigersWithIncompleteCompliance: List<GigersWithPendingComplainceDataItem>
    ): List<ComplianceStatusData> {
        return statusMaster.apply {
            onEach { status ->
                status.value = filteredGigersWithIncompleteCompliance.count {
                    it.compliancePending != null && it.compliancePending!!.containsIgnoreCase(status.id)
                }
                status.selected = selectedStatus?.id == status.id
            }
        }
    }

    private fun filterGigersWithSearchString(
        rawComplianceList: List<GigersWithPendingComplainceDataItem>,
        searchText: String?
    ): List<GigersWithPendingComplainceDataItem> {
        if (searchText.isNullOrBlank())
            return rawComplianceList
        else {

            return rawComplianceList.filter {
                it.name?.contains(
                    searchText,
                    true
                ) ?: false
                        || it.mobileNumber?.contains(
                    searchText,
                    true
                ) ?: false
            }
        }
    }
}