package com.gigforce.wallet;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001\u0012B\u001d\u0012\u0016\u0010\u0003\u001a\u0012\u0012\u0004\u0012\u00020\u00050\u0004j\b\u0012\u0004\u0012\u00020\u0005`\u0006\u00a2\u0006\u0002\u0010\u0007J\b\u0010\b\u001a\u00020\tH\u0016J\u0018\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\tH\u0016J\u0018\u0010\u000e\u001a\u00020\u00022\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\tH\u0016R\u001e\u0010\u0003\u001a\u0012\u0012\u0004\u0012\u00020\u00050\u0004j\b\u0012\u0004\u0012\u00020\u0005`\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/gigforce/wallet/MonthlyTransactionAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/gigforce/wallet/MonthlyTransactionAdapter$TransactionHolder;", "transactions", "Ljava/util/ArrayList;", "Lcom/gigforce/wallet/models/Invoice;", "Lkotlin/collections/ArrayList;", "(Ljava/util/ArrayList;)V", "getItemCount", "", "onBindViewHolder", "", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "TransactionHolder", "wallet_debug"})
public final class MonthlyTransactionAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.gigforce.wallet.MonthlyTransactionAdapter.TransactionHolder> {
    private final java.util.ArrayList<com.gigforce.wallet.models.Invoice> transactions = null;
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public com.gigforce.wallet.MonthlyTransactionAdapter.TransactionHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public int getItemCount() {
        return 0;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.gigforce.wallet.MonthlyTransactionAdapter.TransactionHolder holder, int position) {
    }
    
    public MonthlyTransactionAdapter(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.wallet.models.Invoice> transactions) {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\u0018\u0000 \f2\u00020\u00012\u00020\u0002:\u0001\fB\r\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\u0002\u0010\u0005J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u0006\u001a\u00020\u0007J\u0012\u0010\u000b\u001a\u00020\n2\b\u0010\u0003\u001a\u0004\u0018\u00010\u0004H\u0016R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/gigforce/wallet/MonthlyTransactionAdapter$TransactionHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "Landroid/view/View$OnClickListener;", "v", "Landroid/view/View;", "(Landroid/view/View;)V", "transaction", "Lcom/gigforce/wallet/models/Invoice;", "view", "bindTransaction", "", "onClick", "Companion", "wallet_debug"})
    public static final class TransactionHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder implements android.view.View.OnClickListener {
        private android.view.View view;
        private com.gigforce.wallet.models.Invoice transaction;
        private static final java.lang.String TRANSACTION_KEY = "TRANSACTION";
        @org.jetbrains.annotations.NotNull()
        public static final com.gigforce.wallet.MonthlyTransactionAdapter.TransactionHolder.Companion Companion = null;
        
        @java.lang.Override()
        public void onClick(@org.jetbrains.annotations.Nullable()
        android.view.View v) {
        }
        
        public final void bindTransaction(@org.jetbrains.annotations.NotNull()
        com.gigforce.wallet.models.Invoice transaction) {
        }
        
        public TransactionHolder(@org.jetbrains.annotations.NotNull()
        android.view.View v) {
            super(null);
        }
        
        @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/gigforce/wallet/MonthlyTransactionAdapter$TransactionHolder$Companion;", "", "()V", "TRANSACTION_KEY", "", "wallet_debug"})
        public static final class Companion {
            
            private Companion() {
                super();
            }
        }
    }
}