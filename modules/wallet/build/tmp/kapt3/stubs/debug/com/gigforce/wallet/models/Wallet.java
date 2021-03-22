package com.gigforce.wallet.models;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0016\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B-\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\tJ\t\u0010\u0017\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0007H\u00c6\u0003J1\u0010\u001b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010\u001c\u001a\u00020\u00052\b\u0010\u001d\u001a\u0004\u0018\u00010\u001eH\u00d6\u0003J\t\u0010\u001f\u001a\u00020\u0007H\u00d6\u0001J\t\u0010 \u001a\u00020!H\u00d6\u0001R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0004\u0010\u000e\"\u0004\b\u000f\u0010\u0010R\u001a\u0010\b\u001a\u00020\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R\u001a\u0010\u0006\u001a\u00020\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0015\u0010\u0012\"\u0004\b\u0016\u0010\u0014\u00a8\u0006\""}, d2 = {"Lcom/gigforce/wallet/models/Wallet;", "Lcom/gigforce/core/base/basefirestore/BaseFirestoreDataModel;", "balance", "", "isMonthlyGoalSet", "", "monthlyGoalLimit", "", "monthlyEarnedAmount", "(FZII)V", "getBalance", "()F", "setBalance", "(F)V", "()Z", "setMonthlyGoalSet", "(Z)V", "getMonthlyEarnedAmount", "()I", "setMonthlyEarnedAmount", "(I)V", "getMonthlyGoalLimit", "setMonthlyGoalLimit", "component1", "component2", "component3", "component4", "copy", "equals", "other", "", "hashCode", "toString", "", "wallet_debug"})
public final class Wallet extends com.gigforce.core.base.basefirestore.BaseFirestoreDataModel {
    private float balance;
    private boolean isMonthlyGoalSet;
    private int monthlyGoalLimit;
    private int monthlyEarnedAmount;
    
    public final float getBalance() {
        return 0.0F;
    }
    
    public final void setBalance(float p0) {
    }
    
    public final boolean isMonthlyGoalSet() {
        return false;
    }
    
    public final void setMonthlyGoalSet(boolean p0) {
    }
    
    public final int getMonthlyGoalLimit() {
        return 0;
    }
    
    public final void setMonthlyGoalLimit(int p0) {
    }
    
    public final int getMonthlyEarnedAmount() {
        return 0;
    }
    
    public final void setMonthlyEarnedAmount(int p0) {
    }
    
    public Wallet(float balance, boolean isMonthlyGoalSet, int monthlyGoalLimit, int monthlyEarnedAmount) {
        super(null);
    }
    
    public Wallet() {
        super(null);
    }
    
    public final float component1() {
        return 0.0F;
    }
    
    public final boolean component2() {
        return false;
    }
    
    public final int component3() {
        return 0;
    }
    
    public final int component4() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gigforce.wallet.models.Wallet copy(float balance, boolean isMonthlyGoalSet, int monthlyGoalLimit, int monthlyEarnedAmount) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object p0) {
        return false;
    }
}