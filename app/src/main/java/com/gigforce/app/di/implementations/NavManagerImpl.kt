package com.gigforce.app.di.implementations

import android.content.Context
import androidx.navigation.NavController
import com.gigforce.app.MainActivity
import com.gigforce.app.MainApplication
import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl
import javax.inject.Inject

class NavManagerImpl @Inject constructor(
) :
        BaseNavigationImpl()
{

    override fun getNavController(context: Context): NavController {
        return (context as MainActivity).getNavController()
    }

    override fun RegisterAllRoutes() {

        this.RegisterRoute("setting", R.id.settingFragment)
        this.RegisterRoute("profile", R.id.profileFragment)

        this.registerForChatModule()
        this.registerForLearningModule()
    }

    private fun registerForChatModule(){

    }

    private fun registerForLearningModule(){

    }
}