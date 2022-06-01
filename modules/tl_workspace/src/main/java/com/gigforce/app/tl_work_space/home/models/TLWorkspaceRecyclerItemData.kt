package com.gigforce.app.tl_work_space.home.models

import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceDateFilterOption
import com.gigforce.app.tl_work_space.TLWorkSpaceCoreRecyclerViewBindings
import com.gigforce.app.tl_work_space.home.TLWorkspaceHomeViewModel
import com.gigforce.core.SimpleDVM

open class TLWorkspaceRecyclerItemData(
    val type: Int
) : SimpleDVM(type) {

    /**
     * Data Model for Type 1 Card ex : activity Tracker Card
     */
    data class TLWorkspaceType1RecyclerItemData(
        val sectionId: String,
        val sectionTitle: String,
        val currentDateFilter: TLWorkSpaceDateFilterOption?,
        val itemData: List<TLWorkType1CardInnerItemData>,
        val noOfItemsToShowInGrid: Int,
        val viewModel: TLWorkspaceHomeViewModel
    ) : TLWorkspaceRecyclerItemData(
        type = TLWorkSpaceCoreRecyclerViewBindings.TLWorkspaceType1SectionItemType
    )

    /**
     * ^ Type 1 Card inner data model
     */
    data class TLWorkType1CardInnerItemData(
        val sectionId: String,
        val cardId: String,
        val title: String,
        val value: Int,
        val valueChangedBy: Int,
        val changeType: ValueChangeType,
        val viewModel: TLWorkspaceHomeViewModel
    ) : SimpleDVM(
        TLWorkSpaceCoreRecyclerViewBindings.TLWorkspaceType1InnerCardType
    ) {

        fun hasSameContentAs(
            data: TLWorkType1CardInnerItemData
        ): Boolean {
            return this.title == data.title &&
                    this.value == data.value &&
                    this.valueChangedBy == data.valueChangedBy &&
                    this.changeType == data.changeType &&
                    this.sectionId == data.sectionId
        }
    }


    /**
     * Data Model for Type 1 Card ex : compliance pending card
     */
    data class TLWorkspaceType2RecyclerItemData(
        val sectionId: String,
        val sectionTitle: String,
        val currentDateFilter: TLWorkSpaceDateFilterOption?,
        val itemData: List<TLWorkType2CardInnerItemData>,
        val noOfItemsToShowInGrid: Int,
        val viewModel: TLWorkspaceHomeViewModel
    ) : TLWorkspaceRecyclerItemData(
        type = TLWorkSpaceCoreRecyclerViewBindings.TLWorkspaceType2SectionItemType
    )

    /**
     * ^ Type 2 Card inner data model
     */
    data class TLWorkType2CardInnerItemData(
        val sectionId: String,
        val cardId: String,
        val title: String,
        val value: Int,
        val valueChangedBy: Int,
        val changeType: ValueChangeType,
        val viewModel: TLWorkspaceHomeViewModel
    ) : SimpleDVM(
        TLWorkSpaceCoreRecyclerViewBindings.TLWorkspaceType2InnerCardType
    ) {

        fun hasSameContentAs(
            data: TLWorkType2CardInnerItemData
        ): Boolean {
            return this.title == data.title &&
                    this.value == data.value &&
                    this.valueChangedBy == data.valueChangedBy &&
                    this.changeType == data.changeType &&
                    this.sectionId == data.sectionId
        }
    }

    /**
     * Upcoming giger section card data model
     */
    data class TLWorkspaceUpcomingGigersRecyclerItemData(
        val sectionId: String,
        val title: String,
        val upcomingGigers: List<UpcomingGigerInnerItemData>,
        val viewModel: TLWorkspaceHomeViewModel
    ) : TLWorkspaceRecyclerItemData(
        type = TLWorkSpaceCoreRecyclerViewBindings.UpcomingGigersSectionItemType
    )

    /**
     * ^ Upcoming giger section inner individual giger card data model
     */
    data class UpcomingGigerInnerItemData(
        val gigerId: String,
        val gigerName: String,
        val business: String? = null,
        val jobProfile: String? = null,
        val profilePicture: String? = null,
        val profilePictureThumbnail: String? = null,
        val viewModel: TLWorkspaceHomeViewModel
    ) : TLWorkspaceRecyclerItemData(
        type = TLWorkSpaceCoreRecyclerViewBindings.UpcomingGigersInnerItemType
    ) {

        fun hasSameContentAs(
            data: UpcomingGigerInnerItemData
        ): Boolean {
            return this.gigerId == data.gigerId &&
                    this.gigerName == data.gigerName &&
                    this.jobProfile == data.jobProfile &&
                    this.business == data.business &&
                    this.profilePicture == data.profilePicture &&
                    this.profilePictureThumbnail == data.profilePictureThumbnail
        }
    }
}