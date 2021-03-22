package com.gigforce.wallet.components;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\b\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\b\u0018\u00002\u00020\u0001B\u000f\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004B\u0017\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007R$\u0010\n\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR$\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\b\u001a\u00020\u000f@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R$\u0010\u0015\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\f\"\u0004\b\u0017\u0010\u000eR$\u0010\u0019\u001a\u00020\u00182\u0006\u0010\b\u001a\u00020\u0018@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001a\u0010\u001b\"\u0004\b\u001c\u0010\u001dR$\u0010\u001e\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010\f\"\u0004\b \u0010\u000eR$\u0010!\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\"\u0010\f\"\u0004\b#\u0010\u000eR$\u0010%\u001a\u00020$2\u0006\u0010\b\u001a\u00020$@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b%\u0010&\"\u0004\b\'\u0010(R$\u0010)\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b*\u0010\f\"\u0004\b+\u0010\u000e\u00a8\u0006,"}, d2 = {"Lcom/gigforce/wallet/components/InvoiceCollapsedCard;", "Lcom/google/android/material/card/MaterialCardView;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "attrs", "Landroid/util/AttributeSet;", "(Landroid/content/Context;Landroid/util/AttributeSet;)V", "value", "", "agent", "getAgent", "()Ljava/lang/String;", "setAgent", "(Ljava/lang/String;)V", "Landroid/graphics/drawable/Drawable;", "agentIcon", "getAgentIcon", "()Landroid/graphics/drawable/Drawable;", "setAgentIcon", "(Landroid/graphics/drawable/Drawable;)V", "endDate", "getEndDate", "setEndDate", "", "gigAmount", "getGigAmount", "()I", "setGigAmount", "(I)V", "gigId", "getGigId", "setGigId", "invoiceStatus", "getInvoiceStatus", "setInvoiceStatus", "", "isInvoiceGenerated", "()Z", "setInvoiceGenerated", "(Z)V", "startDate", "getStartDate", "setStartDate", "wallet_debug"})
public final class InvoiceCollapsedCard extends com.google.android.material.card.MaterialCardView {
    @org.jetbrains.annotations.NotNull()
    private android.graphics.drawable.Drawable agentIcon;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String agent = "";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String gigId = "123";
    private boolean isInvoiceGenerated = true;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String startDate = "XX-XX-XXXX";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String endDate = "XX-XX-XXXX";
    private int gigAmount = 2000;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String invoiceStatus = "pending";
    private java.util.HashMap _$_findViewCache;
    
    @org.jetbrains.annotations.NotNull()
    public final android.graphics.drawable.Drawable getAgentIcon() {
        return null;
    }
    
    public final void setAgentIcon(@org.jetbrains.annotations.NotNull()
    android.graphics.drawable.Drawable value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAgent() {
        return null;
    }
    
    public final void setAgent(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getGigId() {
        return null;
    }
    
    public final void setGigId(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final boolean isInvoiceGenerated() {
        return false;
    }
    
    public final void setInvoiceGenerated(boolean value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getStartDate() {
        return null;
    }
    
    public final void setStartDate(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEndDate() {
        return null;
    }
    
    public final void setEndDate(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final int getGigAmount() {
        return 0;
    }
    
    public final void setGigAmount(int value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getInvoiceStatus() {
        return null;
    }
    
    public final void setInvoiceStatus(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public InvoiceCollapsedCard(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super(null);
    }
    
    public InvoiceCollapsedCard(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.util.AttributeSet attrs) {
        super(null);
    }
}