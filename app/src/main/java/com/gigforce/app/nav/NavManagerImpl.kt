package com.gigforce.app.nav

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.gigforce.app.MainActivity
import com.gigforce.app.R
import com.gigforce.app.modules.client_activation.PlayVideoDialogWithUrl
import com.gigforce.app.modules.learning.learningVideo.PlayVideoDialogFragment
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.DocViewerActivity
import com.gigforce.app.utils.StringConstants
import com.gigforce.common_ui.BaseNavigationImpl
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class NavManagerImpl @Inject constructor(
    @ActivityContext val context: Context
    ) : BaseNavigationImpl()
{

    override fun getNavController(): NavController {
        return (context as MainActivity).getNavController()
    }
    
    override fun getActivity(): Activity {
        return context as MainActivity
    }
    override fun registerAllRoutes() {
        this.registerRoute("profile", R.id.profileFragment)
        this.registerRoute("bottom_sheet",R.id.bsFragment)
        this.registerRoute("all_videos", R.id.helpVideosFragment)
        this.registerForWalletAndPayouts()
        NavForSettingsModule(this)
        NavForAmbassadorModule(this)
        NavForGigPageModule(this)
        NavForLearningModule(this)
        NavForChatModule(this)
        NavForClientActivatonModule(this)
        NavForVerificationModule(this)
        NavForCommonModule(this)
    }

    private fun registerForWalletAndPayouts(){
        val moduleName:String = "wallet"
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

    override fun navigateToPhotoCrop(intent: Intent, requestCode: Int, fragment: Fragment) {
        val photoCropIntent = Intent(context, PhotoCrop::class.java)
        photoCropIntent.putExtra("purpose", intent.getStringExtra("verification"))
        if (intent.hasExtra("uid"))
            photoCropIntent.putExtra("uid", intent.getStringExtra("uid"))
        photoCropIntent.putExtra("fbDir", intent.getStringExtra("fbDir"))
        photoCropIntent.putExtra("folder", intent.getStringExtra("folder"))
        photoCropIntent.putExtra("detectFace", 0)
        if (intent.hasExtra("file"))
            photoCropIntent.putExtra("file", intent.getStringExtra("file"))

        fragment.startActivityForResult(intent, requestCode)
    }
}