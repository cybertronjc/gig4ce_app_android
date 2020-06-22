package com.gigforce.app.core.base.navigation

import android.app.Activity
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.gigforce.app.R
import com.gigforce.app.utils.popAllBackStates

class NavigationImpl :NavigationInterface {
    var navController: NavController
    constructor(activity: Activity){
        navController = activity?.findNavController(R.id.nav_fragment)!!
    }

    override fun getNavigationController(): NavController {
        return navController
    }

    override fun popFragmentFromStack(id: Int) {
        navController.popBackStack(id, true)
    }

    override open fun navigate(
        @IdRes resId: Int, args: Bundle?,
        navOptions: NavOptions?
    ) {
        navController
            .navigate(resId, null, navOptions)
    }

    override fun navigate(@IdRes resId: Int) {
        navController.navigate(resId)
    }

    override fun navigate(resId: Int, args: Bundle?) {
        navController.navigate(resId,args)
    }

    override fun navigateWithAllPopupStack(@IdRes resId: Int) {
        popAllBackStates()
        navigate(resId)
    }

    override fun popAllBackStates() {
        navController.popAllBackStates()
    }

    override fun popBackState() {
        navController.popBackStack()
    }
}