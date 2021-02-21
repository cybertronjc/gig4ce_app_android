package com.gigforce.app.modules.ambassador_user_enrollment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
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
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.models.EnrolledUser
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.verify_mobile.ConfirmOtpFragment
import com.gigforce.app.modules.chatmodule.ui.ChatFragment
import com.gigforce.app.modules.verification.UtilMethods
import com.gigforce.app.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_embassador_enrolled_users_list.*
import java.io.File
import java.io.FileOutputStream

class AmbassadorEnrolledUsersListFragment : BaseFragment(),

        EnrolledUsersRecyclerAdapter.EnrolledUsersRecyclerAdapterClickListener, LocationUpdates.LocationUpdateCallbacks {
    private val locationUpdates: LocationUpdates by lazy {
        LocationUpdates()
    }
    private val viewModel: AmbassadorEnrollViewModel by viewModels()
    private var location: Location? = null
    private val enrolledUserAdapter: EnrolledUsersRecyclerAdapter by lazy {
        EnrolledUsersRecyclerAdapter(requireContext()).apply {
            this.setListener(this@AmbassadorEnrolledUsersListFragment)
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
        getEnrolledUsers()
    }

    private fun initUi() {
        ic_back_iv?.setOnClickListener {
            activity?.onBackPressed()
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

                        val totalCompleteProfiles = it.count { it.enrollmentStepsCompleted.allStepsCompleted() }
                        val totalInCompleteProfiles = it.count { it.enrollmentStepsCompleted.allStepsCompleted().not() }

                        total_complete_profile_tv.text = buildSpannedString {
                            append("Total Completed Profile : ")
                            bold {
                                color(ResourcesCompat.getColor(resources, R.color.activated_color, null)) {
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

        viewModel
                .sendOtpToPhoneNumber
                .observe(viewLifecycleOwner, Observer {
                    it ?: return@Observer

                    when (it) {
                        Lce.Loading -> {
                            UtilMethods.showLoading(requireContext())
                        }
                        is Lce.Content -> {
                            UtilMethods.hideLoading()

                            showToast("Otp Sent")
                            navigate(
                                    R.id.confirmOtpFragment, bundleOf(
                                    EnrollmentConstants.INTENT_EXTRA_USER_ID to it.content.enrolledUser.uid,
                                    EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER to it.content.enrolledUser.mobileNumber,
                                    EnrollmentConstants.INTENT_EXTRA_MODE to EnrollmentConstants.MODE_EDIT,
                                    ConfirmOtpFragment.INTENT_EXTRA_MOBILE_NO to it.content.enrolledUser.mobileNumber,
                                    ConfirmOtpFragment.INTENT_EXTRA_OTP_TOKEN to it.content.checkMobileResponse.verificationToken
                            )
                            )
                        }
                        is Lce.Error -> {

                            UtilMethods.hideLoading()
                            MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Error")
                                    .setMessage(it.error)
                                    .setPositiveButton("Okay") { _, _ -> }
                                    .show()
                        }
                    }
                })
    }

    private fun getEnrolledUsers() {
        viewModel.enrolledUsers
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
                    Uri.parse(buildDeepLink(Uri.parse("http://www.gig4ce.com/?invite=${FirebaseAuth.getInstance().currentUser?.uid!!}&is_ambassador=true&latitude=${location?.latitude?:0.0}&longitude=${location?.longitude?:0.0}")).toString())
        }.addOnSuccessListener { result ->
            // Short link created
            val shortLink = result.shortLink
            shareToAnyApp(shortLink.toString())
        }.addOnFailureListener {
            // Error
            // ...
            showToast(it.message!!);
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

        return dynamicLink.uri;
    }

    fun shareToAnyApp(url: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/png"
            shareIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.app_name)
            )
            val shareMessage = getString(R.string.looking_for_dynamic_working_hours) + " " + url
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            val bitmap =
                    BitmapFactory.decodeResource(requireContext().resources, R.drawable.bg_gig_type)

            //save bitmap to app cache folder

            //save bitmap to app cache folder
            val outputFile = File(requireContext().cacheDir, "share" + ".png")
            val outPutStream = FileOutputStream(outputFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outPutStream)
            outPutStream.flush()
            outPutStream.close()
            outputFile.setReadable(true, false)
            shareIntent.putExtra(
                    Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().packageName + ".provider",
                    outputFile
            )
            )
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            //e.toString();
        }
    }

    override fun onUserEditButtonclicked(enrolledUser: EnrolledUser) {
        viewModel.getMobileNumberAndSendOtpInfo(enrolledUser)
    }

    override fun openChat(enrollUser: EnrolledUser) {
        val bundle = Bundle()

        bundle.putString(ChatFragment.INTENT_EXTRA_OTHER_USER_IMAGE, enrollUser.profileAvatarThumbnail)
        bundle.putString(ChatFragment.INTENT_EXTRA_OTHER_USER_NAME, enrollUser.name)

        bundle.putString(ChatFragment.INTENT_EXTRA_CHAT_HEADER_ID, "")
        bundle.putString(ChatFragment.INTENT_EXTRA_OTHER_USER_ID,enrollUser.id)

        bundle.putString(StringConstants.MOBILE_NUMBER.value, enrollUser.mobileNumber)
        bundle.putBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, true)
        navigate(R.id.chatScreenFragment, bundle)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationUpdates.stopLocationUpdates(requireActivity())
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
                locationUpdates!!.startUpdates(requireActivity() as AppCompatActivity)
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