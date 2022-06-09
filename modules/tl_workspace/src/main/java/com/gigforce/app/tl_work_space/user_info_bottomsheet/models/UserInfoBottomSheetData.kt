package com.gigforce.app.tl_work_space.user_info_bottomsheet.models

import androidx.annotation.DrawableRes
import com.gigforce.app.tl_work_space.user_info_bottomsheet.UserInfoBottomSheetViewModel

open class UserInfoBottomSheetData {

    data class UserDetailsAndActionData(
        val gigerId: String,
        val gigerName: String,
        val lastActiveText: String?,
        val profilePicture: String?,
        val profilePictureThumbnail: String?,
        val actionButtons: List<UserInfoActionButtonData>,
        val payoutInformation: PayoutInformation?,
        val viewModel: UserInfoBottomSheetViewModel
    ) : UserInfoBottomSheetData() {

        data class PayoutInformation(
            val status: String,
            val colorCode: String,
            val category: String
        )
    }

    data class UserInfoActionButtonData(
        val id: String,
        @DrawableRes val icon: Int,
        val text: String,
        val viewModel: UserInfoBottomSheetViewModel,
        val navigationRoute: String? = null,
    )

    data class RetentionComplianceWarningCardData(
        val warningText: String,
        @DrawableRes val icon: Int,
        val backgroundColorCode: String,
        val actionButton: UserInfoActionButtonData?,
        val viewModel: UserInfoBottomSheetViewModel
    ) : UserInfoBottomSheetData()


    data class UserDetailsBusinessAndUserDetailsData(
        val business: String,
        val businessIcon: String?,
        val dataItems: List<UserDetailsBusinessAndUserDetailsDataItem>,
        val viewModel: UserInfoBottomSheetViewModel
    ) : UserInfoBottomSheetData() {

        data class UserDetailsBusinessAndUserDetailsDataItem(
            val id: String,
            @DrawableRes val icon: Int,
            val key: String,
            val value: String,
            val actionIcon: UserInfoActionButtonData?,
            val viewModel: UserInfoBottomSheetViewModel
        )
    }
}