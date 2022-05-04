package com.gigforce.app.di.implementations

import com.gigforce.core.CoreViewHolderFactory
import com.gigforce.giger_app.AppModuleLevelViewTypeLoader
import com.gigforce.giger_app.ComponentViewLoader
import com.gigforce.giger_app.LandingViewTypeLoader
import com.gigforce.giger_gigs.GigCoreRecyclerViewBindings
import com.gigforce.lead_management.views.LeadActivationViewTypeLoader
import com.gigforce.giger_gigs.LoginSummaryViewTypeLoader
import com.gigforce.modules.feature_chat.ChatViewTypeLoader
import com.gigforce.verification.mainverification.VerificationViewTypeLoader
import com.gigforce.wallet.PayoutCoreRecyclerViewBindings
import javax.inject.Inject

class MyViewHolderFactory @Inject constructor(): CoreViewHolderFactory() {

    override fun registerAllViewTypeLoaders() {
        this.registerViewTypeLoader(AppModuleLevelViewTypeLoader())
        this.registerViewTypeLoader(ChatViewTypeLoader())
        this.registerViewTypeLoader(LandingViewTypeLoader())
        this.registerViewTypeLoader(GigCoreRecyclerViewBindings())
        this.registerViewTypeLoader(ComponentViewLoader())
        this.registerViewTypeLoader(LeadActivationViewTypeLoader)
        this.registerViewTypeLoader(LoginSummaryViewTypeLoader)
        this.registerViewTypeLoader(VerificationViewTypeLoader)
        this.registerViewTypeLoader(PayoutCoreRecyclerViewBindings)
    }
}