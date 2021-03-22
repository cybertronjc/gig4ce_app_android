package com.gigforce.wallet.components;

import java.lang.System;

@androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.O)
@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\f\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004B\u0017\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007R\u001a\u0010\b\u001a\u00020\tX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR$\u0010\u000f\u001a\u00020\t2\u0006\u0010\u000e\u001a\u00020\t@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u000b\"\u0004\b\u0011\u0010\rR$\u0010\u0012\u001a\u00020\t2\u0006\u0010\u000e\u001a\u00020\t@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u000b\"\u0004\b\u0014\u0010\r\u00a8\u0006\u0015"}, d2 = {"Lcom/gigforce/wallet/components/PaymentSummaryComponent;", "Landroidx/constraintlayout/widget/ConstraintLayout;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "attrs", "Landroid/util/AttributeSet;", "(Landroid/content/Context;Landroid/util/AttributeSet;)V", "invoiceAmount", "", "getInvoiceAmount", "()I", "setInvoiceAmount", "(I)V", "value", "monthlyEarning", "getMonthlyEarning", "setMonthlyEarning", "paymentDueAmount", "getPaymentDueAmount", "setPaymentDueAmount", "wallet_debug"})
public final class PaymentSummaryComponent extends androidx.constraintlayout.widget.ConstraintLayout {
    private int monthlyEarning = 0;
    private int invoiceAmount = 0;
    private int paymentDueAmount = 0;
    private java.util.HashMap _$_findViewCache;
    
    public final int getMonthlyEarning() {
        return 0;
    }
    
    public final void setMonthlyEarning(int value) {
    }
    
    public final int getInvoiceAmount() {
        return 0;
    }
    
    public final void setInvoiceAmount(int p0) {
    }
    
    public final int getPaymentDueAmount() {
        return 0;
    }
    
    public final void setPaymentDueAmount(int value) {
    }
    
    public PaymentSummaryComponent(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super(null);
    }
    
    public PaymentSummaryComponent(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.util.AttributeSet attrs) {
        super(null);
    }
}