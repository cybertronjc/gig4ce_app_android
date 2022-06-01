package com.gigforce.app.tl_work_space.home.mapper

import com.gigforce.app.domain.models.tl_workspace.*
import com.gigforce.app.tl_work_space.home.TLWorkspaceHomeViewModel
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.app.tl_work_space.home.models.ValueChangeType

object ApiModelToPresentationModelMapper {

    fun mapToPresentationList(
        sectionToSelectedDateFilterMap: MutableMap<TLWorkspaceHomeSection, TLWorkSpaceDateFilterOption?>,
        rawData: List<TLWorkSpaceSectionApiModel>,
        tlWorkspaceHomeViewModel: TLWorkspaceHomeViewModel
    ): List<TLWorkspaceRecyclerItemData> {

        return rawData.filter {
            it.sectionId != null
        }/*.filter {

            *//*Removing Upcoming gigers Section If there are no upcoming Gigers*//*
            if (TLWorkspaceHomeSection.UPCOMING_GIGERS.getSectionId() == it.sectionId) {
                it.upcomingGigers != null && it.upcomingGigers!!.isNotEmpty()
            } else {
                true
            }

        }*/.sortedBy {
            it.index ?: 0
        }.map {
            when (TLWorkspaceHomeSection.fromId(it.sectionId!!)) {
                TLWorkspaceHomeSection.ACTIVITY_TRACKER,
                TLWorkspaceHomeSection.PAYOUT,
                TLWorkspaceHomeSection.RETENTION,
                TLWorkspaceHomeSection.SELECTIONS
                -> mapToType1Item(
                    it,
                    sectionToSelectedDateFilterMap,
                    tlWorkspaceHomeViewModel
                )
                TLWorkspaceHomeSection.UPCOMING_GIGERS -> mapToUpcomingGigerItem(
                    it,
                    sectionToSelectedDateFilterMap,
                    tlWorkspaceHomeViewModel
                )
                TLWorkspaceHomeSection.COMPLIANCE_PENDING -> mapToType2Item(
                    it,
                    sectionToSelectedDateFilterMap,
                    tlWorkspaceHomeViewModel
                )
            }
        }
    }

    private fun mapToType1Item(
        it: TLWorkSpaceSectionApiModel,
        sectionToSelectedDateFilterMap: MutableMap<TLWorkspaceHomeSection, TLWorkSpaceDateFilterOption?>,
        tlWorkspaceHomeViewModel: TLWorkspaceHomeViewModel
    ): TLWorkspaceRecyclerItemData.TLWorkspaceType1RecyclerItemData {
        val section = TLWorkspaceHomeSection.fromId(it.sectionId!!)

        return TLWorkspaceRecyclerItemData.TLWorkspaceType1RecyclerItemData(
            viewModel = tlWorkspaceHomeViewModel,
            sectionId = it.sectionId!!,
            sectionTitle = getSectionTitleString(
                it.title,
                it.sectionId!!
            ),
            currentDateFilter = sectionToSelectedDateFilterMap.getOrDefault(
                section,
                null
            ),
            itemData = mapType1InnerItems(
                it.sectionId!!,
                it.items,
                tlWorkspaceHomeViewModel
            ),
            noOfItemsToShowInGrid = getGridColumnCountFor(
                section
            )
        )
    }

    private fun getGridColumnCountFor(section: TLWorkspaceHomeSection): Int {
        return when (section) {
            TLWorkspaceHomeSection.ACTIVITY_TRACKER -> 3
            TLWorkspaceHomeSection.UPCOMING_GIGERS -> -1
            TLWorkspaceHomeSection.PAYOUT -> 3
            TLWorkspaceHomeSection.COMPLIANCE_PENDING -> 3
            TLWorkspaceHomeSection.RETENTION -> 4
            TLWorkspaceHomeSection.SELECTIONS -> 3
        }
    }

    private fun mapType1InnerItems(
        sectionId: String,
        items: List<SectionItemApiModel>?,
        tlWorkspaceHomeViewModel: TLWorkspaceHomeViewModel
    ): List<TLWorkspaceRecyclerItemData.TLWorkType1CardInnerItemData> {
        val section1InnerItems = items ?: return emptyList()
        return section1InnerItems
            .sortedBy {
                it.cardIndex ?: 0
            }
            .map {
                TLWorkspaceRecyclerItemData.TLWorkType1CardInnerItemData(
                    cardId = it.cardId ?: "",
                    sectionId = sectionId,
                    title = it.title ?: "",
                    value = it.count ?: 0,
                    valueChangedBy = it.valueChangedBy ?: 0,
                    changeType = ValueChangeType.fromChangeString(it.valueChangeType),
                    viewModel = tlWorkspaceHomeViewModel
                )
            }
    }


    private fun mapToType2Item(
        it: TLWorkSpaceSectionApiModel,
        sectionToSelectedDateFilterMap: MutableMap<TLWorkspaceHomeSection, TLWorkSpaceDateFilterOption?>,
        tlWorkspaceHomeViewModel: TLWorkspaceHomeViewModel
    ): TLWorkspaceRecyclerItemData.TLWorkspaceType2RecyclerItemData {
        val section = TLWorkspaceHomeSection.fromId(it.sectionId!!)

        return TLWorkspaceRecyclerItemData.TLWorkspaceType2RecyclerItemData(
            viewModel = tlWorkspaceHomeViewModel,
            sectionId = it.sectionId!!,
            sectionTitle = getSectionTitleString(
                it.title,
                it.sectionId!!
            ),
            currentDateFilter = sectionToSelectedDateFilterMap.getOrDefault(
                section,
                null
            ),
            itemData = mapToSectionType2InnerItems(
                it.sectionId!!,
                it.items,
                tlWorkspaceHomeViewModel
            ),
            noOfItemsToShowInGrid = getGridColumnCountFor(
                section
            )
        )
    }

    private fun mapToSectionType2InnerItems(
        sectionId: String,
        items: List<SectionItemApiModel>?,
        tlWorkspaceHomeViewModel: TLWorkspaceHomeViewModel
    ): List<TLWorkspaceRecyclerItemData.TLWorkType2CardInnerItemData> {
        val section2InnerItems = items ?: return emptyList()
        return section2InnerItems.sortedBy {
            it.cardIndex ?: 0
        }.map {
            TLWorkspaceRecyclerItemData.TLWorkType2CardInnerItemData(
                sectionId = sectionId,
                cardId = it.cardId ?: "",
                title = it.title ?: "",
                value = it.count ?: 0,
                valueChangedBy = it.valueChangedBy ?: 0,
                changeType = ValueChangeType.fromChangeString(it.valueChangeType),
                viewModel = tlWorkspaceHomeViewModel
            )
        }
    }

    private fun mapToUpcomingGigerItem(
        it: TLWorkSpaceSectionApiModel,
        sectionToSelectedDateFilterMap: MutableMap<TLWorkspaceHomeSection, TLWorkSpaceDateFilterOption?>,
        tlWorkspaceHomeViewModel: TLWorkspaceHomeViewModel
    ): TLWorkspaceRecyclerItemData.TLWorkspaceUpcomingGigersRecyclerItemData {

        return TLWorkspaceRecyclerItemData.TLWorkspaceUpcomingGigersRecyclerItemData(
            sectionId = it.sectionId!!,
            title = getSectionTitleString(
                it.title,
                it.sectionId!!
            ),
            upcomingGigers = mapToPresentationUpcomingGigModel(
                it.upcomingGigers,
                tlWorkspaceHomeViewModel
            ),
            viewModel = tlWorkspaceHomeViewModel
        )
    }

    private fun mapToPresentationUpcomingGigModel(
        it: List<UpcomingGigersApiModel>?,
        tlWorkspaceHomeViewModel: TLWorkspaceHomeViewModel
    ): List<TLWorkspaceRecyclerItemData.UpcomingGigerInnerItemData> {
        val gigersInApiModel = it ?: return emptyList()
        return gigersInApiModel.map {
            TLWorkspaceRecyclerItemData.UpcomingGigerInnerItemData(
                gigerId = it.gigerId!!,
                gigerName = it.gigerName ?: "----",
                business = it.business ?: "",
                jobProfile = it.jobProfile ?: "",
                profilePicture = it.profilePicture,
                profilePictureThumbnail = it.profilePictureThumbnail,
                viewModel = tlWorkspaceHomeViewModel
            )
        }
    }

    private fun getSectionTitleString(
        title: String?,
        sectionId: String
    ): String {
        if (!title.isNullOrBlank()) return title
        return when (TLWorkspaceHomeSection.fromId(sectionId)) {
            TLWorkspaceHomeSection.ACTIVITY_TRACKER -> "Activity Tracker"
            TLWorkspaceHomeSection.UPCOMING_GIGERS -> "Upcoming Gigers"
            TLWorkspaceHomeSection.PAYOUT -> "Payout"
            TLWorkspaceHomeSection.COMPLIANCE_PENDING -> "Compliance Pending"
            TLWorkspaceHomeSection.RETENTION -> "Retention"
            TLWorkspaceHomeSection.SELECTIONS -> "Selections"
            else -> ""
        }
    }
}