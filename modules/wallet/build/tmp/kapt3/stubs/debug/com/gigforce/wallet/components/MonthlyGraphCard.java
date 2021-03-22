package com.gigforce.wallet.components;

import java.lang.System;

@androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.O)
@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004B\u0017\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007J\b\u0010!\u001a\u00020\"H\u0017J\u000e\u0010#\u001a\u00020\"2\u0006\u0010$\u001a\u00020\tR\u001e\u0010\b\u001a\u00020\t8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR \u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\t0\u000fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013R \u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\u000fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u0011\"\u0004\b\u0017\u0010\u0013R \u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00150\u0019X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001a\u0010\u001b\"\u0004\b\u001c\u0010\u001dR\u001a\u0010\u001e\u001a\u00020\tX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010\u000b\"\u0004\b \u0010\r\u00a8\u0006%"}, d2 = {"Lcom/gigforce/wallet/components/MonthlyGraphCard;", "Lcom/google/android/material/card/MaterialCardView;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "attributeSet", "Landroid/util/AttributeSet;", "(Landroid/content/Context;Landroid/util/AttributeSet;)V", "activeMonth", "", "getActiveMonth", "()I", "setActiveMonth", "(I)V", "month", "Landroidx/lifecycle/MutableLiveData;", "getMonth", "()Landroidx/lifecycle/MutableLiveData;", "setMonth", "(Landroidx/lifecycle/MutableLiveData;)V", "monthYear", "", "getMonthYear", "setMonthYear", "months", "Ljava/util/ArrayList;", "getMonths", "()Ljava/util/ArrayList;", "setMonths", "(Ljava/util/ArrayList;)V", "year", "getYear", "setYear", "attachAdapter", "", "setYearFromPosition", "position", "wallet_debug"})
public final class MonthlyGraphCard extends com.google.android.material.card.MaterialCardView {
    @androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.O)
    private int activeMonth;
    @org.jetbrains.annotations.NotNull()
    private java.util.ArrayList<java.lang.String> months;
    @org.jetbrains.annotations.NotNull()
    private androidx.lifecycle.MutableLiveData<java.lang.Integer> month;
    @org.jetbrains.annotations.NotNull()
    private androidx.lifecycle.MutableLiveData<java.lang.String> monthYear;
    private int year = 2020;
    private java.util.HashMap _$_findViewCache;
    
    public final int getActiveMonth() {
        return 0;
    }
    
    public final void setActiveMonth(int p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.ArrayList<java.lang.String> getMonths() {
        return null;
    }
    
    public final void setMonths(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<java.lang.String> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.lang.Integer> getMonth() {
        return null;
    }
    
    public final void setMonth(@org.jetbrains.annotations.NotNull()
    androidx.lifecycle.MutableLiveData<java.lang.Integer> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.lang.String> getMonthYear() {
        return null;
    }
    
    public final void setMonthYear(@org.jetbrains.annotations.NotNull()
    androidx.lifecycle.MutableLiveData<java.lang.String> p0) {
    }
    
    public final int getYear() {
        return 0;
    }
    
    public final void setYear(int p0) {
    }
    
    @androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.O)
    public void attachAdapter() {
    }
    
    public final void setYearFromPosition(int position) {
    }
    
    public MonthlyGraphCard(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super(null);
    }
    
    public MonthlyGraphCard(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.util.AttributeSet attributeSet) {
        super(null);
    }
}