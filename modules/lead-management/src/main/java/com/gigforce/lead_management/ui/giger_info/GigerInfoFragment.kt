package com.gigforce.lead_management.ui.giger_info


import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.CommonIntentExtras
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.common_ui.viewdatamodels.leadManagement.ChangeTeamLeaderRequestItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.GigerInfo
import com.gigforce.core.AppConstants
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.gigforce.common_ui.navigation.LeadManagementConstants
import com.gigforce.lead_management.R
import com.gigforce.lead_management.analytics.LeadManagementAnalyticsEvents
import com.gigforce.lead_management.databinding.GigerInfoFragmentBinding
import com.gigforce.lead_management.models.ApplicationChecklistRecyclerItemData
import com.gigforce.common_ui.viewdatamodels.leadManagement.DropScreenIntentModel
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.gigforce.lead_management.ui.LeadManagementSharedViewModelState
import com.gigforce.lead_management.ui.changing_tl.ChangeTeamLeaderBottomSheetFragment
import com.gigforce.lead_management.ui.drop_selection_2.DropSelectionFragment2
import com.gigforce.lead_management.ui.giger_info.views.AppCheckListRecyclerComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class GigerInfoFragment : BaseFragment2<GigerInfoFragmentBinding>(
    fragmentName = "GigerInfoFragment",
    layoutId = R.layout.giger_info_fragment,
    statusBarColor = R.color.lipstick_2
) {

    companion object {
        fun newInstance() = GigerInfoFragment()
        private const val TAG = "GigerInfoFragment"
        val UPLOAD_PROFILE_PIC = 1
    }

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var buildConfig: IBuildConfig

    @Inject
    lateinit var eventTracker: IEventTracker

    private val viewModel: GigerInfoViewModel by viewModels()
    private val sharedViewModel: LeadManagementSharedViewModel by activityViewModels()

    var gigerPhone = ""

    private var joiningId: String? =null
    private var gigId: String? =null
    private lateinit var gigerUid: String

    var hasStartEndDate = false
    var dropScreenIntentModel: DropScreenIntentModel? = DropScreenIntentModel("", "",false, false, "", "", "")


    override fun viewCreated(viewBinding: GigerInfoFragmentBinding, savedInstanceState: Bundle?) {
        getDataFrom(arguments, savedInstanceState)
        initToolbar(viewBinding)
        initViews()
        initListeners()
        initViewModel()
        checkForDropSelection()
        initSharedViewModel()
    }

    private fun initViews() {
        joiningId?.let {
            dropScreenIntentModel?.joiningId = it
        }
        gigId?.let {
            dropScreenIntentModel?.gigId = it
        }
    }

    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {
        arguments?.let {
            gigerUid = it.getString(CommonIntentExtras.INTENT_USER_ID) ?: return@let
            joiningId = it.getString(LeadManagementConstants.INTENT_EXTRA_JOINING_ID)
            gigId = it.getString(LeadManagementConstants.INTENT_EXTRA_GIG_ID)
        }

        savedInstanceState?.let {
            gigerUid = it.getString(CommonIntentExtras.INTENT_USER_ID) ?: return@let
            joiningId = it.getString(LeadManagementConstants.INTENT_EXTRA_JOINING_ID)
            gigId = it.getString(LeadManagementConstants.INTENT_EXTRA_GIG_ID)
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_GIG_ID, gigId)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_JOINING_ID, joiningId)
        outState.putString(CommonIntentExtras.INTENT_USER_ID, gigerUid)
    }


    private fun checkForDropSelection() {
//        val navController = findNavController()
//        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("drop_status")?.observe(
//            viewLifecycleOwner) { result ->
//            if (result == "dropped"){
//                Log.d("droppedInfo","dropped")
//                navigation.popBackStack()
//            }
//        }
        childFragmentManager.setFragmentResultListener(
            "drop_status",
            viewLifecycleOwner
        ) { key, bundle ->
            val result = bundle.getString("drop_status")
            // Do something with the result
            if (result == "dropped") {
                Log.d("droppedInfo", "dropped")
                navigation.popBackStack()
            }
        }

    }

    private fun initViewModel() {
        viewModel.getGigerJoiningInfo(
            joiningId,
            gigId
        )

        //observe data
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            val state = it ?: return@Observer
            when (state) {

                is GigerInfoState.ErrorLoadingData -> showErrorLoadingInfo(
                    state.error
                )
                is GigerInfoState.GigerInfoLoaded -> showGigerInfo(state.gigerInfo)

                GigerInfoState.LoadingDataFromServer -> showLoadingInfo()
            }
        })
    }

    private fun initSharedViewModel() {

        lifecycleScope.launchWhenCreated {

            sharedViewModel.viewStateFlow.collect {
                when (it) {
                    is LeadManagementSharedViewModelState.JoiningsUpdated -> {
                        viewModel.getGigerJoiningInfo(joiningId,gigId)
                    }
                }
            }
        }

        lifecycleScope.launchWhenCreated {

            viewModel.viewEffects.collect {
                when(it){
                    is GigerInfoEffects.OpenChangeTeamLeaderScreen -> openChangeTeamLeaderScreen(
                        it.joiningId,
                        it.gigerId,
                        it.gigerName,
                        it.teamLeaderId,
                        gigId
                    )
                }
            }
        }
    }

    private fun openChangeTeamLeaderScreen(
        joiningId : String?,
        gigerId : String?,
        gigerName : String?,
        teamLeaderId : String?,
        gigId : String?
    ) {
        ChangeTeamLeaderBottomSheetFragment.launch(
            arrayListOf(
                ChangeTeamLeaderRequestItem(
                    gigerUid = gigerId,
                    joiningId = joiningId,
                    gigerName = gigerName,
                    teamLeaderId = teamLeaderId,
                    gigId = gigId,
                    jobProfileId = null
                )
            ),
            childFragmentManager
        )
    }

    private fun showLoadingInfo() = viewBinding.apply {
        checklistLayout.removeAllViews()
        mainScrollView.gone()
        gigerinfoShimmerContainer.visible()

        startShimmer(
            this.gigerinfoShimmerContainer as LinearLayout,
            ShimmerDataModel(
                minHeight = R.dimen.size_120,
                minWidth = LinearLayout.LayoutParams.MATCH_PARENT,
                marginRight = R.dimen.size_16,
                marginTop = R.dimen.size_1,
                orientation = LinearLayout.VERTICAL
            ),
            R.id.shimmer_controller
        )
    }

    private fun showGigerInfo(gigerInfo: GigerInfo) = viewBinding.apply {
        stopShimmer(
            gigerinfoShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        gigerinfoShimmerContainer.gone()
        mainScrollView.visible()


        gigerInfo.let {

            viewBinding.bottomButtonLayout.dropGigerBtn.isVisible = it.isActive
            toolbar.showTitle(it.gigerName)
            toolbar.setTitleTypeface(Typeface.BOLD)
            overlayCardLayout.companyName.text = ": " + it.businessName ?: ""
            overlayCardLayout.jobProfileTitle.text = it.jobProfileTitle.capitalize() ?: ""
            val reportingLocText =
                if (!it.reportingLocation.isNullOrBlank() && it.reportingLocation != "null") it.reportingLocation + ", " else ""
            val businessLocText =
                if (!it.businessLocation.isNullOrBlank() && it.businessLocation != "null") it.businessLocation else ""
            overlayCardLayout.locationText.text = ": " + reportingLocText + businessLocText ?: ""

            gigerPhone = it.gigerPhone.toString()
            toolbar.showSubtitle(gigerPhone)
            setGigerProfilePicture(it.gigerProfilePicture.toString())

            context?.let { it1 ->
                GlideApp.with(it1)
                    .load(it.businessLogo)
                    .placeholder(getCircularProgressDrawable(it1))
                    .into(overlayCardLayout.profileImageOverlay.companyImg)
            }

            applicationStatusLayout.statusText.text =
                getString(R.string.application_lead, it.status)
            if (it.status == "Pending") {
                applicationStatusLayout.statusIconImg.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_pending_icon,
                        null
                    )
                )
                applicationStatusLayout.root.setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.status_background_pink,
                        null
                    )
                )
            } else {
                applicationStatusLayout.statusIconImg.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_blue_tick,
                        null
                    )
                )
                applicationStatusLayout.root.setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.status_background_blue,
                        null
                    )
                )
            }


            overlayCardLayout.selectionDate.text = ": " + getFormattedDate(it.selectionDate)
            overlayCardLayout.joiningDate.text =
                ": " + getFormattedDateFromYYMMDD(it.joiningDate) ?: ""

            //check for gigStartDate and gigEndDate
            if (!it.gigStartDate.isNullOrBlank() && !it.gigEndDate.isNullOrBlank()) {
                hasStartEndDate = true
                dropScreenIntentModel?.gigStartDate = it.gigStartDate
                dropScreenIntentModel?.gigEndDate = it.gigEndDate
                dropScreenIntentModel?.hasStartEndDate = true
                dropScreenIntentModel?.gigId = gigId
            }
            if (!it.currentDate.isNullOrBlank()) {
                dropScreenIntentModel?.currentDate = it.currentDate
            }

            overlayCardLayout.reportingTlTv.text = if(it.reportingTeamLeader?.name != null)
               ": " + it.reportingTeamLeader?.name
            else
                "-"

            overlayCardLayout.recrutingTlTv.text = if(it.recruitingTL?.name != null)
                ": " + it.recruitingTL?.name
            else
                "-"

            val checkListItemData =
                arrayListOf<ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData>()
            if (it.checkList == null) {
                checkListTextview.gone()
                checklistLayout.gone()
            } else {
                it.checkList?.let { it1 ->
                    it1.forEachIndexed { index, checkListItem ->
                        if (checkListItem.type == "bank_account" && checkListItem.status == "Completed") {
                            dropScreenIntentModel?.isBankVerified = true
                        }
                        val itemData =
                            ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData(
                                checkListItem.name,
                                it.gigerId.toString(),
                                it.gigerName,
                                checkListItem.status,
                                checkListItem.type,
                                checkListItem.optional,
                                checkListItem.frontImage,
                                checkListItem.backImage,
                                checkListItem.type,
                                checkListItem.dependency,
                                checkListItem.courseId,
                                checkListItem.moduleId,
                                checkListItem.title
                            )
                        checkListItemData.add(itemData)
                        if (checkListItemData.size > 0) {
                            checklistLayout.visible()
                            inflateCheckListInCheckListContainer(checkListItemData)
                        } else {
                            checkListTextview.gone()
                            checklistLayout.gone()
                        }

                    }
                }
                stopShimmer(
                    gigerinfoShimmerContainer as LinearLayout,
                    R.id.shimmer_controller
                )
                gigerinfoShimmerContainer.gone()

            }
        }
    }

    private fun inflateCheckListInCheckListContainer(
        checkListItemData: ArrayList<ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData>
    ) = viewBinding.checklistLayout.apply {
        removeAllViews()

        checkListItemData.forEach {
            if (it.gigerUid == "null")
                it.gigerUid = null
            val view = AppCheckListRecyclerComponent(requireContext(), null)
            addView(view)
            view.bind(it)
            val viewPhotoText = view.findViewById<TextView>(R.id.viewPhotoText)
            viewPhotoText.setOnClickListener { it1 ->
                if (viewPhotoText.text == "ADD") {
                    var bundleForFragment = bundleOf(
                        AppConstants.INTENT_EXTRA_USER_CAME_FROM_ONBOARDING_FORM to true,
                        "uid" to it.gigerUid
                    )
                    val navString = getNavigationStr(it.docType)
                    navigation.navigateTo(navString, bundleForFragment)
                } else if (viewPhotoText.text == "VIEW PHOTO") {
                    val arrayList = arrayListOf<String>()
                    if (!it.frontImage.isNullOrBlank()) {
                        arrayList.add(getDBImageUrl(it.frontImage.toString()).toString())
                    }
                    if (!it.backImage.isNullOrBlank()) {
                        arrayList.add(getDBImageUrl(it.backImage.toString()).toString())
                    }

                    navigation.navigateTo(
                        "LeadMgmt/showDocImages",
                        bundleOf(
                            ShowCheckListDocsBottomSheet.INTENT_TOP_TITLE to it.checkName,
                            ShowCheckListDocsBottomSheet.INTENT_IMAGES_TO_SHOW to arrayList
                        )
                    )
                }
            }
        }
    }

    private fun showErrorLoadingInfo(error: String) = viewBinding.apply {
        checklistLayout.removeAllViews()
        toolbar.showTitle("")
        stopShimmer(
            gigerinfoShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        gigerinfoShimmerContainer.gone()
        mainScrollView.gone()
        bottomButtonLayout.root.gone()
        joiningInfoErrorInfoLayout.root.visible()
        joiningInfoErrorInfoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        joiningInfoErrorInfoLayout.infoMessageTv.text = error

    }

    private fun initListeners() = viewBinding.apply {
        bottomButtonLayout.dropGigerBtn.setOnClickListener {
            //drop functionality
            DropSelectionFragment2.launch(
                arrayListOf<DropScreenIntentModel>(dropScreenIntentModel!!),
                childFragmentManager
            )
        }

        this.overlayCardLayout.editReportingTlImageview.setOnClickListener {
            viewModel.openChangeTeamLeaderScreen()
        }

        bottomButtonLayout.callLayout.setOnClickListener {
            //call functionality
            eventTracker.pushEvent(
                TrackingEventArgs(
                    LeadManagementAnalyticsEvents.Onboarded_Giger_Calling,
                    mapOf(
                        "Phone_number_called" to gigerPhone
                    )
                )
            )
            eventTracker.pushEvent(
                TrackingEventArgs(
                    "tl_call_giger", null
                )
            )
            val intent =
                Intent(
                    Intent.ACTION_DIAL,
                    Uri.fromParts("tel", gigerPhone, null)
                )
            context?.startActivity(intent)

        }

        this.overlayCardLayout.viewMore.setOnClickListener {

            if(this.overlayCardLayout.moreInfoLayout.isVisible){
                this.overlayCardLayout.moreInfoLayout.gone()
                this.overlayCardLayout.viewMore.text = "View MOre"
            } else{
                this.overlayCardLayout.moreInfoLayout.visible()
                this.overlayCardLayout.viewMore.text = "View Less"
            }
        }


    }

    private fun initToolbar(viewBinding: GigerInfoFragmentBinding) = viewBinding.toolbar.apply {
        this.showTitle("Giger name")
        this.hideActionMenu()
        this.changeBackButtonDrawable()
        this.setBackButtonListener(View.OnClickListener {
            //back functionality
            activity?.onBackPressed()
        })
    }


    fun setGigerProfilePicture(path: String) {
        if (!path.isBlank()) {

            if (path.isEmpty() || path == "avatar.jpg") {
                viewBinding.overlayCardLayout.profileImageOverlay.gigerImg.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_avatar_male, null)
                )
                return
            }

            val userPathInFirebase = if (path.startsWith("profile_pics/"))
                path
            else
                "profile_pics/${path}"

            viewBinding.overlayCardLayout.profileImageOverlay.gigerImg.loadImageIfUrlElseTryFirebaseStorage(
                userPathInFirebase,
                R.drawable.ic_avatar_male,
                R.drawable.ic_avatar_male
            )
        } else {
            viewBinding.overlayCardLayout.profileImageOverlay.gigerImg.loadImage(R.drawable.ic_avatar_male)
        }
    }


    fun getFormattedDate(date: String): String {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val output = SimpleDateFormat("dd/MMM/yy", Locale.getDefault())

        var d: Date? = null
        try {
            d = input.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val formatted = output.format(d)
        return formatted ?: ""
    }

    fun getFormattedDateFromYYMMDD(date: String): String {
        val input = SimpleDateFormat("yyyy-MM-dd")
        val output = SimpleDateFormat("dd/MMM/yy", Locale.getDefault())

        var d: Date? = null
        try {
            d = input.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val formatted = output.format(d)
        return formatted ?: ""
    }


    private fun getNavigationStr(data: String): String {
        when (data) {
            "profile_pic" -> {
                return "userinfo/addProfilePictureFragment"
            }
            "about_me" -> {
                return "profile/addBio"
            }
            "questionnaire" -> {
                return "learning/questionnair"
            }
            "driving_licence" -> {
                return "verification/drivinglicenseimageupload"
            }
            "learning" -> {
                return "learning/coursedetails"
            }
            "aadhar_card" -> {
                return "verification/AadharDetailInfoFragment"
            }
            "pan_card" -> {
                return "verification/pancardimageupload"
            }
            "bank_account" -> {
                return "verification/bank_account_fragment"
            }
            "aadhar_card_questionnaire" -> {
                return "verification/AadharDetailInfoFragment"
            }
            "jp_hub_location" -> {
                return "client_activation/fragment_business_loc_hub"
            }
            "pf_esic" -> {
                return "client_activation/pfesicFragment"
            }
            else -> return ""
        }
    }

    fun getDBImageUrl(imagePath: String): String? {
        if (imagePath.isNotBlank()) {
            try {
                var modifiedString = imagePath
                if (!imagePath.startsWith("/"))
                    modifiedString = "/$imagePath"
                return buildConfig.getStorageBaseUrl() + modifiedString
            } catch (e: Exception) {
                return null
            }
        }
        return null
    }

}