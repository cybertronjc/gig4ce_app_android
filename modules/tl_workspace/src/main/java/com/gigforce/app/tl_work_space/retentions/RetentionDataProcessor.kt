package com.gigforce.app.tl_work_space.retentions

import com.gigforce.app.domain.models.tl_workspace.retention.GigersRetentionListItem
import com.gigforce.app.tl_work_space.retentions.models.RetentionScreenData
import com.gigforce.app.tl_work_space.retentions.models.RetentionTabData

object RetentionDataProcessor {

    fun processRawRetentionDataForListForView(
        rawGigerRetentionList: List<GigersRetentionListItem>,
        searchText: String?,
        tabMaster: List<RetentionTabData>,
        collapsedBusinessIds: List<String>,
        selectedTab: RetentionTabData?,
        retentionViewModel: RetentionViewModel
    ): Pair<List<RetentionTabData>, List<RetentionScreenData>> {

        val filteredGigers = filterGigersWithSearchString(
            rawGigerRetentionList,
            searchText
        )

        val updatedStatusMaster = updateCountInTabs(
            tabMaster,
            filteredGigers
        )

        val gigersAfterFinalFilter = filterGigersWithStatus(
            selectedTab,
            filteredGigers
        )
        val gigersListForView = mapGigersDataToPresentationModel(
            gigersAfterFinalFilter,
            retentionViewModel,
            collapsedBusinessIds
        )

        return updatedStatusMaster to gigersListForView
    }

    private fun filterGigersWithSearchString(
        rawGigerList: List<GigersRetentionListItem>,
        searchText: String?
    ): List<GigersRetentionListItem> {
        if (searchText.isNullOrBlank())
            return rawGigerList
        else {

            return rawGigerList.filter {
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

    private fun updateCountInTabs(
        statusMaster: List<RetentionTabData>,
        filteredGigersWithIncompleteCompliance: List<GigersRetentionListItem>
    ): List<RetentionTabData> {
        return statusMaster.apply {
            onEach { status ->
                status.value = filteredGigersWithIncompleteCompliance.count {
                    it.tabStatus != null && it.tabStatus!!.contains(status.id)
                }
            }
        }
    }

    private fun filterGigersWithStatus(
        selectedStatus: RetentionTabData?,
        gigers: List<GigersRetentionListItem>
    ): List<GigersRetentionListItem> {
        if (selectedStatus != null) {
            return gigers.filter {
                it.tabStatus != null && it.tabStatus!!.contains(selectedStatus.id)
            }
        } else {
            return gigers
        }
    }

    private fun mapGigersDataToPresentationModel(
        gigers: List<GigersRetentionListItem>,
        viewModel: RetentionViewModel,
        collapsedBusinessId: List<String>
    ): MutableList<RetentionScreenData> {

        val businessToGigerMap = gigers.filter {
            !it.tabStatus.isNullOrEmpty()
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
        businessToGigerMap: Map<String, List<GigersRetentionListItem>>,
        viewModel: RetentionViewModel,
        collapsedBusiness: List<String>
    ) = mutableListOf<RetentionScreenData>().apply {

        businessToGigerMap.forEach { (businessName, gigerList) ->
            val businessExpanded = !collapsedBusiness.contains(businessName)

            val businessHeader = RetentionScreenData.BusinessItemData(
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
        gigerList: List<GigersRetentionListItem>,
        viewModel: RetentionViewModel
    ): Collection<RetentionScreenData.GigerItemData> {

        return gigerList.sortedBy {
            it.name
        }.map {
            RetentionScreenData.GigerItemData(
                gigerId = it.gigerId ?: "",
                gigerName = it.name ?: "N/A",
                phoneNumber = it.mobileNumber,
                business = it.getBusinessNonNull(),
                jobProfile = it.jobProfile,
                profilePicture = it.profilePicture,
                profilePictureThumbnail = it.profilePictureThumbnail,
                selectionDateString = "",
                warningText = it.warningString,
                viewModel = viewModel
            )
        }
    }
}
