package com.gigforce.wallet.components;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\t\u0018\u00002\u00020\u0001B\u000f\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004B\u0017\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007R$\u0010\n\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR$\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\b\u001a\u00020\u000f@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R$\u0010\u0015\u001a\u00020\u000f2\u0006\u0010\b\u001a\u00020\u000f@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u0012\"\u0004\b\u0017\u0010\u0014\u00a8\u0006\u0018"}, d2 = {"Lcom/gigforce/wallet/components/BalanceMainCard;", "Lcom/google/android/material/card/MaterialCardView;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "attributeSet", "Landroid/util/AttributeSet;", "(Landroid/content/Context;Landroid/util/AttributeSet;)V", "value", "", "balance", "getBalance", "()F", "setBalance", "(F)V", "", "receivedThisMonth", "getReceivedThisMonth", "()I", "setReceivedThisMonth", "(I)V", "withdrawnThisMonth", "getWithdrawnThisMonth", "setWithdrawnThisMonth", "wallet_debug"})
public final class BalanceMainCard extends com.google.android.material.card.MaterialCardView {
    private float balance = 0.0F;
    private int receivedThisMonth = 0;
    private int withdrawnThisMonth = 0;
    private java.util.HashMap _$_findViewCache;
    
    public final float getBalance() {
        return 0.0F;
    }
    
    public final void setBalance(float value) {
    }
    
    public final int getReceivedThisMonth() {
        return 0;
    }
    
    public final void setReceivedThisMonth(int value) {
    }
    
    public final int getWithdrawnThisMonth() {
        return 0;
    }
    
    public final void setWithdrawnThisMonth(int value) {
    }
    
    public BalanceMainCard(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super(null);
    }
    
    public BalanceMainCard(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.util.AttributeSet attributeSet) {
        super(null);
    }
}