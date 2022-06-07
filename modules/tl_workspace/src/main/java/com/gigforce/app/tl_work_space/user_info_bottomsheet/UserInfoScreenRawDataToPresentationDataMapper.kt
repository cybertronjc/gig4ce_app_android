package com.gigforce.app.tl_work_space.user_info_bottomsheet

import com.gigforce.app.data.repositoriesImpl.tl_workspace.user_info.GigerInfoApiModel
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.user_info_bottomsheet.models.UserInfoBottomSheetData
import com.gigforce.common_ui.ext.formatToCurrency

object UserInfoScreenRawDataToPresentationDataMapper {

    val ID_CALL_GIGER = "call_giger"
    val ID_DROP_GIGER = "drop_giger"
    val ID_CHANGE_TL = "change_tl"
    val ID_OPEN_ATTENDANCE_HISTORY = "open_giger_attendance_history"
    val ID_DISABLE_GIGER = "disable_giger"
    val ID_CALL_SCOUT = "call_scout"
    val ID_DOWNLOAD_PAYSLIPS = "download_payslip"
    val ID_CHANGE_CLIENT_ID = "change_client_id"
    val ID_NAVIGATE_TO_OTHER_SCREEN = "navigateToOtherScreen"

    fun prepareUserInfoSections(
        openGigerDetailsFor: String,
        rawGigerData: GigerInfoApiModel,
        viewModel: UserInfoBottomSheetViewModel,
    ): List<UserInfoBottomSheetData> {
        return when (openGigerDetailsFor) {
            TLWorkSpaceNavigation.PAYOUT -> prepareViewItemsForPayout(
                openGigerDetailsFor,
                rawGigerData,
                viewModel
            )
            TLWorkSpaceNavigation.RETENTION -> prepareViewItemsForRetention(
                openGigerDetailsFor,
                rawGigerData,
                viewModel
            )
            else -> prepareViewItemsForCompliance(
                openGigerDetailsFor,
                rawGigerData,
                viewModel
            )
        }
    }

    private fun prepareViewItemsForPayout(
        openGigerDetailsFor: String,
        rawGigerData: GigerInfoApiModel,
        viewModel: UserInfoBottomSheetViewModel
    ) = mutableListOf<UserInfoBottomSheetData>().apply {
        add(
            prepareGigDetailsData(
                openGigerDetailsFor = openGigerDetailsFor,
                rawGigerData = rawGigerData,
                viewModel = viewModel
            )
        )

        add(
            prepareBottomCardData(
                openGigerDetailsFor = openGigerDetailsFor,
                rawGigerData = rawGigerData,
                viewModel = viewModel
            )
        )
    }

    private fun prepareViewItemsForCompliance(
        openGigerDetailsFor: String,
        rawGigerData: GigerInfoApiModel,
        viewModel: UserInfoBottomSheetViewModel
    ) = mutableListOf<UserInfoBottomSheetData>().apply {

        add(
            prepareGigDetailsData(
                openGigerDetailsFor = openGigerDetailsFor,
                rawGigerData = rawGigerData,
                viewModel = viewModel
            )
        )

        if (rawGigerData.pendingComplianceInformation?.string != null) {

            UserInfoBottomSheetData.RetentionComplianceWarningCardData(
                warningText = rawGigerData.pendingComplianceInformation?.string!!,
                icon = if ("high" == rawGigerData.pendingComplianceInformation?.priority) R.drawable.ic_baseline_info_24 else R.drawable.ic_warning_white,
                backgroundColorCode = rawGigerData.pendingComplianceInformation?.backgroundColorCode
                    ?: "#F9B021",
                actionButton = if (rawGigerData.pendingComplianceInformation?.navigationRoute != null)
                    UserInfoBottomSheetData.UserInfoActionButtonData(
                        id = ID_NAVIGATE_TO_OTHER_SCREEN,
                        icon = -1,
                        text = "Review",
                        viewModel = viewModel,
                        navigationRoute = rawGigerData.pendingComplianceInformation?.navigationRoute
                    ) else null,
                viewModel = viewModel
            )
        }

        add(
            prepareBottomCardData(
                openGigerDetailsFor = openGigerDetailsFor,
                rawGigerData = rawGigerData,
                viewModel = viewModel
            )
        )
    }

    private fun prepareViewItemsForRetention(
        openGigerDetailsFor: String,
        rawGigerData: GigerInfoApiModel,
        viewModel: UserInfoBottomSheetViewModel
    ) = mutableListOf<UserInfoBottomSheetData>().apply {

        add(
            prepareGigDetailsData(
                openGigerDetailsFor = openGigerDetailsFor,
                rawGigerData = rawGigerData,
                viewModel = viewModel
            )
        )

        if (rawGigerData.retention?.lastActiveWarningString != null) {
            add(
                UserInfoBottomSheetData.RetentionComplianceWarningCardData(
                    warningText = rawGigerData.retention?.lastActiveWarningString!!,
                    icon = R.drawable.ic_baseline_info_24,
                    backgroundColorCode = rawGigerData.retention?.backgroundColorCode ?: "#E11900",
                    actionButton = null,
                    viewModel = viewModel
                )
            )
        }

        add(
            prepareBottomCardData(
                openGigerDetailsFor = openGigerDetailsFor,
                rawGigerData = rawGigerData,
                viewModel = viewModel
            )
        )
    }

    private fun prepareGigDetailsData(
        openGigerDetailsFor: String,
        rawGigerData: GigerInfoApiModel,
        viewModel: UserInfoBottomSheetViewModel
    ): UserInfoBottomSheetData {

        val actionItems = when (openGigerDetailsFor) {
            TLWorkSpaceNavigation.PAYOUT -> prepareActionButtonListForPayout(
                rawGigerData,
                viewModel
            )
            TLWorkSpaceNavigation.RETENTION -> prepareActionButtonListForRetention(
                rawGigerData,
                viewModel
            )
            else -> prepareActionButtonListForCompliance(
                rawGigerData,
                viewModel
            )
        }

        return UserInfoBottomSheetData.UserDetailsAndActionData(
            gigerId = rawGigerData.gigerId ?: "",
            gigerName = rawGigerData.gigerName ?: "",
            lastActiveText = rawGigerData.lastActiveText,
            profilePicture = rawGigerData.gigerProfilePicture,
            profilePictureThumbnail = rawGigerData.gigerProfilePictureThumbnail,
            actionButtons = actionItems,
            viewModel = viewModel
        )
    }

    private fun prepareActionButtonListForPayout(
        rawGigerData: GigerInfoApiModel,
        viewModel: UserInfoBottomSheetViewModel
    ) = mutableListOf<UserInfoBottomSheetData.UserInfoActionButtonData>().apply {

        if (!rawGigerData.gigerMobile.isNullOrBlank()) {

            add(
                UserInfoBottomSheetData.UserInfoActionButtonData(
                    id = ID_CALL_GIGER,
                    icon = R.drawable.ic_call_phone_pink,
                    text = "Call",
                    viewModel = viewModel
                )
            )
        }

        add(
            UserInfoBottomSheetData.UserInfoActionButtonData(
                id = ID_CHANGE_TL,
                icon = R.drawable.ic_change_pink,
                text = "Change TL",
                viewModel = viewModel
            )
        )

        add(
            UserInfoBottomSheetData.UserInfoActionButtonData(
                id = ID_OPEN_ATTENDANCE_HISTORY,
                icon = R.drawable.ic_calendar_pink,
                text = "Att. History",
                viewModel = viewModel
            )
        )

        if (!rawGigerData.payoutInformation?.pdfUrl.isNullOrBlank()) {

            add(
                UserInfoBottomSheetData.UserInfoActionButtonData(
                    id = ID_DOWNLOAD_PAYSLIPS,
                    icon = R.drawable.ic_download_payslip,
                    text = "Payslips",
                    viewModel = viewModel
                )
            )
        }
    }

    private fun prepareActionButtonListForRetention(
        rawGigerData: GigerInfoApiModel,
        viewModel: UserInfoBottomSheetViewModel
    ) = mutableListOf<UserInfoBottomSheetData.UserInfoActionButtonData>().apply {

        if (!rawGigerData.gigerMobile.isNullOrBlank()) {

            add(
                UserInfoBottomSheetData.UserInfoActionButtonData(
                    id = ID_CALL_GIGER,
                    icon = R.drawable.ic_call_phone_pink,
                    text = "Call",
                    viewModel = viewModel
                )
            )
        }

        add(
            UserInfoBottomSheetData.UserInfoActionButtonData(
                id = ID_CHANGE_TL,
                icon = R.drawable.ic_change_pink,
                text = "Change TL",
                viewModel = viewModel
            )
        )

        add(
            UserInfoBottomSheetData.UserInfoActionButtonData(
                id = ID_OPEN_ATTENDANCE_HISTORY,
                icon = R.drawable.ic_calendar_pink,
                text = "Att. History",
                viewModel = viewModel
            )
        )


        add(
            UserInfoBottomSheetData.UserInfoActionButtonData(
                id = ID_DISABLE_GIGER,
                icon = R.drawable.ic_block_pink,
                text = "Disable",
                viewModel = viewModel
            )
        )
    }

    private fun prepareActionButtonListForCompliance(
        rawGigerData: GigerInfoApiModel,
        viewModel: UserInfoBottomSheetViewModel
    ) = mutableListOf<UserInfoBottomSheetData.UserInfoActionButtonData>().apply {

        if (!rawGigerData.gigerMobile.isNullOrBlank()) {

            add(
                UserInfoBottomSheetData.UserInfoActionButtonData(
                    id = ID_CALL_GIGER,
                    icon = R.drawable.ic_call_phone_pink,
                    text = "Call",
                    viewModel = viewModel
                )
            )
        }

        add(
            UserInfoBottomSheetData.UserInfoActionButtonData(
                id = ID_CHANGE_TL,
                icon = R.drawable.ic_change_pink,
                text = "Change TL",
                viewModel = viewModel
            )
        )

        add(
            UserInfoBottomSheetData.UserInfoActionButtonData(
                id = ID_OPEN_ATTENDANCE_HISTORY,
                icon = R.drawable.ic_calendar_pink,
                text = "Att. History",
                viewModel = viewModel
            )
        )


        add(
            UserInfoBottomSheetData.UserInfoActionButtonData(
                id = ID_DROP_GIGER,
                icon = R.drawable.ic_block_pink,
                text = "Drop Giger",
                viewModel = viewModel
            )
        )
    }


    private fun prepareBottomCardData(
        openGigerDetailsFor: String,
        rawGigerData: GigerInfoApiModel,
        viewModel: UserInfoBottomSheetViewModel
    ): UserInfoBottomSheetData {

        val actionItems = when (openGigerDetailsFor) {
            TLWorkSpaceNavigation.PAYOUT -> prepareBottomCardDataListForPayout(
                rawGigerData,
                viewModel
            )
            TLWorkSpaceNavigation.RETENTION -> prepareBottomCardDataListForRetention(
                rawGigerData,
                viewModel
            )
            else -> prepareBottomCardDataListForComplaince(
                rawGigerData,
                viewModel
            )
        }

        return UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData(
            viewModel = viewModel,
            business = rawGigerData.businessName ?: "",
            businessIcon = rawGigerData.businessIcon,
            dataItems = actionItems
        )
    }

    private fun prepareBottomCardDataListForComplaince(
        rawGigerData: GigerInfoApiModel,
        viewModel: UserInfoBottomSheetViewModel
    ) =
        mutableListOf<UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem>().apply {

            add(
                UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem(
                    id = "client_id",
                    icon = R.drawable.ic_manager_grey_gigs,
                    key = "Client ID",
                    value = rawGigerData.clientId ?: "----",
                    actionIcon = UserInfoBottomSheetData.UserInfoActionButtonData(
                        id = ID_CHANGE_CLIENT_ID,
                        icon = R.drawable.ic_edit_pencil_pink,
                        text = "",
                        viewModel = viewModel
                    ),
                    viewModel = viewModel
                )
            )

            add(
                UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem(
                    id = "joining_date",
                    icon = R.drawable.ic_calendar_grey_gigs,
                    key = "Joining Date",
                    value = rawGigerData.getFormattedJoiningDate() ?: "----",
                    actionIcon = null,
                    viewModel = viewModel
                )
            )

            add(
                UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem(
                    id = "scout",
                    icon = R.drawable.ic_user_grey_gigs,
                    key = "Scout",
                    value = rawGigerData.scout?.name ?: "----",
                    actionIcon = if (rawGigerData.scout != null) UserInfoBottomSheetData.UserInfoActionButtonData(
                        id = ID_CALL_SCOUT,
                        icon = R.drawable.ic_call_phone_pink,
                        text = "",
                        viewModel = viewModel
                    ) else null,
                    viewModel = viewModel
                )
            )

            add(
                UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem(
                    id = "location",
                    icon = R.drawable.ic_location_grey_gigs,
                    key = "Location",
                    value = rawGigerData.location ?: "----",
                    actionIcon = null,
                    viewModel = viewModel
                )
            )

            add(
                UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem(
                    id = "job_profile",
                    icon = R.drawable.ic_breifcase_grey_gigs,
                    key = "Job Profile",
                    value = rawGigerData.jobProfile ?: "----",
                    actionIcon = null,
                    viewModel = viewModel
                )
            )
        }

    private fun prepareBottomCardDataListForRetention(
        rawGigerData: GigerInfoApiModel,
        viewModel: UserInfoBottomSheetViewModel
    ) =
        mutableListOf<UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem>().apply {

            add(
                UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem(
                    id = "job_profile",
                    icon = R.drawable.ic_breifcase_grey_gigs,
                    key = "Job Profile",
                    value = rawGigerData.jobProfile ?: "----",
                    actionIcon = null,
                    viewModel = viewModel
                )
            )

            add(
                UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem(
                    id = "joining_date",
                    icon = R.drawable.ic_calendar_grey_gigs,
                    key = "Joining Date",
                    value = rawGigerData.getFormattedJoiningDate() ?: "----",
                    actionIcon = null,
                    viewModel = viewModel
                )
            )

            add(
                UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem(
                    id = "location",
                    icon = R.drawable.ic_location_grey_gigs,
                    key = "Location",
                    value = rawGigerData.location ?: "----",
                    actionIcon = null,
                    viewModel = viewModel
                )
            )

            add(
                UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem(
                    id = "client_id",
                    icon = R.drawable.ic_manager_grey_gigs,
                    key = "Client ID",
                    value = rawGigerData.clientId ?: "----",
                    actionIcon = null,
                    viewModel = viewModel
                )
            )

            add(
                UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem(
                    id = "scout",
                    icon = R.drawable.ic_user_grey_gigs,
                    key = "Scout",
                    value = rawGigerData.scout?.name ?: "----",
                    actionIcon = null,
                    viewModel = viewModel
                )
            )
        }


    private fun prepareBottomCardDataListForPayout(
        rawGigerData: GigerInfoApiModel,
        viewModel: UserInfoBottomSheetViewModel
    ) =
        mutableListOf<UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem>().apply {

            add(
                UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem(
                    id = "amount",
                    icon = R.drawable.ic_wallet_grey,
                    key = "Amount",
                    value = rawGigerData.payoutInformation?.amount?.formatToCurrency("----")
                        ?: "----",
                    actionIcon = null,
                    viewModel = viewModel
                )
            )

            add(
                UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem(
                    id = "payment_cycle",
                    icon = R.drawable.ic_refresh_grey,
                    key = "Payment Cycle",
                    value = rawGigerData.payoutInformation?.payOutCycle ?: "----",
                    actionIcon = null,
                    viewModel = viewModel
                )
            )

            add(
                UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem(
                    id = "paid_on",
                    icon = R.drawable.ic_calendar_grey,
                    key = "Paid On",
                    value = rawGigerData.payoutInformation?.getPaidOnDateString() ?: "----",
                    actionIcon = null,
                    viewModel = viewModel
                )
            )


            add(
                UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem(
                    id = "category",
                    icon = R.drawable.ic_menu_grey,
                    key = "Category",
                    value = rawGigerData.payoutInformation?.category ?: "----",
                    actionIcon = null,
                    viewModel = viewModel
                )
            )

            add(
                UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem(
                    id = "job_profile",
                    icon = R.drawable.ic_breifcase_grey_gigs,
                    key = "Job Profile",
                    value = rawGigerData.jobProfile ?: "----",
                    actionIcon = null,
                    viewModel = viewModel
                )
            )
        }
}