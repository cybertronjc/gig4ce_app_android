package com.gigforce.app.nav

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.gigforce.app.MainActivity
import com.gigforce.app.R
import com.gigforce.client_activation.client_activation.PlayVideoDialogWithUrl
import com.gigforce.app.utils.DocViewerActivity
import com.gigforce.common_ui.BaseNavigationImpl
import com.gigforce.common_ui.StringConstants
import com.gigforce.learning.learning.learningVideo.PlayVideoDialogFragment
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
        this.registerRoute("referrals",R.id.referrals_fragment)
        this.registerRoute("login",R.id.Login)
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
        NavForWalletModule(this)
    }



    private fun registerForWalletAndPayouts(){
        val moduleName:String = "wallet"
        this.registerRoute("${moduleName}/main", R.id.walletBalancePage)
    }

    override fun navigateToDocViewerActivity(activity:Activity,url:String){
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

    override fun navigateToPlayVideoDialogFragment(fragment:Fragment,lessonId:String,shouldShowFeedbackDialog:Boolean){
        PlayVideoDialogFragment.launch(
            childFragmentManager = fragment.childFragmentManager,
            lessonId = lessonId,
            moduleId = "",
            shouldShowFeedbackDialog = shouldShowFeedbackDialog,
            disableLessonCompleteAction = true
        )
    }

    override fun navigateToPlayVideoDialogWithUrl(fragment:Fragment,lessonId:String,shouldShowFeedbackDialog:Boolean){
        PlayVideoDialogWithUrl.launch(
            childFragmentManager = fragment.childFragmentManager,
            lessonId = lessonId,
            moduleId = "",
            shouldShowFeedbackDialog = shouldShowFeedbackDialog
        )
    }
}