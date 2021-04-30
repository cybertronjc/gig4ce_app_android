package com.gigforce.common_ui

import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.gigforce.core.navigation.INavigation
import java.lang.Exception

abstract class BaseNavigationImpl(): INavigation {

    abstract fun getNavController():NavController
    abstract fun registerAllRoutes()

    private val navMap: HashMap<String, Int> = HashMap()

    init {
        registerAllRoutes()
    }

    override fun navigateTo(dest:String, args: Bundle?, navOptions:NavOptions?){
        val navController = getNavController()
        if(this.navMap.containsKey(dest))
             navController.navigate(this.navMap[dest]!!, args, navOptions)
    }

    override fun popBackStack() {
        val navController = getNavController()
        navController.popBackStack()
    }

    override fun popBackStack(des: String, inclusive: Boolean) {
        val navController = getNavController()
        if(this.navMap.containsKey(des))
            navController.popBackStack(this.navMap[des]!!,inclusive)
    }

    override fun getBackStackEntry(des: String) {
        val navController = getNavController()
        if(this.navMap.containsKey(des))
            navController.getBackStackEntry(this.navMap[des]!!)
    }


    fun registerRoute(dest:String, destResId:Int){
        if(this.navMap.containsKey(dest)){
            Log.w("Base/Nav", "Overriding existing nav key registration")
            throw Exception("Nav Key Already Exists") // Comment if not required
        }
        this.navMap[dest] = destResId
    }
    override fun popAllBackStates() {
        val navController = getNavController()
        var hasBackStack = true;
        while (hasBackStack) {
            hasBackStack = navController.popBackStack()
        }
    }

    override fun navigateUp() {
        getNavController().navigateUp()
    }
 }