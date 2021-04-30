package com.gigforce.wallet.adapters;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0007\u0018\u00002\f\u0012\b\u0012\u00060\u0002R\u00020\u00000\u0001:\u0001\u001cB\r\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\u0002\u0010\u0005J\b\u0010\u000e\u001a\u00020\u000fH\u0016J\u0010\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u000fH\u0016J\u001c\u0010\u0012\u001a\u00020\r2\n\u0010\u0013\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0011\u001a\u00020\u000fH\u0016J\u001c\u0010\u0014\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u000fH\u0016J\u001a\u0010\u0018\u001a\u00020\r2\u0012\u0010\u0019\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\r0\fJ\u0014\u0010\u001a\u001a\u00020\r2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\n0\tR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u000b\u001a\u0010\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\r\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lcom/gigforce/wallet/adapters/MonthlyPayslipsAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/gigforce/wallet/adapters/MonthlyPayslipsAdapter$TimeLineViewHolder;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "mLayoutInflater", "Landroid/view/LayoutInflater;", "mPayslips", "", "Lcom/gigforce/wallet/models/Payslip;", "paySlipClickActionListener", "Lkotlin/Function1;", "", "getItemCount", "", "getItemViewType", "position", "onBindViewHolder", "holder", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "setOnPaySlipClickActionListener", "listener", "updateCourseContent", "payslips", "TimeLineViewHolder", "wallet_debug"})
public final class MonthlyPayslipsAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.gigforce.wallet.adapters.MonthlyPayslipsAdapter.TimeLineViewHolder> {
    private kotlin.jvm.functions.Function1<? super com.gigforce.wallet.models.Payslip, kotlin.Unit> paySlipClickActionListener;
    private android.view.LayoutInflater mLayoutInflater;
    private java.util.List<com.gigforce.wallet.models.Payslip> mPayslips;
    private final android.content.Context context = null;
    
    public final void setOnPaySlipClickActionListener(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.gigforce.wallet.models.Payslip, kotlin.Unit> listener) {
    }
    
    public final void updateCourseContent(@org.jetbrains.annotations.NotNull()
    java.util.List<com.gigforce.wallet.models.Payslip> payslips) {
    }
    
    @java.lang.Override()
    public int getItemViewType(int position) {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public com.gigforce.wallet.adapters.MonthlyPayslipsAdapter.TimeLineViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.gigforce.wallet.adapters.MonthlyPayslipsAdapter.TimeLineViewHolder holder, int position) {
    }
    
    @java.lang.Override()
    public int getItemCount() {
        return 0;
    }
    
    public MonthlyPayslipsAdapter(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0004\u0018\u00002\u00020\u00012\u00020\u0002B\r\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\u0002\u0010\u0005J\u000e\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0014J\u0012\u0010\u0015\u001a\u00020\u00122\b\u0010\u0016\u001a\u0004\u0018\u00010\u0004H\u0016R\u0019\u0010\u0006\u001a\n \b*\u0004\u0018\u00010\u00070\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0019\u0010\u000b\u001a\n \b*\u0004\u0018\u00010\u00070\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\nR\u0019\u0010\r\u001a\n \b*\u0004\u0018\u00010\u00070\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\nR\u0019\u0010\u000f\u001a\n \b*\u0004\u0018\u00010\u00070\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\n\u00a8\u0006\u0017"}, d2 = {"Lcom/gigforce/wallet/adapters/MonthlyPayslipsAdapter$TimeLineViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "Landroid/view/View$OnClickListener;", "itemView", "Landroid/view/View;", "(Lcom/gigforce/wallet/adapters/MonthlyPayslipsAdapter;Landroid/view/View;)V", "monthYearTV", "Lcom/google/android/material/textview/MaterialTextView;", "kotlin.jvm.PlatformType", "getMonthYearTV", "()Lcom/google/android/material/textview/MaterialTextView;", "roleTV", "getRoleTV", "serialNoTV", "getSerialNoTV", "totalPayslipAmountTV", "getTotalPayslipAmountTV", "binTo", "", "paySlip", "Lcom/gigforce/wallet/models/Payslip;", "onClick", "v", "wallet_debug"})
    public final class TimeLineViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder implements android.view.View.OnClickListener {
        private final com.google.android.material.textview.MaterialTextView roleTV = null;
        private final com.google.android.material.textview.MaterialTextView serialNoTV = null;
        private final com.google.android.material.textview.MaterialTextView totalPayslipAmountTV = null;
        private final com.google.android.material.textview.MaterialTextView monthYearTV = null;
        
        public final com.google.android.material.textview.MaterialTextView getRoleTV() {
            return null;
        }
        
        public final com.google.android.material.textview.MaterialTextView getSerialNoTV() {
            return null;
        }
        
        public final com.google.android.material.textview.MaterialTextView getTotalPayslipAmountTV() {
            return null;
        }
        
        public final com.google.android.material.textview.MaterialTextView getMonthYearTV() {
            return null;
        }
        
        public final void binTo(@org.jetbrains.annotations.NotNull()
        com.gigforce.wallet.models.Payslip paySlip) {
        }
        
        @java.lang.Override()
        public void onClick(@org.jetbrains.annotations.Nullable()
        android.view.View v) {
        }
        
        public TimeLineViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.View itemView) {
            super(null);
        }
    }
}