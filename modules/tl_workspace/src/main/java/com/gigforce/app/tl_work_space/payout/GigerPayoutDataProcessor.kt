package com.gigforce.app.tl_work_space.payout

import com.gigforce.app.domain.models.tl_workspace.payout.GigerPayoutListItem
import com.gigforce.app.tl_work_space.payout.models.GigerPayoutScreenData
import com.gigforce.app.tl_work_space.payout.models.GigerPayoutStatusData

object GigerPayoutDataProcessor {

    fun processRawGigerPayoutDataForListForView(
        rawGigerGigerPayoutList: List<GigerPayoutListItem>,
        searchText: String?,
        tabMaster: List<GigerPayoutStatusData>,
        collapsedBusinessIds: List<String>,
        selectedTab: GigerPayoutStatusData?,
        retentionViewModel: GigerPayoutViewModel
    ): Pair<List<GigerPayoutStatusData>, List<GigerPayoutScreenData>> {

        val filteredGigers = filterGigersWithSearchString(
            rawGigerGigerPayoutList,
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
        rawGigerList: List<GigerPayoutListItem>,
        searchText: String?
    ): List<GigerPayoutListItem> {
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
        statusMaster: List<GigerPayoutStatusData>,
        filteredGigersWithIncompleteCompliance: List<GigerPayoutListItem>
    ): List<GigerPayoutStatusData> {
        return statusMaster.apply {
            onEach { status ->
                status.value = filteredGigersWithIncompleteCompliance.count {
                    it.tabStatus != null && it.tabStatus!!.contains(status.id)
                }
            }
        }
    }

    private fun filterGigersWithStatus(
        selectedStatus: GigerPayoutStatusData?,
        gigers: List<GigerPayoutListItem>
    ): List<GigerPayoutListItem> {
        if (selectedStatus != null) {
            return gigers.filter {
                it.tabStatus != null && it.tabStatus!!.contains(selectedStatus.id)
            }
        } else {
            return gigers
        }
    }

    private fun mapGigersDataToPresentationModel(
        gigers: List<GigerPayoutListItem>,
        viewModel: GigerPayoutViewModel,
        collapsedBusinessId: List<String>
    ): MutableList<GigerPayoutScreenData> {

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
        businessToGigerMap: Map<String, List<GigerPayoutListItem>>,
        viewModel: GigerPayoutViewModel,
        collapsedBusiness: List<String>
    ) = mutableListOf<GigerPayoutScreenData>().apply {

        businessToGigerMap.forEach { (businessName, gigerList) ->
            val businessExpanded = !collapsedBusiness.contains(businessName)

            val businessHeader = GigerPayoutScreenData.BusinessItemData(
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
        gigerList: List<GigerPayoutListItem>,
        viewModel: GigerPayoutViewModel
    ): Collection<GigerPayoutScreenData.GigerItemData> {

        return gigerList.sortedBy {
            it.name
        }.map {
            GigerPayoutScreenData.GigerItemData(
                gigerId = it.gigerId ?: "",
                gigerName = it.name ?: "N/A",
                phoneNumber = it.mobileNumber,
                business = it.getBusinessNonNull(),
                jobProfile = it.jobProfile,
                profilePicture = it.profilePicture,
                profilePictureThumbnail = it.profilePictureThumbnail,
                selectionDateString = "",
                category = it.category,
                amount = it.amount,
                status = it.payoutStatus.toString() ?: "N/A",
                statusColorCode = it.statusColorCode.toString(),
                paymentDate = it.paidOnDate,
                viewModel = viewModel
            )
        }
    }
}