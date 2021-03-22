package com.gigforce.wallet;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\b&\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002R\u001b\u0010\u0003\u001a\u00020\u00048FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0005\u0010\u0006R\u001b\u0010\t\u001a\u00020\n8FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\r\u0010\b\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\u000e"}, d2 = {"Lcom/gigforce/wallet/WalletBaseFragment;", "Landroidx/fragment/app/Fragment;", "()V", "invoiceViewModel", "Lcom/gigforce/wallet/vm/InvoiceViewModel;", "getInvoiceViewModel", "()Lcom/gigforce/wallet/vm/InvoiceViewModel;", "invoiceViewModel$delegate", "Lkotlin/Lazy;", "walletViewModel", "Lcom/gigforce/wallet/vm/WalletViewModel;", "getWalletViewModel", "()Lcom/gigforce/wallet/vm/WalletViewModel;", "walletViewModel$delegate", "wallet_debug"})
public abstract class WalletBaseFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy walletViewModel$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy invoiceViewModel$delegate = null;
    private java.util.HashMap _$_findViewCache;
    
    @org.jetbrains.annotations.NotNull()
    public final com.gigforce.wallet.vm.WalletViewModel getWalletViewModel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gigforce.wallet.vm.InvoiceViewModel getInvoiceViewModel() {
        return null;
    }
    
    public WalletBaseFragment() {
        super();
    }
}