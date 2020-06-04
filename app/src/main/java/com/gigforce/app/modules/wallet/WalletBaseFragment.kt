package com.gigforce.app.modules.wallet

import androidx.fragment.app.activityViewModels
import com.gigforce.app.core.base.BaseFragment

abstract class WalletBaseFragment: BaseFragment() {

    val walletViewModel: WalletViewModel by activityViewModels<WalletViewModel>()
}