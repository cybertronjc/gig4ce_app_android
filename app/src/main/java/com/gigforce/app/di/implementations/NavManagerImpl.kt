package com.gigforce.app.di.implementations

import androidx.navigation.NavController
import com.gigforce.app.MainActivity
import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl
import javax.inject.Inject

class NavManagerImpl @Inject constructor() :
        BaseNavigationImpl()
{

    override val navController:NavController
        get() {return (this.context as MainActivity).getNavController()}

    override fun RegisterAllRoutes() {

        this.RegisterRoute("setting", R.id.settingFragment)

        this.registerForChatModule()
        this.registerForLearningModule()
    }

    private fun registerForChatModule(){

    }

    private fun registerForLearningModule(){

    }
}

interface INavAction {

}