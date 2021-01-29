package com.gigforce.common_ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.gigforce.core.navigation.INavigation
import java.lang.Exception

abstract class BaseNavigationImpl(): INavigation {

    abstract fun getNavController(context: Context):NavController
    abstract fun RegisterAllRoutes()

    private val navMap: HashMap<String, Int> = HashMap()

    init {
        RegisterAllRoutes()
    }

    override fun NavigateTo(context: Context, dest:String, args: Bundle?, navOptions:NavOptions?){
        val navController = getNavController(context)
        if(this.navMap.containsKey(dest))
             navController.navigate(this.navMap[dest]!!, args, navOptions)
    }

    fun RegisterRoute(dest:String, destResId:Int){
        if(this.navMap.containsKey(dest)){
            Log.w("Base/Nav", "Overriding existing nav key registration")
            throw Exception("Nav Key Already Exists") // Comment if not required
        }
        this.navMap[dest] = destResId
    }

 }