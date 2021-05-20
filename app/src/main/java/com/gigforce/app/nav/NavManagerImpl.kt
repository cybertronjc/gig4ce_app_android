package com.gigforce.app.nav

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.gigforce.app.MainActivity
import com.gigforce.app.R
import com.gigforce.app.modules.markattendance.AttendanceImageCaptureActivity
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.DocViewerActivity
import com.gigforce.client_activation.client_activation.PlayVideoDialogWithUrl
import com.gigforce.common_ui.BaseNavigationImpl
import com.gigforce.common_ui.StringConstants
import com.gigforce.learning.learning.learningVideo.PlayVideoDialogFragment
import com.gigforce.verification.gigerVerfication.WhyWeNeedThisBottomSheet
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class NavManagerImpl @Inject constructor(
    @ActivityContext val context: Context
) : BaseNavigationImpl() {

    override fun getNavController(): NavController {
        return (context as MainActivity).getNavController()
    }

    override fun getActivity(): Activity {
        return context as MainActivity
    }

    override fun registerAllRoutes() {
        this.registerRoute("referrals", R.id.referrals_fragment)
        this.registerRoute("login", R.id.Login)
        this.registerRoute("bottom_sheet", R.id.bsFragment)
        this.registerRoute("profile", R.id.profileFragment)
        this.registerRoute("loader_screen", R.id.onboardingLoaderfragment)
        this.registerRoute("all_videos", R.id.helpVideosFragment)
        this.registerRoute("main_home_screen", R.id.mainHomeScreen)
        this.registerRoute("gigContactPersonBottomSheet",R.id.gigContactPersonBottomSheet)
        this.registerRoute("landinghomefragment",R.id.landinghomefragment)
        this.registerForWalletAndPayouts()
        NavForSettingsModule(this)
        NavForAmbassadorModule(this)
        NavForGigPageModule(this)
        NavForLearningModule(this)
        NavForChatModule(this)
        NavForClientActivatonModule(this)
        NavForVerificationModule(this)
        NavForCommonModule(this)
        NavForWalletModule(this)
        NavForPreferencesModule(this)
        NavUserDetailsInfo(this)
    }

    private fun registerForWalletAndPayouts() {
        val moduleName: String = "wallet"
        this.registerRoute("${moduleName}/main", R.id.walletBalancePage)
    }

    override fun navigateToDocViewerActivity(activity: Activity, url: String) {
        val docIntent = Intent(
            activity,
            DocViewerActivity::class.java
        )
        docIntent.putExtra(
            StringConstants.DOC_URL.value,
            url
        )
        activity.startActivity(docIntent)
    }


    override fun navigateToPlayVideoDialogFragment(
        fragment: Fragment,
        lessonId: String,
        shouldShowFeedbackDialog: Boolean
    ) {
        PlayVideoDialogFragment.launch(
            childFragmentManager = fragment.childFragmentManager,
            lessonId = lessonId,
            moduleId = "",
            shouldShowFeedbackDialog = shouldShowFeedbackDialog,
            disableLessonCompleteAction = true
        )
    }

    override fun navigateToPlayVideoDialogWithUrl(
        fragment: Fragment,
        lessonId: String,
        shouldShowFeedbackDialog: Boolean
    ) {
        PlayVideoDialogWithUrl.launch(
            childFragmentManager = fragment.childFragmentManager,
            lessonId = lessonId,
            moduleId = "",
            shouldShowFeedbackDialog = shouldShowFeedbackDialog
        )
    }

    override fun navigateToPhotoCrop(intent: Intent,
                                     requestCodeUploadPanImage: Int,
                                     requireContext: Context,
                                     fragment: Fragment) {
        val photoCropIntent = Intent(requireContext, PhotoCrop::class.java)
        photoCropIntent.putExtra("purpose", intent.getStringExtra("purpose"))
        if (intent.hasExtra("uid"))
            photoCropIntent.putExtra("uid", intent.getStringExtra("uid"))
        photoCropIntent.putExtra("fbDir", intent.getStringExtra("fbDir"))
        photoCropIntent.putExtra("folder", intent.getStringExtra("folder"))
        photoCropIntent.putExtra("detectFace", 0)
        if (intent.hasExtra("file"))
            photoCropIntent.putExtra("file", intent.getStringExtra("file"))

        fragment.startActivityForResult(photoCropIntent, requestCodeUploadPanImage)
    }

    override fun navigateToAttendanceImageCaptureActivity(
        intent: Intent,
        requestCodeUploadPanImage: Int,
        requireContext: Context,
        fragment: Fragment
    ) {
        val photoCropIntent = Intent(requireContext, AttendanceImageCaptureActivity::class.java)
        fragment.startActivityForResult(photoCropIntent, requestCodeUploadPanImage)
    }


    override fun navigateToWhyNeedThisBSFragment(childFragmentManager: FragmentManager,bundle : Bundle){
        val fragment = WhyWeNeedThisBottomSheet()
        fragment.arguments = bundle
        fragment.show(childFragmentManager, WhyWeNeedThisBottomSheet.TAG)
    }

}