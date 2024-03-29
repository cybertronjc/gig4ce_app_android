package com.gigforce.core.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavDestination

interface INavigation {
    fun getNavController(): NavController
    fun navigateTo(dest:String, args: Bundle? = null, navOptions: NavOptions? = null)
    fun popBackStack()
    fun popBackStack(des: String, inclusive: Boolean = true)
    fun getBackStackEntry(des:String)
    fun popAllBackStates()
    fun getActivity(): Activity
    fun navigateToDocViewerActivity(activity: Activity?,url:String, purpose: String, bundle: Bundle?=null, context: Context?=null)
    fun navigateToPlayVideoDialogFragment(fragment: Fragment, moduleId:String?="",lessonId:String, shouldShowFeedbackDialog:Boolean)
    fun navigateToPlayVideoDialogWithUrl(fragment: Fragment, lessonId:String, shouldShowFeedbackDialog:Boolean)
    fun navigateToPhotoCrop(photoCropIntent: Intent, requestCodeUploadPanImage: Int, requireContext: Context, fragment: Fragment)
    fun navigateUp()
    fun navigateToWhyNeedThisBSFragment(childFragmentManager: FragmentManager, bundle : Bundle)
    fun getCurrentDestination() : NavDestination?
}