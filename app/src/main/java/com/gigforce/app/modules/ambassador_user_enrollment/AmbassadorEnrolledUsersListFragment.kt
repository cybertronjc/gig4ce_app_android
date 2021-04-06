package com.gigforce.app.modules.ambassador_user_enrollment

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.BuildConfig
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.ambassador_user_enrollment.models.EnrolledUser
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.verify_mobile.ConfirmOtpFragment
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.verify_mobile.EditProfileConsentAndSendOtpDialogFragment
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.verify_mobile.UserDetailsFilledDialogFragmentResultListener
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.GigerVerificationStatus
import com.gigforce.app.modules.gigerVerfication.bankDetails.AddBankDetailsInfoFragment
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.modules.referrals.ReferralsFragment
import com.gigforce.app.utils.*
import com.gigforce.common_ui.views.GigforceToolbar
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.screens.ChatPageFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_embassador_enrolled_users_list.*
import kotlinx.android.synthetic.main.fragment_embassador_enrolled_users_list.bank_details_check_iv
import kotlinx.android.synthetic.main.fragment_embassador_enrolled_users_list.bank_details_layout
import kotlinx.android.synthetic.main.fragment_embassador_enrolled_users_list.current_address_check_iv
import kotlinx.android.synthetic.main.fragment_embassador_enrolled_users_list.current_address_layout
import kotlinx.android.synthetic.main.fragment_embassador_enrolled_users_list.profile_photo_layout
import kotlinx.android.synthetic.main.fragment_embassador_enrolled_users_list.profile_pic_check_iv
import kotlinx.android.synthetic.main.fragment_embassador_enrolled_users_list.toolbar_layout
import kotlinx.android.synthetic.main.fragment_embassador_program_requirement_screen.*

class AmbassadorEnrolledUsersListFragment : BaseFragment(),

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

    private val enrolledUserAdapter: EnrolledUsersRecyclerAdapter by lazy {
        EnrolledUsersRecyclerAdapter(requireContext()).apply {
            this.setListener(this@AmbassadorEnrolledUsersListFragment)
        }
    }

    private val onBackPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            Log.d("TAg","Back preseed")

            if (toolbar_layout.isSearchCurrentlyShown) {
                hideSoftKeyboard()
                toolbar_layout.hideSearchOption()
                enrolledUserAdapter.filter.filter("")
            }  else {
                isEnabled = false
                activity?.onBackPressed()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_embassador_enrolled_users_list, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initViewModel()

        startListeningForGigerVerificationStatusChanges()
    }

    private fun initUi() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,onBackPressCallback)
        onBackPressCallback.isEnabled = true

        bank_details_layout.setOnClickListener {
            redirectToNextStep = true
            navigate(
                R.id.addBankDetailsInfoFragment, bundleOf(
                    AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT to true
                )
            )
        }

        current_address_layout.setOnClickListener {
            redirectToNextStep = true
            navigate(
                R.id.addCurrentAddressFragment, bundleOf(
                    AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT to true
                )
            )
        }

        profile_photo_layout.setOnClickListener {
            redirectToNextStep = true
            navigate(
                R.id.addProfilePictureFragment, bundleOf(
                    EnrollmentConstants.INTENT_EXTRA_MODE to EnrollmentConstants.MODE_ENROLLMENT_REQUIREMENT,
                    AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT to true
                )
            )
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

            setBackButtonListener {

                if (toolbar_layout.isSearchCurrentlyShown) {
                    hideSoftKeyboard()
                    enrolledUserAdapter.filter.filter("")
                }  else {
                    activity?.onBackPressed()
                }
            }
        }

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
            navigate(R.id.checkMobileFragment)
        }

        createProfileBtn.setOnClickListener {
            navigate(R.id.checkMobileFragment)
        }

        enrolled_users_rv.layoutManager = LinearLayoutManager(activity?.applicationContext)
        enrolled_users_rv.addItemDecoration(VerticalItemDecorator(30))
        enrolled_users_rv.adapter = enrolledUserAdapter

        share_link.setOnClickListener {
            shareLink()
        }
        share_link_cl.setOnClickListener {
            shareLink()
        }
    }

    private fun initViewModel() {
        viewModel.enrolledUsers
            .observe(viewLifecycleOwner, Observer {
//                    it ?: return@Observer

                if (it.isEmpty()) {
                    enrolledUserAdapter.setData(emptyList())
                    no_users_enrolled_layout.visible()
                    createProfileBtn.gone()
                    share_link.gone()
                    total_complete_profile_tv.gone()
                    total_incomplete_profile_tv.gone()
                } else {
                    no_users_enrolled_layout.gone()
                    createProfileBtn.visible()
                    share_link.visible()
                    enrolledUserAdapter.setData(it)
                    total_complete_profile_tv.visible()

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

                    total_incomplete_profile_tv.visible()
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

        try {
            findNavController().getBackStackEntry(R.id.mainHomeScreen)
            findNavController().popBackStack(R.id.mainHomeScreen, false)
        } catch (e: Exception) {
            findNavController().popBackStack(R.id.landinghomefragment, false)
        }
        return true
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
            .setDomainUriPrefix(BuildConfig.REFERRAL_BASE_URL)
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

        val shareMessage = getString(R.string.looking_for_dynamic_working_hours) + " " + url
        navigate(
            R.id.referrals_fragment, bundleOf(
                ReferralsFragment.INTENT_EXTRA_REFERRAL_LINK_WITH_TEXT to shareMessage,
                ReferralsFragment.INTENT_EXTRA_REFERRAL_LINK to url
            )
        )
    }

    override fun onUserEditButtonclicked(enrolledUser: EnrolledUser) {
        EditProfileConsentAndSendOtpDialogFragment.launch(
            enrolledUser,
            childFragmentManager,
            this@AmbassadorEnrolledUsersListFragment
        )
    }

    override fun onOtpSent(sendOtpResponseData: SendOtpResponseData) {
        navigate(
            R.id.confirmOtpFragment, bundleOf(
                EnrollmentConstants.INTENT_EXTRA_USER_ID to sendOtpResponseData.enrolledUser.uid,
                EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER to sendOtpResponseData.enrolledUser.mobileNumber,
                EnrollmentConstants.INTENT_EXTRA_MODE to EnrollmentConstants.MODE_EDIT,
                ConfirmOtpFragment.INTENT_EXTRA_MOBILE_NO to sendOtpResponseData.enrolledUser.mobileNumber,
                ConfirmOtpFragment.INTENT_EXTRA_OTP_TOKEN to sendOtpResponseData.checkMobileResponse.verificationToken
            )
        )
    }

    override fun openChat(enrollUser: EnrolledUser) {
        val bundle = Bundle()

        bundle.putString(ChatPageFragment.INTENT_EXTRA_CHAT_TYPE, ChatConstants.CHAT_TYPE_USER)
        bundle.putString(
            ChatPageFragment.INTENT_EXTRA_OTHER_USER_IMAGE,
            enrollUser.profileAvatarThumbnail
        )
        bundle.putString(ChatPageFragment.INTENT_EXTRA_OTHER_USER_NAME, enrollUser.name)

        bundle.putString(ChatPageFragment.INTENT_EXTRA_CHAT_HEADER_ID, "")
        bundle.putString(ChatPageFragment.INTENT_EXTRA_OTHER_USER_ID, enrollUser.id)

        bundle.putString(StringConstants.MOBILE_NUMBER.value, enrollUser.mobileNumber)
        bundle.putBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, true)
        navigate(R.id.chatPageFragment, bundle)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationUpdates.stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        locationUpdates.startUpdates(requireActivity() as AppCompatActivity)
        locationUpdates.setLocationUpdateCallbacks(this)
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