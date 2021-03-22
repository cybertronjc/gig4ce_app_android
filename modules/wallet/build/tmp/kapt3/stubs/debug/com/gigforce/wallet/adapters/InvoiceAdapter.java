package com.gigforce.wallet.adapters;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\b\u0018\u0000 \u001c2\b\u0012\u0004\u0012\u00020\u00020\u00012\u00020\u0003:\u0006\u001c\u001d\u001e\u001f !B\u001d\u0012\u0016\u0010\u0004\u001a\u0012\u0012\u0004\u0012\u00020\u00060\u0005j\b\u0012\u0004\u0012\u00020\u0006`\u0007\u00a2\u0006\u0002\u0010\bJ\u0018\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\u00022\u0006\u0010\f\u001a\u00020\rH\u0002J\u0018\u0010\u000e\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\u00022\u0006\u0010\f\u001a\u00020\u000fH\u0002J\b\u0010\u0010\u001a\u00020\u0011H\u0016J\u0010\u0010\u0012\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\u0011H\u0016J\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0011H\u0016J\u0018\u0010\u0017\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\u00022\u0006\u0010\u0013\u001a\u00020\u0011H\u0016J\u0018\u0010\u0018\u001a\u00020\u00022\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u0011H\u0016R\u001e\u0010\u0004\u001a\u0012\u0012\u0004\u0012\u00020\u00060\u0005j\b\u0012\u0004\u0012\u00020\u0006`\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/gigforce/wallet/adapters/InvoiceAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "Lcom/jay/widget/StickyHeaders;", "transactions", "Ljava/util/ArrayList;", "Lcom/gigforce/wallet/adapters/InvoiceAdapter$IRow;", "Lkotlin/collections/ArrayList;", "(Ljava/util/ArrayList;)V", "OnBindHeader", "", "holder", "row", "Lcom/gigforce/wallet/adapters/InvoiceAdapter$HeaderRow;", "OnBindSection", "Lcom/gigforce/wallet/adapters/InvoiceAdapter$SectionRow;", "getItemCount", "", "getItemViewType", "position", "isStickyHeader", "", "p0", "onBindViewHolder", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "Companion", "HeaderRow", "HeaderViewHolder", "IRow", "SectionRow", "SectionViewHolder", "wallet_debug"})
public final class InvoiceAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder> implements com.jay.widget.StickyHeaders {
    private final java.util.ArrayList<com.gigforce.wallet.adapters.InvoiceAdapter.IRow> transactions = null;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_SECTION = 1;
    @org.jetbrains.annotations.NotNull()
    public static final com.gigforce.wallet.adapters.InvoiceAdapter.Companion Companion = null;
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public int getItemCount() {
        return 0;
    }
    
    @java.lang.Override()
    public int getItemViewType(int position) {
        return 0;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    androidx.recyclerview.widget.RecyclerView.ViewHolder holder, int position) {
    }
    
    private final void OnBindHeader(androidx.recyclerview.widget.RecyclerView.ViewHolder holder, com.gigforce.wallet.adapters.InvoiceAdapter.HeaderRow row) {
    }
    
    private final void OnBindSection(androidx.recyclerview.widget.RecyclerView.ViewHolder holder, com.gigforce.wallet.adapters.InvoiceAdapter.SectionRow row) {
    }
    
    @java.lang.Override()
    public boolean isStickyHeader(int p0) {
        return false;
    }
    
    public InvoiceAdapter(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.wallet.adapters.InvoiceAdapter.IRow> transactions) {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\n\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\bf\u0018\u00002\u00020\u0001\u00a8\u0006\u0002"}, d2 = {"Lcom/gigforce/wallet/adapters/InvoiceAdapter$IRow;", "", "wallet_debug"})
    public static abstract interface IRow {
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/gigforce/wallet/adapters/InvoiceAdapter$HeaderRow;", "Lcom/gigforce/wallet/adapters/InvoiceAdapter$IRow;", "monthString", "", "(Ljava/lang/String;)V", "getMonthString", "()Ljava/lang/String;", "wallet_debug"})
    public static final class HeaderRow implements com.gigforce.wallet.adapters.InvoiceAdapter.IRow {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String monthString = null;
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getMonthString() {
            return null;
        }
        
        public HeaderRow(@org.jetbrains.annotations.NotNull()
        java.lang.String monthString) {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/gigforce/wallet/adapters/InvoiceAdapter$SectionRow;", "Lcom/gigforce/wallet/adapters/InvoiceAdapter$IRow;", "invoice", "Lcom/gigforce/wallet/models/Invoice;", "(Lcom/gigforce/wallet/models/Invoice;)V", "getInvoice", "()Lcom/gigforce/wallet/models/Invoice;", "wallet_debug"})
    public static final class SectionRow implements com.gigforce.wallet.adapters.InvoiceAdapter.IRow {
        @org.jetbrains.annotations.NotNull()
        private final com.gigforce.wallet.models.Invoice invoice = null;
        
        @org.jetbrains.annotations.NotNull()
        public final com.gigforce.wallet.models.Invoice getInvoice() {
            return null;
        }
        
        public SectionRow(@org.jetbrains.annotations.NotNull()
        com.gigforce.wallet.models.Invoice invoice) {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/gigforce/wallet/adapters/InvoiceAdapter$HeaderViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "view", "Landroid/view/View;", "(Landroid/view/View;)V", "wallet_debug"})
    public static final class HeaderViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        
        public HeaderViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.View view) {
            super(null);
        }
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u0000 \n2\u00020\u0001:\u0001\nB\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\u0005\u001a\u00020\u0006R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0003X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/gigforce/wallet/adapters/InvoiceAdapter$SectionViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "v", "Landroid/view/View;", "(Landroid/view/View;)V", "transaction", "Lcom/gigforce/wallet/models/Invoice;", "view", "bindTransaction", "", "Companion", "wallet_debug"})
    public static final class SectionViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        private android.view.View view;
        private com.gigforce.wallet.models.Invoice transaction;
        private static final java.lang.String TRANSACTION_KEY = "TRANSACTION";
        @org.jetbrains.annotations.NotNull()
        public static final com.gigforce.wallet.adapters.InvoiceAdapter.SectionViewHolder.Companion Companion = null;
        
        public final void bindTransaction(@org.jetbrains.annotations.NotNull()
        com.gigforce.wallet.models.Invoice transaction) {
        }
        
        public SectionViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.View v) {
            super(null);
        }
        
        @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/gigforce/wallet/adapters/InvoiceAdapter$SectionViewHolder$Companion;", "", "()V", "TRANSACTION_KEY", "", "wallet_debug"})
        public static final class Companion {
            
            private Companion() {
                super();
            }
        }
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J.\u0010\u0006\u001a\u0012\u0012\u0004\u0012\u00020\b0\u0007j\b\u0012\u0004\u0012\u00020\b`\t2\u0016\u0010\n\u001a\u0012\u0012\u0004\u0012\u00020\u000b0\u0007j\b\u0012\u0004\u0012\u00020\u000b`\tR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/gigforce/wallet/adapters/InvoiceAdapter$Companion;", "", "()V", "TYPE_HEADER", "", "TYPE_SECTION", "arrangeTransactions", "Ljava/util/ArrayList;", "Lcom/gigforce/wallet/adapters/InvoiceAdapter$IRow;", "Lkotlin/collections/ArrayList;", "allTransactions", "Lcom/gigforce/wallet/models/Invoice;", "wallet_debug"})
    public static final class Companion {
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.ArrayList<com.gigforce.wallet.adapters.InvoiceAdapter.IRow> arrangeTransactions(@org.jetbrains.annotations.NotNull()
        java.util.ArrayList<com.gigforce.wallet.models.Invoice> allTransactions) {
            return null;
        }
        
        private Companion() {
            super();
        }
    }
}