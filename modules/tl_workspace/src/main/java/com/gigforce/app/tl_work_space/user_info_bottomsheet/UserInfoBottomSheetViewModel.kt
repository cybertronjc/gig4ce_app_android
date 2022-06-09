package com.gigforce.app.tl_work_space.user_info_bottomsheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gigforce.app.data.repositoriesImpl.tl_workspace.user_info.GigerInfoApiModel
import com.gigforce.app.data.repositoriesImpl.tl_workspace.user_info.UserInfoRepository
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.app.tl_work_space.BaseTLWorkSpaceViewModel
import com.gigforce.app.tl_work_space.user_info_bottomsheet.models.UserInfoBottomSheetData
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class UserInfoBottomSheetViewModel @Inject constructor(
    private val logger: GigforceLogger,
    private val savedStateHandle: SavedStateHandle,
    private val repository: UserInfoRepository,
    private val firebaseAuthStateListener: FirebaseAuthStateListener
) : BaseTLWorkSpaceViewModel<
        GigerInformationDetailsBottomSheetFragmentViewEvents,
        GigerInformationDetailsBottomSheetFragmentViewState,
        GigerInformationDetailsBottomSheetFragmentViewEffects>(initialState = GigerInformationDetailsBottomSheetFragmentViewState.LoadingGigerInformation) {

    companion object {
        const val TAG = "UserInfoBottomSheetViewModel"
    }

    private lateinit var openGigerDetailsFor: String
    private lateinit var gigerId: String
    private lateinit var jobProfileId: String
    private lateinit var businessId: String
    private var payoutId: String? = null

    // Raw Info
    private var rawUserInfo: GigerInfoApiModel? = null
    private var userInfoViewShownOnView: List<UserInfoBottomSheetData> = emptyList()

    init {
        tryRestoringKeys()
    }


    private fun tryRestoringKeys() {
        gigerId = savedStateHandle.get<String?>(
            TLWorkSpaceNavigation.INTENT_OPEN_USER_DETAILS_OF
        ) ?: return
        jobProfileId = savedStateHandle.get<String?>(
            TLWorkSpaceNavigation.INTENT_EXTRA_JOB_PROFILE_ID
        ) ?: return
        businessId = savedStateHandle.get<String?>(
            TLWorkSpaceNavigation.INTENT_EXTRA_BUSINESS_ID
        ) ?: return
        payoutId = savedStateHandle.get<String?>(
            TLWorkSpaceNavigation.INTENT_EXTRA_PAYOUT_ID
        )

    }


    fun setKeysReceivedFromPreviousScreen(
        openDetailsFor: String,
        gigerId: String,
        businessId: String,
        jobProfileId: String,
        payoutId: String?
    ) {
        this.openGigerDetailsFor = openDetailsFor
        this.gigerId = gigerId
        this.businessId = businessId
        this.jobProfileId = jobProfileId
        this.payoutId = payoutId

        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_OPEN_USER_DETAILS_OF,
            openGigerDetailsFor
        )
        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_EXTRA_JOB_PROFILE_ID,
            jobProfileId
        )
        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_EXTRA_BUSINESS_ID,
            businessId
        )
        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_EXTRA_PAYOUT_ID,
            payoutId
        )

        fetchUserDetails()
    }

    private fun fetchUserDetails() = viewModelScope.launch {

        setState {
            GigerInformationDetailsBottomSheetFragmentViewState.LoadingGigerInformation
        }

        try {

            rawUserInfo = repository.getUserInfo(
                fetchInfoFor = openGigerDetailsFor,
                gigerId = gigerId,
                jobProfileId = jobProfileId,
                businessId = businessId,
                payoutId = payoutId
            )

            userInfoViewShownOnView =
                UserInfoScreenRawDataToPresentationDataMapper.prepareUserInfoSections(
                    openGigerDetailsFor = openGigerDetailsFor,
                    rawGigerData = rawUserInfo!!,
                    viewModel = this@UserInfoBottomSheetViewModel
                )

            setState {
                GigerInformationDetailsBottomSheetFragmentViewState.ShowGigerInformation(
                    viewItems = userInfoViewShownOnView
                )
            }
        } catch (e: Exception) {

            if (e is IOException) {
                setState {
                    GigerInformationDetailsBottomSheetFragmentViewState.ErrorWhileFetchingGigerInformation(
                        e.message ?: "Unable to load data"
                    )
                }
            } else {

                setState {
                    GigerInformationDetailsBottomSheetFragmentViewState.ErrorWhileFetchingGigerInformation(
                        "Unable to load data"
                    )
                }
            }
        }
    }


    override fun handleEvent(
        event: GigerInformationDetailsBottomSheetFragmentViewEvents
    ) {
        when (event) {
            is GigerInformationDetailsBottomSheetFragmentViewEvents.ActionButtonClicked -> handleActionButtonClick(
                event.actionButton
            )
        }
    }

    private fun handleActionButtonClick(
        actionButtonClicked: UserInfoBottomSheetData.UserInfoActionButtonData
    ) {

        when (actionButtonClicked.id) {
            UserInfoScreenRawDataToPresentationDataMapper.ID_CALL_SCOUT -> callScout()
            UserInfoScreenRawDataToPresentationDataMapper.ID_CHANGE_CLIENT_ID -> changeClientId()
            UserInfoScreenRawDataToPresentationDataMapper.ID_DROP_GIGER -> dropGiger()
            UserInfoScreenRawDataToPresentationDataMapper.ID_DOWNLOAD_PAYSLIPS -> downloadPaySlip()
            UserInfoScreenRawDataToPresentationDataMapper.ID_CALL_GIGER -> callGiger()
            UserInfoScreenRawDataToPresentationDataMapper.ID_CHANGE_TL -> changeTeamLeader()
            UserInfoScreenRawDataToPresentationDataMapper.ID_OPEN_ATTENDANCE_HISTORY -> navigateToAttendanceHistory()
            UserInfoScreenRawDataToPresentationDataMapper.ID_DISABLE_GIGER -> dropGiger()
            else -> {}
        }
    }

    private fun changeTeamLeader() {

        setEffect {
            GigerInformationDetailsBottomSheetFragmentViewEffects.OpenChangeTeamLeaderScreen(
                gigerId = rawUserInfo?.gigerId!!,
                jobProfileId = rawUserInfo?.jobProfileId!!,
                gigerName = rawUserInfo?.gigerName!!,
                teamLeaderUid = firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid
            )
        }
    }

    private fun navigateToAttendanceHistory() {

        setEffect {
            GigerInformationDetailsBottomSheetFragmentViewEffects.OpenMonthlyAttendanceScreen(
                gigerId = rawUserInfo?.gigerId!!,
                jobProfileId = rawUserInfo?.jobProfileId!!,
                gigDate = LocalDate.now(),
                gigTitle = rawUserInfo?.jobProfile!!,
                companyLogo = rawUserInfo?.businessIcon,
                companyName = rawUserInfo?.businessName!!
            )
        }
    }

    private fun changeClientId() {
        if (rawUserInfo?.jobProfileId.isNullOrBlank()) {
            logger.d(
                TAG,
                "ignoring dropGiger call ,as pdf url  : '${rawUserInfo?.payoutInformation?.pdfUrl}', is null or blank"
            )
            return
        }

        setEffect {
            GigerInformationDetailsBottomSheetFragmentViewEffects.OpenChangeClientIdBottomSheet(
                existingClientId = rawUserInfo?.clientId ?: "",
                gigerId = rawUserInfo?.gigerId!!,
                gigerMobile = rawUserInfo?.gigerMobile!!,
                gigerName = rawUserInfo?.gigerName!!,
                jobProfileId = rawUserInfo?.jobProfileId!!,
                jobProfileName = rawUserInfo?.jobProfile!!,
                businessId = rawUserInfo?.businessId!!
            )
        }
    }

    private fun dropGiger() {
        if (rawUserInfo?.jobProfileId.isNullOrBlank()) {
            logger.d(
                TAG,
                "ignoring dropGiger call ,as pdf url  : '${rawUserInfo?.payoutInformation?.pdfUrl}', is null or blank"
            )
            return
        }

        setEffect {
            GigerInformationDetailsBottomSheetFragmentViewEffects.DropGiger(
                jobProfileId = rawUserInfo?.jobProfileId!!,
                gigerId = rawUserInfo?.gigerId!!
            )
        }
    }

    private fun downloadPaySlip() {
        if (rawUserInfo?.payoutInformation?.pdfUrl.isNullOrBlank()) {
            logger.d(
                TAG,
                "ignoring downloadPaySlip call ,as pdf url  : '${rawUserInfo?.payoutInformation?.pdfUrl}', is null or blank"
            )
            return
        }

        setEffect {
            GigerInformationDetailsBottomSheetFragmentViewEffects.DownloadPayslip(
                businessName = rawUserInfo?.businessName ?: "",
                payslipUrl = rawUserInfo?.payoutInformation?.pdfUrl!!
            )
        }
    }

    private fun callScout() {
        if (rawUserInfo?.scout?.mobile.isNullOrBlank()) {
            logger.d(
                TAG,
                "ignoring call scout ,as scout mobile : '${rawUserInfo?.scout?.mobile}', is null or blank"
            )
            return
        }

        setEffect {
            GigerInformationDetailsBottomSheetFragmentViewEffects.CallPhoneNumber(
                phoneNumber = rawUserInfo?.scout?.mobile!!
            )
        }
    }

    private fun callGiger() {
        if (rawUserInfo?.gigerMobile.isNullOrBlank()) {
            logger.d(
                TAG,
                "ignoring call giger ,as gigerMobile : '${rawUserInfo?.gigerMobile}', is null or blank"
            )
            return
        }

        setEffect {
            GigerInformationDetailsBottomSheetFragmentViewEffects.CallPhoneNumber(
                phoneNumber = rawUserInfo?.gigerMobile!!
            )
        }
    }

    override fun clientIdChanged(newClientId: String) {
        rawUserInfo?.clientId = newClientId
        //Try to update only requried view

        userInfoViewShownOnView =
            UserInfoScreenRawDataToPresentationDataMapper.prepareUserInfoSections(
                openGigerDetailsFor = openGigerDetailsFor,
                rawGigerData = rawUserInfo!!,
                viewModel = this@UserInfoBottomSheetViewModel
            )

        setState {
            GigerInformationDetailsBottomSheetFragmentViewState.ShowGigerInformation(
                viewItems = userInfoViewShownOnView
            )
        }
    }
}