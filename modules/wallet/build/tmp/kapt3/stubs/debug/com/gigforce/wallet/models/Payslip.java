package com.gigforce.wallet.models;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\u0006\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0011\n\u0002\u0010\u000b\n\u0002\b0\b\u0086\b\u0018\u00002\u00020\u0001B\u009d\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0003\u0012\b\b\u0002\u0010\b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\t\u001a\u00020\u0003\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u0012\b\b\u0002\u0010\r\u001a\u00020\f\u0012\b\b\u0002\u0010\u000e\u001a\u00020\f\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0012\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\u0014J\t\u0010?\u001a\u00020\u0003H\u00c6\u0003J\t\u0010@\u001a\u00020\fH\u00c6\u0003J\t\u0010A\u001a\u00020\fH\u00c6\u0003J\t\u0010B\u001a\u00020\u0003H\u00c6\u0003J\t\u0010C\u001a\u00020\u0003H\u00c6\u0003J\t\u0010D\u001a\u00020\u0012H\u00c6\u0003J\u000b\u0010E\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010F\u001a\u00020\u0003H\u00c6\u0003J\t\u0010G\u001a\u00020\u0003H\u00c6\u0003J\t\u0010H\u001a\u00020\u0003H\u00c6\u0003J\t\u0010I\u001a\u00020\u0003H\u00c6\u0003J\t\u0010J\u001a\u00020\u0003H\u00c6\u0003J\t\u0010K\u001a\u00020\u0003H\u00c6\u0003J\t\u0010L\u001a\u00020\u0003H\u00c6\u0003J\t\u0010M\u001a\u00020\fH\u00c6\u0003J\u00a1\u0001\u0010N\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\f2\b\b\u0002\u0010\u000e\u001a\u00020\f2\b\b\u0002\u0010\u000f\u001a\u00020\u00032\b\b\u0002\u0010\u0010\u001a\u00020\u00032\b\b\u0002\u0010\u0011\u001a\u00020\u00122\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001J\u0013\u0010O\u001a\u00020$2\b\u0010P\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\u0006\u0010Q\u001a\u00020\u0012J\t\u0010R\u001a\u00020\u0012H\u00d6\u0001J\t\u0010S\u001a\u00020\u0003H\u00d6\u0001R\u001e\u0010\u0004\u001a\u00020\u00038\u0007@\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0015\u0010\u0016\"\u0004\b\u0017\u0010\u0018R\u001e\u0010\u0005\u001a\u00020\u00038\u0007@\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u0016\"\u0004\b\u001a\u0010\u0018R\u001e\u0010\u000e\u001a\u00020\f8\u0007@\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001b\u0010\u001c\"\u0004\b\u001d\u0010\u001eR\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010\u0016\"\u0004\b \u0010\u0018R\u001e\u0010\u0006\u001a\u00020\u00038\u0007@\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b!\u0010\u0016\"\u0004\b\"\u0010\u0018R\u001e\u0010#\u001a\u00020$8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b%\u0010&\"\u0004\b\'\u0010(R\u001e\u0010\u0007\u001a\u00020\u00038\u0007@\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b)\u0010\u0016\"\u0004\b*\u0010\u0018R\u001e\u0010\u000f\u001a\u00020\u00038\u0007@\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b+\u0010\u0016\"\u0004\b,\u0010\u0018R\u001e\u0010\b\u001a\u00020\u00038\u0007@\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b-\u0010\u0016\"\u0004\b.\u0010\u0018R \u0010\u0013\u001a\u0004\u0018\u00010\u00038\u0007@\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b/\u0010\u0016\"\u0004\b0\u0010\u0018R\u001e\u0010\t\u001a\u00020\u00038\u0007@\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b1\u0010\u0016\"\u0004\b2\u0010\u0018R\u001e\u0010\n\u001a\u00020\u00038\u0007@\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b3\u0010\u0016\"\u0004\b4\u0010\u0018R\u001e\u0010\u000b\u001a\u00020\f8\u0007@\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b5\u0010\u001c\"\u0004\b6\u0010\u001eR\u001e\u0010\u0010\u001a\u00020\u00038\u0007@\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b7\u0010\u0016\"\u0004\b8\u0010\u0018R\u001e\u0010\r\u001a\u00020\f8\u0007@\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b9\u0010\u001c\"\u0004\b:\u0010\u001eR\u001e\u0010\u0011\u001a\u00020\u00128\u0007@\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b;\u0010<\"\u0004\b=\u0010>\u00a8\u0006T"}, d2 = {"Lcom/gigforce/wallet/models/Payslip;", "", "id", "", "accountNo", "dateOfPayment", "ifsc", "location", "name", "profile", "serialNumber", "totalPayout", "", "variablePayout", "fixedPayout", "monthOfPayment", "uid", "yearOfPayment", "", "pdfDownloadLink", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DDDLjava/lang/String;Ljava/lang/String;ILjava/lang/String;)V", "getAccountNo", "()Ljava/lang/String;", "setAccountNo", "(Ljava/lang/String;)V", "getDateOfPayment", "setDateOfPayment", "getFixedPayout", "()D", "setFixedPayout", "(D)V", "getId", "setId", "getIfsc", "setIfsc", "loading", "", "getLoading", "()Z", "setLoading", "(Z)V", "getLocation", "setLocation", "getMonthOfPayment", "setMonthOfPayment", "getName", "setName", "getPdfDownloadLink", "setPdfDownloadLink", "getProfile", "setProfile", "getSerialNumber", "setSerialNumber", "getTotalPayout", "setTotalPayout", "getUid", "setUid", "getVariablePayout", "setVariablePayout", "getYearOfPayment", "()I", "setYearOfPayment", "(I)V", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "getMonthNo", "hashCode", "toString", "wallet_debug"})
public final class Payslip {
    @com.google.firebase.firestore.Exclude()
    private boolean loading = false;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String id;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String accountNo;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String dateOfPayment;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String ifsc;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String location;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String name;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String profile;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String serialNumber;
    private double totalPayout;
    private double variablePayout;
    private double fixedPayout;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String monthOfPayment;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String uid;
    private int yearOfPayment;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String pdfDownloadLink;
    
    public final int getMonthNo() {
        return 0;
    }
    
    public final boolean getLoading() {
        return false;
    }
    
    public final void setLoading(boolean p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    public final void setId(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @com.google.firebase.firestore.PropertyName(value = "accountNo")
    public final java.lang.String getAccountNo() {
        return null;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "accountNo")
    public final void setAccountNo(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @com.google.firebase.firestore.PropertyName(value = "dateOfPayment")
    public final java.lang.String getDateOfPayment() {
        return null;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "dateOfPayment")
    public final void setDateOfPayment(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @com.google.firebase.firestore.PropertyName(value = "ifsc")
    public final java.lang.String getIfsc() {
        return null;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "ifsc")
    public final void setIfsc(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @com.google.firebase.firestore.PropertyName(value = "location")
    public final java.lang.String getLocation() {
        return null;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "location")
    public final void setLocation(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @com.google.firebase.firestore.PropertyName(value = "name")
    public final java.lang.String getName() {
        return null;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "name")
    public final void setName(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @com.google.firebase.firestore.PropertyName(value = "profile")
    public final java.lang.String getProfile() {
        return null;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "profile")
    public final void setProfile(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @com.google.firebase.firestore.PropertyName(value = "serialNumber")
    public final java.lang.String getSerialNumber() {
        return null;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "serialNumber")
    public final void setSerialNumber(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @com.google.firebase.firestore.PropertyName(value = "totalPayout")
    public final double getTotalPayout() {
        return 0.0;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "totalPayout")
    public final void setTotalPayout(double p0) {
    }
    
    @com.google.firebase.firestore.PropertyName(value = "variablePayout")
    public final double getVariablePayout() {
        return 0.0;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "variablePayout")
    public final void setVariablePayout(double p0) {
    }
    
    @com.google.firebase.firestore.PropertyName(value = "fixedPayout")
    public final double getFixedPayout() {
        return 0.0;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "fixedPayout")
    public final void setFixedPayout(double p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @com.google.firebase.firestore.PropertyName(value = "monthOfPayment")
    public final java.lang.String getMonthOfPayment() {
        return null;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "monthOfPayment")
    public final void setMonthOfPayment(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @com.google.firebase.firestore.PropertyName(value = "uid")
    public final java.lang.String getUid() {
        return null;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "uid")
    public final void setUid(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @com.google.firebase.firestore.PropertyName(value = "yearOfPayment")
    public final int getYearOfPayment() {
        return 0;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "yearOfPayment")
    public final void setYearOfPayment(int p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    @com.google.firebase.firestore.PropertyName(value = "pdfDownloadLink")
    public final java.lang.String getPdfDownloadLink() {
        return null;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "pdfDownloadLink")
    public final void setPdfDownloadLink(@org.jetbrains.annotations.Nullable()
    java.lang.String p0) {
    }
    
    public Payslip(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String accountNo, @org.jetbrains.annotations.NotNull()
    java.lang.String dateOfPayment, @org.jetbrains.annotations.NotNull()
    java.lang.String ifsc, @org.jetbrains.annotations.NotNull()
    java.lang.String location, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String profile, @org.jetbrains.annotations.NotNull()
    java.lang.String serialNumber, double totalPayout, double variablePayout, double fixedPayout, @org.jetbrains.annotations.NotNull()
    java.lang.String monthOfPayment, @org.jetbrains.annotations.NotNull()
    java.lang.String uid, int yearOfPayment, @org.jetbrains.annotations.Nullable()
    java.lang.String pdfDownloadLink) {
        super();
    }
    
    public Payslip() {
        super();
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
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component8() {
        return null;
    }
    
    public final double component9() {
        return 0.0;
    }
    
    public final double component10() {
        return 0.0;
    }
    
    public final double component11() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component12() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component13() {
        return null;
    }
    
    public final int component14() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component15() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gigforce.wallet.models.Payslip copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String accountNo, @org.jetbrains.annotations.NotNull()
    java.lang.String dateOfPayment, @org.jetbrains.annotations.NotNull()
    java.lang.String ifsc, @org.jetbrains.annotations.NotNull()
    java.lang.String location, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String profile, @org.jetbrains.annotations.NotNull()
    java.lang.String serialNumber, double totalPayout, double variablePayout, double fixedPayout, @org.jetbrains.annotations.NotNull()
    java.lang.String monthOfPayment, @org.jetbrains.annotations.NotNull()
    java.lang.String uid, int yearOfPayment, @org.jetbrains.annotations.Nullable()
    java.lang.String pdfDownloadLink) {
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