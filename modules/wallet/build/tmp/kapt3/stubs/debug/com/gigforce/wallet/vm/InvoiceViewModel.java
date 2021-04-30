package com.gigforce.wallet.vm;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0014\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u0000 !2\u00020\u0001:\u0001!B\u0005\u00a2\u0006\u0002\u0010\u0002J.\u0010\u0017\u001a\u0012\u0012\u0004\u0012\u00020\u00060\u0005j\b\u0012\u0004\u0012\u00020\u0006`\u00072\u0016\u0010\u0018\u001a\u0012\u0012\u0004\u0012\u00020\u00060\u0005j\b\u0012\u0004\u0012\u00020\u0006`\u0007J.\u0010\u0019\u001a\u0012\u0012\u0004\u0012\u00020\u00060\u0005j\b\u0012\u0004\u0012\u00020\u0006`\u00072\u0016\u0010\u0018\u001a\u0012\u0012\u0004\u0012\u00020\u00060\u0005j\b\u0012\u0004\u0012\u00020\u0006`\u0007JB\u0010\u001a\u001a\u0012\u0012\u0004\u0012\u00020\u00060\u0005j\b\u0012\u0004\u0012\u00020\u0006`\u00072\u001a\u0010\u0018\u001a\u0016\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005j\n\u0012\u0004\u0012\u00020\u0006\u0018\u0001`\u00072\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u001cJ\u001e\u0010\u001e\u001a\u00020\u001c2\u0016\u0010\u0018\u001a\u0012\u0012\u0004\u0012\u00020\u00060\u0005j\b\u0012\u0004\u0012\u00020\u0006`\u0007J.\u0010\u0015\u001a\u0012\u0012\u0004\u0012\u00020\u00060\u0005j\b\u0012\u0004\u0012\u00020\u0006`\u00072\u0016\u0010\u0018\u001a\u0012\u0012\u0004\u0012\u00020\u00060\u0005j\b\u0012\u0004\u0012\u00020\u0006`\u0007J\b\u0010\u001f\u001a\u00020 H\u0002R0\u0010\u0003\u001a\u0018\u0012\u0014\u0012\u0012\u0012\u0004\u0012\u00020\u00060\u0005j\b\u0012\u0004\u0012\u00020\u0006`\u00070\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR0\u0010\f\u001a\u0018\u0012\u0014\u0012\u0012\u0012\u0004\u0012\u00020\u00060\u0005j\b\u0012\u0004\u0012\u00020\u0006`\u00070\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\t\"\u0004\b\u000e\u0010\u000bR*\u0010\u000f\u001a\u0012\u0012\u0004\u0012\u00020\u00060\u0005j\b\u0012\u0004\u0012\u00020\u0006`\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013R0\u0010\u0014\u001a\u0018\u0012\u0014\u0012\u0012\u0012\u0004\u0012\u00020\u00060\u0005j\b\u0012\u0004\u0012\u00020\u0006`\u00070\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0015\u0010\t\"\u0004\b\u0016\u0010\u000b\u00a8\u0006\""}, d2 = {"Lcom/gigforce/wallet/vm/InvoiceViewModel;", "Landroidx/lifecycle/ViewModel;", "()V", "allInvoices", "Landroidx/lifecycle/MutableLiveData;", "Ljava/util/ArrayList;", "Lcom/gigforce/wallet/models/Invoice;", "Lkotlin/collections/ArrayList;", "getAllInvoices", "()Landroidx/lifecycle/MutableLiveData;", "setAllInvoices", "(Landroidx/lifecycle/MutableLiveData;)V", "generatedInvoice", "getGeneratedInvoice", "setGeneratedInvoice", "monthlyInvoice", "getMonthlyInvoice", "()Ljava/util/ArrayList;", "setMonthlyInvoice", "(Ljava/util/ArrayList;)V", "pendingInvoices", "getPendingInvoices", "setPendingInvoices", "getDisputedInvoices", "invoices", "getGeneratedInvoices", "getMonthlyInvoices", "month", "", "year", "getPaymentDueAmount", "queryInvoices", "", "Companion", "wallet_debug"})
public final class InvoiceViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private androidx.lifecycle.MutableLiveData<java.util.ArrayList<com.gigforce.wallet.models.Invoice>> pendingInvoices;
    @org.jetbrains.annotations.NotNull()
    private androidx.lifecycle.MutableLiveData<java.util.ArrayList<com.gigforce.wallet.models.Invoice>> generatedInvoice;
    @org.jetbrains.annotations.NotNull()
    private java.util.ArrayList<com.gigforce.wallet.models.Invoice> monthlyInvoice;
    @org.jetbrains.annotations.NotNull()
    private androidx.lifecycle.MutableLiveData<java.util.ArrayList<com.gigforce.wallet.models.Invoice>> allInvoices;
    @org.jetbrains.annotations.NotNull()
    public static final com.gigforce.wallet.vm.InvoiceViewModel.Companion Companion = null;
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.util.ArrayList<com.gigforce.wallet.models.Invoice>> getPendingInvoices() {
        return null;
    }
    
    public final void setPendingInvoices(@org.jetbrains.annotations.NotNull()
    androidx.lifecycle.MutableLiveData<java.util.ArrayList<com.gigforce.wallet.models.Invoice>> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.util.ArrayList<com.gigforce.wallet.models.Invoice>> getGeneratedInvoice() {
        return null;
    }
    
    public final void setGeneratedInvoice(@org.jetbrains.annotations.NotNull()
    androidx.lifecycle.MutableLiveData<java.util.ArrayList<com.gigforce.wallet.models.Invoice>> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.ArrayList<com.gigforce.wallet.models.Invoice> getMonthlyInvoice() {
        return null;
    }
    
    public final void setMonthlyInvoice(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.wallet.models.Invoice> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.util.ArrayList<com.gigforce.wallet.models.Invoice>> getAllInvoices() {
        return null;
    }
    
    public final void setAllInvoices(@org.jetbrains.annotations.NotNull()
    androidx.lifecycle.MutableLiveData<java.util.ArrayList<com.gigforce.wallet.models.Invoice>> p0) {
    }
    
    private final void queryInvoices() {
    }
    
    public final int getPaymentDueAmount(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.wallet.models.Invoice> invoices) {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.ArrayList<com.gigforce.wallet.models.Invoice> getDisputedInvoices(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.wallet.models.Invoice> invoices) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.ArrayList<com.gigforce.wallet.models.Invoice> getMonthlyInvoices(@org.jetbrains.annotations.Nullable()
    java.util.ArrayList<com.gigforce.wallet.models.Invoice> invoices, int month, int year) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.ArrayList<com.gigforce.wallet.models.Invoice> getGeneratedInvoices(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.wallet.models.Invoice> invoices) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.ArrayList<com.gigforce.wallet.models.Invoice> getPendingInvoices(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.wallet.models.Invoice> invoices) {
        return null;
    }
    
    public InvoiceViewModel() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/gigforce/wallet/vm/InvoiceViewModel$Companion;", "", "()V", "newInstance", "Lcom/gigforce/wallet/vm/InvoiceViewModel;", "wallet_debug"})
    public static final class Companion {
        
        @org.jetbrains.annotations.NotNull()
        public final com.gigforce.wallet.vm.InvoiceViewModel newInstance() {
            return null;
        }
        
        private Companion() {
            super();
        }
    }
}