package com.gigforce.app.tl_work_space.upcoming_gigers

import com.gigforce.app.android_common_utils.extensions.capitalizeFirstLetter
import com.gigforce.app.domain.models.tl_workspace.UpcomingGigersApiModel
import com.gigforce.app.tl_work_space.upcoming_gigers.models.UpcomingGigersListData

object UpcomingGigersListProcessor {

    fun processRawUpcomingListForView(
        rawList: List<UpcomingGigersApiModel>,
        searchText: String?,
        upcomingGigersViewModel: UpcomingGigersViewModel
    ): List<UpcomingGigersListData> {
        if (rawList.isEmpty()) return emptyList()

        val filteredList = filterUpcomingGigers(searchText, rawList)
        return mapListToPresentationData(
            filteredList,
            upcomingGigersViewModel
        )
    }

    private fun mapListToPresentationData(
        filteredList: List<UpcomingGigersApiModel>,
        upcomingGigersViewModel: UpcomingGigersViewModel
    ): List<UpcomingGigersListData> {

        val businessToGigersGroup = filteredList.filter {
            it.business.isNullOrBlank().not()
        }.groupBy {
            it.business
        }

        return mutableListOf<UpcomingGigersListData>().apply {

            businessToGigersGroup.forEach { (businessName, giger) ->
                add(
                    UpcomingGigersListData.BusinessItemData(
                        businessName = businessName!!.capitalizeFirstLetter()
                    )
                )

                giger.forEach {
                    add(mapGigerItemToPresentation(it, upcomingGigersViewModel))
                }
            }
        }
    }

    private fun mapGigerItemToPresentation(
        giger: UpcomingGigersApiModel,
        upcomingGigersViewModel: UpcomingGigersViewModel
    ): UpcomingGigersListData.UpcomingGigerItemData {

        return UpcomingGigersListData.UpcomingGigerItemData(
            gigerId = giger.gigerId ?: "",
            gigerName = giger.gigerName ?: "",
            phoneNumber = giger.mobileNumber,
            business = giger.business,
            jobProfile = giger.jobProfile,
            profilePicture = giger.profilePicture,
            profilePictureThumbnail = giger.profilePictureThumbnail,
            viewModel = upcomingGigersViewModel
        )
    }

    private fun filterUpcomingGigers(
        searchText: String?,
        rawList: List<UpcomingGigersApiModel>
    ): List<UpcomingGigersApiModel> {
        return if (searchText.isNullOrBlank()) {
            rawList
        } else {
            filterUpcomingGigersList(
                rawList,
                searchText
            )
        }
    }

    private fun filterUpcomingGigersList(
        rawList: List<UpcomingGigersApiModel>,
        searchText: String
    ): List<UpcomingGigersApiModel> {

        return rawList.filter {
            it.gigerName?.contains(
                searchText,
                true
            ) ?: false || it.business?.contains(
                searchText,
                true
            ) ?: false
        }
    }
}