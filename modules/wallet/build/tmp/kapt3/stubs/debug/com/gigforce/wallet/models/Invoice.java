package com.gigforce.wallet.models;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b3\n\u0002\u0010\u0000\n\u0002\b\u0003\b\u0086\b\u0018\u00002\u00020\u0001B\u008b\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0007\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\f\u001a\u00020\n\u0012\b\b\u0002\u0010\r\u001a\u00020\n\u0012\b\b\u0002\u0010\u000e\u001a\u00020\n\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0011\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0013J\t\u00105\u001a\u00020\u0003H\u00c6\u0003J\t\u00106\u001a\u00020\nH\u00c6\u0003J\t\u00107\u001a\u00020\u0003H\u00c6\u0003J\t\u00108\u001a\u00020\u0011H\u00c6\u0003J\t\u00109\u001a\u00020\u0003H\u00c6\u0003J\t\u0010:\u001a\u00020\u0003H\u00c6\u0003J\t\u0010;\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010<\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u000b\u0010=\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\t\u0010>\u001a\u00020\nH\u00c6\u0003J\t\u0010?\u001a\u00020\u0003H\u00c6\u0003J\t\u0010@\u001a\u00020\nH\u00c6\u0003J\t\u0010A\u001a\u00020\nH\u00c6\u0003J\u008f\u0001\u0010B\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00072\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\u00032\b\b\u0002\u0010\f\u001a\u00020\n2\b\b\u0002\u0010\r\u001a\u00020\n2\b\b\u0002\u0010\u000e\u001a\u00020\n2\b\b\u0002\u0010\u000f\u001a\u00020\u00032\b\b\u0002\u0010\u0010\u001a\u00020\u00112\b\b\u0002\u0010\u0012\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010C\u001a\u00020\u00112\b\u0010D\u001a\u0004\u0018\u00010EH\u00d6\u0003J\t\u0010F\u001a\u00020\nH\u00d6\u0001J\t\u0010G\u001a\u00020\u0003H\u00d6\u0001R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0014\u0010\u0015\"\u0004\b\u0016\u0010\u0017R\u001a\u0010\f\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0018\u0010\u0019\"\u0004\b\u001a\u0010\u001bR\u001c\u0010\b\u001a\u0004\u0018\u00010\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001c\u0010\u001d\"\u0004\b\u001e\u0010\u001fR\u001a\u0010\t\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b \u0010\u0019\"\u0004\b!\u0010\u001bR\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\"\u0010\u0015\"\u0004\b#\u0010\u0017R\u001a\u0010\u000f\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b$\u0010\u0015\"\u0004\b%\u0010\u0017R\u001a\u0010\u0005\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b&\u0010\u0015\"\u0004\b\'\u0010\u0017R\u001a\u0010\u0012\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b(\u0010\u0015\"\u0004\b)\u0010\u0017R\u001a\u0010\u000b\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b*\u0010\u0015\"\u0004\b+\u0010\u0017R\u001a\u0010\u0010\u001a\u00020\u0011X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010,\"\u0004\b-\u0010.R\u001a\u0010\r\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b/\u0010\u0019\"\u0004\b0\u0010\u001bR\u001c\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b1\u0010\u001d\"\u0004\b2\u0010\u001fR\u001a\u0010\u000e\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b3\u0010\u0019\"\u0004\b4\u0010\u001b\u00a8\u0006H"}, d2 = {"Lcom/gigforce/wallet/models/Invoice;", "Lcom/gigforce/core/base/basefirestore/BaseFirestoreDataModel;", "agentName", "", "gigId", "gigerId", "startDate", "Ljava/util/Date;", "endDate", "gigAmount", "", "invoiceStatus", "date", "month", "year", "gigTiming", "isInvoiceGenerated", "", "invoiceGeneratedTime", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/Date;Ljava/util/Date;ILjava/lang/String;IIILjava/lang/String;ZLjava/lang/String;)V", "getAgentName", "()Ljava/lang/String;", "setAgentName", "(Ljava/lang/String;)V", "getDate", "()I", "setDate", "(I)V", "getEndDate", "()Ljava/util/Date;", "setEndDate", "(Ljava/util/Date;)V", "getGigAmount", "setGigAmount", "getGigId", "setGigId", "getGigTiming", "setGigTiming", "getGigerId", "setGigerId", "getInvoiceGeneratedTime", "setInvoiceGeneratedTime", "getInvoiceStatus", "setInvoiceStatus", "()Z", "setInvoiceGenerated", "(Z)V", "getMonth", "setMonth", "getStartDate", "setStartDate", "getYear", "setYear", "component1", "component10", "component11", "component12", "component13", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "", "hashCode", "toString", "wallet_debug"})
public final class Invoice extends com.gigforce.core.base.basefirestore.BaseFirestoreDataModel {
    @org.jetbrains.annotations.NotNull()
    private java.lang.String agentName;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String gigId;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String gigerId;
    @org.jetbrains.annotations.Nullable()
    private java.util.Date startDate;
    @org.jetbrains.annotations.Nullable()
    private java.util.Date endDate;
    private int gigAmount;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String invoiceStatus;
    private int date;
    private int month;
    private int year;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String gigTiming;
    private boolean isInvoiceGenerated;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String invoiceGeneratedTime;
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAgentName() {
        return null;
    }
    
    public final void setAgentName(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getGigId() {
        return null;
    }
    
    public final void setGigId(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getGigerId() {
        return null;
    }
    
    public final void setGigerId(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.Date getStartDate() {
        return null;
    }
    
    public final void setStartDate(@org.jetbrains.annotations.Nullable()
    java.util.Date p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.Date getEndDate() {
        return null;
    }
    
    public final void setEndDate(@org.jetbrains.annotations.Nullable()
    java.util.Date p0) {
    }
    
    public final int getGigAmount() {
        return 0;
    }
    
    public final void setGigAmount(int p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getInvoiceStatus() {
        return null;
    }
    
    public final void setInvoiceStatus(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    public final int getDate() {
        return 0;
    }
    
    public final void setDate(int p0) {
    }
    
    public final int getMonth() {
        return 0;
    }
    
    public final void setMonth(int p0) {
    }
    
    public final int getYear() {
        return 0;
    }
    
    public final void setYear(int p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getGigTiming() {
        return null;
    }
    
    public final void setGigTiming(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    public final boolean isInvoiceGenerated() {
        return false;
    }
    
    public final void setInvoiceGenerated(boolean p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getInvoiceGeneratedTime() {
        return null;
    }
    
    public final void setInvoiceGeneratedTime(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    public Invoice(@org.jetbrains.annotations.NotNull()
    java.lang.String agentName, @org.jetbrains.annotations.NotNull()
    java.lang.String gigId, @org.jetbrains.annotations.NotNull()
    java.lang.String gigerId, @org.jetbrains.annotations.Nullable()
    java.util.Date startDate, @org.jetbrains.annotations.Nullable()
    java.util.Date endDate, int gigAmount, @org.jetbrains.annotations.NotNull()
    java.lang.String invoiceStatus, int date, int month, int year, @org.jetbrains.annotations.NotNull()
    java.lang.String gigTiming, boolean isInvoiceGenerated, @org.jetbrains.annotations.NotNull()
    java.lang.String invoiceGeneratedTime) {
        super(null);
    }
    
    public Invoice() {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.Date component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.Date component5() {
        return null;
    }
    
    public final int component6() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component7() {
        return null;
    }
    
    public final int component8() {
        return 0;
    }
    
    public final int component9() {
        return 0;
    }
    
    public final int component10() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component11() {
        return null;
    }
    
    public final boolean component12() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component13() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gigforce.wallet.models.Invoice copy(@org.jetbrains.annotations.NotNull()
    java.lang.String agentName, @org.jetbrains.annotations.NotNull()
    java.lang.String gigId, @org.jetbrains.annotations.NotNull()
    java.lang.String gigerId, @org.jetbrains.annotations.Nullable()
    java.util.Date startDate, @org.jetbrains.annotations.Nullable()
    java.util.Date endDate, int gigAmount, @org.jetbrains.annotations.NotNull()
    java.lang.String invoiceStatus, int date, int month, int year, @org.jetbrains.annotations.NotNull()
    java.lang.String gigTiming, boolean isInvoiceGenerated, @org.jetbrains.annotations.NotNull()
    java.lang.String invoiceGeneratedTime) {
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