package com.gigforce.app.nav

import android.content.Context
import androidx.navigation.NavController
import com.gigforce.app.MainActivity
import com.gigforce.app.R
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

    override fun registerAllRoutes() {

        this.registerRoute("setting", R.id.settingFragment)
        this.registerRoute("profile", R.id.profileFragment)
        this.registerRoute("bottom_sheet",R.id.bsFragment)
        this.registerForWalletAndPayouts()

        NavForLearningModule(this)
        NavForChatModule(this)
    }

    private fun registerForWalletAndPayouts(){
        val moduleName:String = "wallet"
        this.registerRoute("${moduleName}/main", R.id.walletBalancePage)
    }
}