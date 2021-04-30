package com.gigforce.wallet.components;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\f\u0018\u00002\u00020\u0001B\u000f\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004B\u0017\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007R$\u0010\n\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR$\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\b\u001a\u00020\u000f@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R$\u0010\u0015\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\f\"\u0004\b\u0017\u0010\u000eR$\u0010\u0018\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\f\"\u0004\b\u001a\u0010\u000e\u00a8\u0006\u001b"}, d2 = {"Lcom/gigforce/wallet/components/TransactionCard;", "Lcom/google/android/material/card/MaterialCardView;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "attrs", "Landroid/util/AttributeSet;", "(Landroid/content/Context;Landroid/util/AttributeSet;)V", "value", "", "agent", "getAgent", "()Ljava/lang/String;", "setAgent", "(Ljava/lang/String;)V", "", "amount", "getAmount", "()I", "setAmount", "(I)V", "status", "getStatus", "setStatus", "timings", "getTimings", "setTimings", "wallet_debug"})
public final class TransactionCard extends com.google.android.material.card.MaterialCardView {
    @org.jetbrains.annotations.NotNull()
    private java.lang.String timings = "";
    private int amount = 0;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String agent = "";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String status = "";
    private java.util.HashMap _$_findViewCache;
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTimings() {
        return null;
    }
    
    public final void setTimings(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final int getAmount() {
        return 0;
    }
    
    public final void setAmount(int value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAgent() {
        return null;
    }
    
    public final void setAgent(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getStatus() {
        return null;
    }
    
    public final void setStatus(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public TransactionCard(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super(null);
    }
    
    public TransactionCard(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.util.AttributeSet attrs) {
        super(null);
    }
}