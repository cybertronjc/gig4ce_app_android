package com.gigforce.wallet

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.gigforce.wallet.vm.InvoiceViewModel
import com.gigforce.wallet.vm.InvoicesListViewModel
import com.gigforce.wallet.vm.WalletViewModel

abstract class WalletBaseFragment : Fragment() {

    val walletViewModel: WalletViewModel by activityViewModels<WalletViewModel>()

    val invoiceViewModel: InvoiceViewModel by activityViewModels<InvoiceViewModel>()

    val invoicesListViewModel: InvoicesListViewModel by activityViewModels<InvoicesListViewModel>()
}