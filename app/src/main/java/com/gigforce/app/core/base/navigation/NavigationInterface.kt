package com.gigforce.app.core.base.navigation

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavOptions

interface NavigationInterface {
    fun getNavigationController(): NavController
    fun popFragmentFromStack(id: Int)
    fun navigate(@IdRes resId: Int, args: Bundle?, navOptions: NavOptions?)
    fun navigate(@IdRes resId: Int)
    fun navigateWithAllPopupStack(@IdRes resId: Int)
    fun popAllBackStates()
    fun popBackState()
    fun navigate(@IdRes resId: Int, args: Bundle?)

}