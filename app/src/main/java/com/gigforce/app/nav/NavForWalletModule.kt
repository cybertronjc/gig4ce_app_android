package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForWalletModule(
    baseImplementation: BaseNavigationImpl
) {
    init {
        val moduleName = "wallet"
//        baseImplementation.registerRoute("${moduleName}", )
        baseImplementation.registerRoute("${moduleName}/helpExpandedPage", R.id.helpExpandedPage)

        baseImplementation.registerRoute("${moduleName}/invoiceStatusPage", R.id.invoiceStatusPage)
        baseImplementation.registerRoute("${moduleName}/paymentDisputePage", R.id.paymentDisputePage)
        baseImplementation.registerRoute("${moduleName}/monthlyEarningPage", R.id.monthlyEarningPage)
        baseImplementation.registerRoute("${moduleName}/walletExpandedPage", R.id.walletExpandedPage)
        baseImplementation.registerRoute("${moduleName}/invoicesList", R.id.invoicesListFragment)





    }
}