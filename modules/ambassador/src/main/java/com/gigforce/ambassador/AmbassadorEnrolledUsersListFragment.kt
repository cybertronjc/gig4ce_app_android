package com.gigforce.ambassador

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.ambassador.user_rollment.verify_mobile.ConfirmOtpFragment
import com.gigforce.ambassador.user_rollment.verify_mobile.EditProfileConsentAndSendOtpDialogFragment
import com.gigforce.ambassador.user_rollment.verify_mobile.UserDetailsFilledDialogFragmentResultListener
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.components.atoms.ChipComponent
import com.gigforce.common_ui.components.atoms.ChipGroupComponent
import com.gigforce.common_ui.components.atoms.models.ChipGroupModel
import com.gigforce.common_ui.components.cells.SearchTextChangeListener
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.datamodels.GigerVerificationStatus
import com.gigforce.common_ui.decors.VerticalItemDecorator
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.listeners.AppBarClicks
import com.gigforce.common_ui.utils.LocationUpdates
import com.gigforce.common_ui.viewmodels.GigVerificationViewModel
import com.gigforce.common_ui.viewmodels.ProfileViewModel
import com.gigforce.common_ui.views.GigforceToolbar
import com.gigforce.core.AppConstants
import com.gigforce.core.datamodels.ambassador.EnrolledUser
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.PermissionUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_embassador_enrolled_users_list.*
import javax.inject.Inject

@AndroidEntryPoint
class AmbassadorEnrolledUsersListFragment : Fragment(),
    IOnBackPressedOverride,
    EnrolledUsersRecyclerAdapter.EnrolledUsersRecyclerAdapterClickListener,
    LocationUpdates.LocationUpdateCallbacks, UserDetailsFilledDialogFragmentResultListener {
    private val locationUpdates: LocationUpdates by lazy {
        LocationUpdates()
    }

    private val viewModel: AmbassadorEnrollViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val gigVerificationViewModel: GigVerificationViewModel by viewModels()

    private val completedItems = LinkedHashMap<String, Boolean>()
    private var redirectToNextStep = false

    private var location: Location? = null
    private var profileData: ProfileData? = null
    private var gigerVerificationStatus: GigerVerificationStatus? = null

    @Inject lateinit var navigation : INavigation
    @Inject lateinit var buildConfig:IBuildConfig
    private val enrolledUserAdapter: EnrolledUsersRecyclerAdapter by lazy {
        EnrolledUsersRecyclerAdapter(requireContext()).apply {
            this.setListener(this@AmbassadorEnrolledUsersListFragment)
        }
    }

    var isEditingDetails = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_embassador_enrolled_users_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initViewModel()

        startListeningForGigerVerificationStatusChanges()
    }

    private fun initUi() {

        bank_details_layout.setOnClickListener {
            redirectToNextStep = true
            isEditingDetails = true
            navigation.navigateTo("userinfo/addBankDetailsInfoFragment",bundleOf(
                AppConstants.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT to true
            ))
//            navigate(
//                R.id.addBankDetailsInfoFragment, bundleOf(
//                    AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT to true
//                )
//            )

        }

        current_address_layout.setOnClickListener {
            redirectToNextStep = true
            isEditingDetails = true
            navigation.navigateTo("userinfo/addCurrentAddressFragment",bundleOf(
                AppConstants.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT to true
            ))
//            navigate(
//                R.id.addCurrentAddressFragment, bundleOf(
//                    AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT to true
//                )
//            )


        }

        profile_photo_layout.setOnClickListener {
            redirectToNextStep = true
            isEditingDetails = true
            navigation.navigateTo("userinfo/addProfilePictureFragment",bundleOf(
                EnrollmentConstants.INTENT_EXTRA_MODE to EnrollmentConstants.MODE_ENROLLMENT_REQUIREMENT,
                AppConstants.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT to true
            ))
//            navigate(
//                R.id.addProfilePictureFragment, bundleOf(
//                    EnrollmentConstants.INTENT_EXTRA_MODE to EnrollmentConstants.MODE_ENROLLMENT_REQUIREMENT,
//                    AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT to true
//                )
//            )

        }

        toolbar_layout.apply {
            showTitle("Gigforce Ambassador")
            hideActionMenu()
            showSearchOption("Search Users")

            setOnSearchTextChangeListener(object : GigforceToolbar.SearchTextChangeListener {

                override fun onSearchTextChanged(text: String) {
                    enrolledUserAdapter.filter.filter(text)
                }
            })

            setBackButtonListener(View.OnClickListener {
//                if (toolbar_layout.isSearchCurrentlyShown) {
//                    hideSoftKeyboard()
//                    enrolledUserAdapter.filter.filter("")
//                } else {
//                    //activity?.onBackPressed()
//                    navigation.popBackStack()
//                }
                activity?.onBackPressed()
            })
        }

        appBar.apply {
            setOnSearchClickListener(object : AppBarClicks.OnSearchClickListener{
                override fun onSearchClick(v: View) {
                    enrolledUserAdapter.filter.filter("")
                }

            })
            setOnSearchTextChangeListener(object : SearchTextChangeListener{
                override fun onSearchTextChanged(text: String) {
                    enrolledUserAdapter.filter.filter(text)
                }

            })
            setBackButtonListener(View.OnClickListener {
                activity?.onBackPressed()
            })
        }

        testingchipgrp.addChips(viewModel.getChipsData(), isSingleSelection = true, true)
        testingchipgrp.setOnCheckedChangeListener(object : ChipGroupComponent.OnCustomCheckedChangeListener{
            override fun onCheckedChangeListener(model: ChipGroupModel) {
                if (model.chipId == 0) {
                    //hide chip
                    user_details_layout.gone()
                    enrolled_users_rv.visible()
                    toolbar_layout.showSearchOption("Search User")
                    toolbar_layout.hideSubTitle()

                    if (enrolledUserAdapter.itemCount != 0) {
                        createProfileBtn.visible()
                        share_link.visible()
                        no_users_enrolled_layout.gone()
                    } else {
                        createProfileBtn.gone()
                        share_link.gone()
                        no_users_enrolled_layout.visible()
                    }
                } else if (model.chipId == 1) {

                    //hide
                    no_users_enrolled_layout.gone()
                    enrolled_users_rv.gone()
                    toolbar_layout.hideSearchOption()
                    toolbar_layout.hideSubTitle()
                    createProfileBtn.gone()
                    share_link.gone()
                    user_details_layout.visible()
                }
            }

        })

        enrolled_user_chipgroup.setOnCheckedChangeListener { group, checkedId ->

            if (checkedId == R.id.chip_profile) {
                //hide chip

                user_details_layout.gone()
                enrolled_users_rv.visible()
                toolbar_layout.showSearchOption("Search User")
                toolbar_layout.hideSubTitle()

                if (enrolledUserAdapter.itemCount != 0) {
                    createProfileBtn.visible()
                    share_link.visible()
                    no_users_enrolled_layout.gone()
                } else {
                    createProfileBtn.gone()
                    share_link.gone()
                    no_users_enrolled_layout.visible()
                }
            } else if (checkedId == R.id.chip_my_details) {

                //hide
                no_users_enrolled_layout.gone()
                enrolled_users_rv.gone()
                toolbar_layout.hideSearchOption()
                toolbar_layout.hideSubTitle()
                createProfileBtn.gone()
                share_link.gone()
                user_details_layout.visible()
            }
        }

        create_profile_btn.setOnClickListener {


            navigation.navigateTo("LeadMgmt/gigerOnboarding")
//            navigate(R.id.checkMobileFragment)
            isEditingDetails = false
            //navigate(R.id.checkMobileFragment)
        }

        createProfileBtn.setOnClickListener {
            navigation.navigateTo("LeadMgmt/gigerOnboarding")
//            navigate(R.id.checkMobileFragment)
            isEditingDetails = false
            //navigate(R.id.checkMobileFragment)
        }

        enrolled_users_rv.layoutManager = LinearLayoutManager(activity?.applicationContext)
        enrolled_users_rv.addItemDecoration(
            VerticalItemDecorator(
                30
            )
        )
        enrolled_users_rv.adapter = enrolledUserAdapter

        share_link.setOnClickListener {
            isEditingDetails = false
            shareLink()
        }
        share_link_cl.setOnClickListener {
            isEditingDetails = false
            shareLink()
        }
    }

    private fun initViewModel() {
        viewModel.enrolledUsers
            .observe(viewLifecycleOwner, Observer {
//                    it ?: return@Observer

                if (it.isEmpty()) {
                    enrolledUserAdapter.setData(emptyList())
                    if (!isEditingDetails){
                        Log.d("here", "prpfile selected")
                        no_users_enrolled_layout.visible()
                        createProfileBtn.gone()
                        share_link.gone()
                        total_complete_profile_tv.gone()
                        total_incomplete_profile_tv.gone()
                    }
                } else {
                    enrolledUserAdapter.setData(it)
                    if (!isEditingDetails){
                        Log.d("here", "prpfile selected and data is there")
                        no_users_enrolled_layout.gone()
                        createProfileBtn.visible()
                        share_link.visible()

                    }
                    total_complete_profile_tv.visible()
                    total_incomplete_profile_tv.visible()


                    val totalCompleteProfiles =
                        it.count { it.enrollmentStepsCompleted.allStepsCompleted() }
                    val totalInCompleteProfiles =
                        it.count { it.enrollmentStepsCompleted.allStepsCompleted().not() }

                    total_complete_profile_tv.text = buildSpannedString {
                        append("Total Completed Profile : ")
                        bold {
                            color(
                                ResourcesCompat.getColor(
                                    resources,
                                    R.color.activated_color,
                                    null
                                )
                            ) {
                                append(totalCompleteProfiles.toString())
                            }
                        }
                    }


                    total_incomplete_profile_tv.text = buildSpannedString {
                        append("Total Incomplete Profile : ")
                        bold {
                            color(ResourcesCompat.getColor(resources, R.color.text_orange, null)) {
                                append(totalInCompleteProfiles.toString())
                            }
                        }
                    }
                }
            })

        profileViewModel.getProfileData()
            .observe(viewLifecycleOwner, Observer {
                this.profileData = it

                completedItems["profile"] = it.hasUserUploadedProfilePicture()
                if (it.hasUserUploadedProfilePicture()) {
                    profile_pic_check_iv.setImageResource(R.drawable.ic_done)
                } else {
                    profile_pic_check_iv.setImageResource(R.drawable.ic_pending_yellow_round)
                }
                completedItems["address"] = !it.address.current.isEmpty()
                if (it.address.current.isEmpty()) {
                    current_address_check_iv.setImageResource(R.drawable.ic_pending_yellow_round)
                } else {
                    current_address_check_iv.setImageResource(R.drawable.ic_done)
                }
            })

        gigVerificationViewModel.gigerVerificationStatus
            .observe(viewLifecycleOwner, Observer {
                this.gigerVerificationStatus = it
                completedItems["bank_details"] = it.bankDetailsUploaded
                if (it.bankDetailsUploaded) {
                    bank_details_check_iv.setImageResource(R.drawable.ic_done)
                } else {
                    bank_details_check_iv.setImageResource(R.drawable.ic_pending_yellow_round)
                }
                // checkForRedirection(completedItems)
            })

    }

    private fun startListeningForGigerVerificationStatusChanges() {
        gigVerificationViewModel.startListeningForGigerVerificationStatusChanges()
    }

    override fun onBackPressed(): Boolean {
        if (appBar.isSearchCurrentlyShown) {
            hideSoftKeyboard()
            appBar.hideSearchOption()
            enrolledUserAdapter.filter.filter("")
            return true
        } else {
//            try {
//                navigation.getBackStackEntry("main_home_screen")
//                navigation.popBackStack("mainHomeScreen",false)
////            findNavController().getBackStackEntry(R.id.mainHomeScreen)
////            findNavController().popBackStack(R.id.mainHomeScreen, false)
//            } catch (e: Exception) {
//                navigation.popBackStack("landinghomefragment",false)
////            findNavController().popBackStack(R.id.landinghomefragment, false)
//            }

            return false

        }

    }

    override fun onUserClicked(enrolledUser: EnrolledUser) {

    }

    fun shareLink() {
        Firebase.dynamicLinks.shortLinkAsync {
            longLink =
                Uri.parse(buildDeepLink(Uri.parse("http://www.gig4ce.com/?invite=${FirebaseAuth.getInstance().currentUser?.uid!!}&is_ambassador=true&latitude=${location?.latitude ?: 0.0}&longitude=${location?.longitude ?: 0.0}")).toString())
        }.addOnSuccessListener { result ->
            // Short link created
            val shortLink = result.shortLink
            shareToAnyApp(shortLink.toString())
        }.addOnFailureListener {
            // Error
            // ...
            showToast(it.message!!)
        }
    }


    fun buildDeepLink(deepLink: Uri): Uri {
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse(deepLink.toString()))
            .setDomainUriPrefix(buildConfig.getReferralBaseUrl())
            // Open links with this app on Android
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
            // Open links with com.example.ios on iOS
            .setIosParameters(DynamicLink.IosParameters.Builder("com.gigforce.ios").build())
            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters.Builder()
                    .setTitle("Gigforce")
                    .setDescription("Flexible work and learning platform")
                    .setImageUrl(Uri.parse("https://firebasestorage.googleapis.com/v0/b/gig4ce-app.appspot.com/o/app_assets%2Fgigforce.jpg?alt=media&token=f7d4463b-47e4-4b8e-9b55-207594656161"))
                    .build()
            ).buildDynamicLink()

        return dynamicLink.uri
    }

    private fun shareToAnyApp(url: String) {
        if (!isAdded) return

        val shareMessage = getString(R.string.looking_for_dynamic_working_hours) + " " + url
        navigation.navigateTo("referrals",bundleOf(
            AppConstants.INTENT_EXTRA_REFERRAL_LINK_WITH_TEXT to shareMessage,
            AppConstants.INTENT_EXTRA_REFERRAL_LINK to url
        ))
//        navigate(
//            R.id.referrals_fragment, bundleOf(
//                AppConstants.INTENT_EXTRA_REFERRAL_LINK_WITH_TEXT to shareMessage,
//                AppConstants.INTENT_EXTRA_REFERRAL_LINK to url
//            )
//        )
    }

    override fun onUserEditButtonclicked(enrolledUser: EnrolledUser) {
        EditProfileConsentAndSendOtpDialogFragment.launch(
            enrolledUser,
            childFragmentManager,
            this@AmbassadorEnrolledUsersListFragment
        )
    }

    override fun onOtpSent(sendOtpResponseData: SendOtpResponseData) {
        navigation.navigateTo("userinfo/confirmOtpFragment",bundleOf(
            EnrollmentConstants.INTENT_EXTRA_USER_ID to sendOtpResponseData.enrolledUser.uid,
            EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER to sendOtpResponseData.enrolledUser.mobileNumber,
            EnrollmentConstants.INTENT_EXTRA_MODE to EnrollmentConstants.MODE_EDIT,
            ConfirmOtpFragment.INTENT_EXTRA_MOBILE_NO to sendOtpResponseData.enrolledUser.mobileNumber,
            ConfirmOtpFragment.INTENT_EXTRA_OTP_TOKEN to sendOtpResponseData.checkMobileResponse.verificationToken
        ))
//        navigate(
//            R.id.confirmOtpFragment, bundleOf(
//                EnrollmentConstants.INTENT_EXTRA_USER_ID to sendOtpResponseData.enrolledUser.uid,
//                EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER to sendOtpResponseData.enrolledUser.mobileNumber,
//                EnrollmentConstants.INTENT_EXTRA_MODE to EnrollmentConstants.MODE_EDIT,
//                ConfirmOtpFragment.INTENT_EXTRA_MOBILE_NO to sendOtpResponseData.enrolledUser.mobileNumber,
//                ConfirmOtpFragment.INTENT_EXTRA_OTP_TOKEN to sendOtpResponseData.checkMobileResponse.verificationToken
//            )
//        )
    }

    override fun openChat(enrollUser: EnrolledUser) {
        val bundle = Bundle()

        bundle.putString(AppConstants.INTENT_EXTRA_CHAT_TYPE, AppConstants.CHAT_TYPE_USER)
        bundle.putString(
            AppConstants.INTENT_EXTRA_OTHER_USER_IMAGE,
            enrollUser.profileAvatarThumbnail
        )
        bundle.putString(AppConstants.INTENT_EXTRA_OTHER_USER_NAME, enrollUser.name)

        bundle.putString(AppConstants.INTENT_EXTRA_CHAT_HEADER_ID, "")
        bundle.putString(AppConstants.INTENT_EXTRA_OTHER_USER_ID, enrollUser.id)

        bundle.putString(StringConstants.MOBILE_NUMBER.value, enrollUser.mobileNumber)
        bundle.putBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, true)
        navigation.navigateTo("chats/chatPage",bundle)
//        navigate(R.id.chatPageFragment, bundle)
    }

    override fun onResume() {
        super.onResume()
//        StatusBarUtil.setDarkMode(requireActivity())
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(),
            ResourcesCompat.getColor(resources, R.color.lipstick_two, null)
        )
        locationUpdates.startUpdates(requireActivity() as AppCompatActivity)
        locationUpdates.setLocationUpdateCallbacks(this)
    }

    override fun onPause() {
        super.onPause()
        try {
            locationUpdates.stopLocationUpdates(activity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {

            LocationUpdates.REQUEST_PERMISSIONS_REQUEST_CODE -> if (PermissionUtils.permissionsGrantedCheck(
                    grantResults
                )
            ) {
                locationUpdates.startUpdates(requireActivity() as AppCompatActivity)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            LocationUpdates.REQUEST_CHECK_SETTINGS -> if (resultCode == Activity.RESULT_OK) locationUpdates.startUpdates(
                requireActivity() as AppCompatActivity
            )

        }
    }

    override fun locationReceiver(location: Location?) {
    }

    override fun lastLocationReceiver(location: Location?) {
        this.location = location
    }



}