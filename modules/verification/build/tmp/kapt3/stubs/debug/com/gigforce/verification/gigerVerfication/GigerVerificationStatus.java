package com.gigforce.verification.gigerVerfication;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\"\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\b\u0018\u0000 92\u00020\u0001:\u00019B}\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\b\b\u0002\u0010\t\u001a\u00020\u0003\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\u0003\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0011\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0013J\t\u0010%\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010&\u001a\u0004\u0018\u00010\u0011H\u00c6\u0003J\t\u0010\'\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010(\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010)\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010*\u001a\u0004\u0018\u00010\bH\u00c6\u0003J\t\u0010+\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010,\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003J\t\u0010-\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010.\u001a\u0004\u0018\u00010\u000eH\u00c6\u0003J\t\u0010/\u001a\u00020\u0003H\u00c6\u0003J\u0081\u0001\u00100\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b2\b\b\u0002\u0010\t\u001a\u00020\u00032\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b2\b\b\u0002\u0010\f\u001a\u00020\u00032\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u00032\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00112\b\b\u0002\u0010\u0012\u001a\u00020\u0003H\u00c6\u0001J\u0013\u00101\u001a\u00020\u00032\b\u00102\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\u000e\u00103\u001a\u0002042\u0006\u00105\u001a\u000204J\t\u00106\u001a\u000204H\u00d6\u0001J\t\u00107\u001a\u000208H\u00d6\u0001R\u0013\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\u000f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0017R\u0013\u0010\u0010\u001a\u0004\u0018\u00010\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0017R\u0013\u0010\r\u001a\u0004\u0018\u00010\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0011\u0010\u0012\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0017R\u0013\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010 R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u0017R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u0017\u00a8\u0006:"}, d2 = {"Lcom/gigforce/verification/gigerVerfication/GigerVerificationStatus;", "", "selfieVideoUploaded", "", "selfieVideoDataModel", "Lcom/gigforce/core/datamodels/verification/SelfieVideoDataModel;", "panCardDetailsUploaded", "panCardDetails", "Lcom/gigforce/core/datamodels/verification/PanCardDataModel;", "aadharCardDetailsUploaded", "aadharCardDataModel", "Lcom/gigforce/core/datamodels/verification/AadharCardDataModel;", "dlCardDetailsUploaded", "drivingLicenseDataModel", "Lcom/gigforce/core/datamodels/verification/DrivingLicenseDataModel;", "bankDetailsUploaded", "bankUploadDetailsDataModel", "Lcom/gigforce/core/datamodels/verification/BankDetailsDataModel;", "everyDocumentUploaded", "(ZLcom/gigforce/core/datamodels/verification/SelfieVideoDataModel;ZLcom/gigforce/core/datamodels/verification/PanCardDataModel;ZLcom/gigforce/core/datamodels/verification/AadharCardDataModel;ZLcom/gigforce/core/datamodels/verification/DrivingLicenseDataModel;ZLcom/gigforce/core/datamodels/verification/BankDetailsDataModel;Z)V", "getAadharCardDataModel", "()Lcom/gigforce/core/datamodels/verification/AadharCardDataModel;", "getAadharCardDetailsUploaded", "()Z", "getBankDetailsUploaded", "getBankUploadDetailsDataModel", "()Lcom/gigforce/core/datamodels/verification/BankDetailsDataModel;", "getDlCardDetailsUploaded", "getDrivingLicenseDataModel", "()Lcom/gigforce/core/datamodels/verification/DrivingLicenseDataModel;", "getEveryDocumentUploaded", "getPanCardDetails", "()Lcom/gigforce/core/datamodels/verification/PanCardDataModel;", "getPanCardDetailsUploaded", "getSelfieVideoDataModel", "()Lcom/gigforce/core/datamodels/verification/SelfieVideoDataModel;", "getSelfieVideoUploaded", "component1", "component10", "component11", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "getColorCodeForStatus", "", "statusCode", "hashCode", "toString", "", "Companion", "verification_debug"})
public final class GigerVerificationStatus {
    private final boolean selfieVideoUploaded = false;
    @org.jetbrains.annotations.Nullable()
    private final com.gigforce.core.datamodels.verification.SelfieVideoDataModel selfieVideoDataModel = null;
    private final boolean panCardDetailsUploaded = false;
    @org.jetbrains.annotations.Nullable()
    private final com.gigforce.core.datamodels.verification.PanCardDataModel panCardDetails = null;
    private final boolean aadharCardDetailsUploaded = false;
    @org.jetbrains.annotations.Nullable()
    private final com.gigforce.core.datamodels.verification.AadharCardDataModel aadharCardDataModel = null;
    private final boolean dlCardDetailsUploaded = false;
    @org.jetbrains.annotations.Nullable()
    private final com.gigforce.core.datamodels.verification.DrivingLicenseDataModel drivingLicenseDataModel = null;
    private final boolean bankDetailsUploaded = false;
    @org.jetbrains.annotations.Nullable()
    private final com.gigforce.core.datamodels.verification.BankDetailsDataModel bankUploadDetailsDataModel = null;
    private final boolean everyDocumentUploaded = false;
    public static final int STATUS_VERIFIED = 2;
    public static final int STATUS_VERIFICATION_FAILED = 3;
    public static final int STATUS_DOCUMENT_RECEIVED_BY_3RD_PARTY = 0;
    public static final int STATUS_DOCUMENT_PROCESSING = 1;
    public static final int STATUS_DOCUMENT_UPLOADED = -1;
    @org.jetbrains.annotations.NotNull()
    public static final com.gigforce.verification.gigerVerfication.GigerVerificationStatus.Companion Companion = null;
    
    public final int getColorCodeForStatus(int statusCode) {
        return 0;
    }
    
    public final boolean getSelfieVideoUploaded() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.gigforce.core.datamodels.verification.SelfieVideoDataModel getSelfieVideoDataModel() {
        return null;
    }
    
    public final boolean getPanCardDetailsUploaded() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.gigforce.core.datamodels.verification.PanCardDataModel getPanCardDetails() {
        return null;
    }
    
    public final boolean getAadharCardDetailsUploaded() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.gigforce.core.datamodels.verification.AadharCardDataModel getAadharCardDataModel() {
        return null;
    }
    
    public final boolean getDlCardDetailsUploaded() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.gigforce.core.datamodels.verification.DrivingLicenseDataModel getDrivingLicenseDataModel() {
        return null;
    }
    
    public final boolean getBankDetailsUploaded() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.gigforce.core.datamodels.verification.BankDetailsDataModel getBankUploadDetailsDataModel() {
        return null;
    }
    
    public final boolean getEveryDocumentUploaded() {
        return false;
    }
    
    public GigerVerificationStatus(boolean selfieVideoUploaded, @org.jetbrains.annotations.Nullable()
    com.gigforce.core.datamodels.verification.SelfieVideoDataModel selfieVideoDataModel, boolean panCardDetailsUploaded, @org.jetbrains.annotations.Nullable()
    com.gigforce.core.datamodels.verification.PanCardDataModel panCardDetails, boolean aadharCardDetailsUploaded, @org.jetbrains.annotations.Nullable()
    com.gigforce.core.datamodels.verification.AadharCardDataModel aadharCardDataModel, boolean dlCardDetailsUploaded, @org.jetbrains.annotations.Nullable()
    com.gigforce.core.datamodels.verification.DrivingLicenseDataModel drivingLicenseDataModel, boolean bankDetailsUploaded, @org.jetbrains.annotations.Nullable()
    com.gigforce.core.datamodels.verification.BankDetailsDataModel bankUploadDetailsDataModel, boolean everyDocumentUploaded) {
        super();
    }
    
    public GigerVerificationStatus() {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.gigforce.core.datamodels.verification.SelfieVideoDataModel component2() {
        return null;
    }
    
    public final boolean component3() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.gigforce.core.datamodels.verification.PanCardDataModel component4() {
        return null;
    }
    
    public final boolean component5() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.gigforce.core.datamodels.verification.AadharCardDataModel component6() {
        return null;
    }
    
    public final boolean component7() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.gigforce.core.datamodels.verification.DrivingLicenseDataModel component8() {
        return null;
    }
    
    public final boolean component9() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.gigforce.core.datamodels.verification.BankDetailsDataModel component10() {
        return null;
    }
    
    public final boolean component11() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gigforce.verification.gigerVerfication.GigerVerificationStatus copy(boolean selfieVideoUploaded, @org.jetbrains.annotations.Nullable()
    com.gigforce.core.datamodels.verification.SelfieVideoDataModel selfieVideoDataModel, boolean panCardDetailsUploaded, @org.jetbrains.annotations.Nullable()
    com.gigforce.core.datamodels.verification.PanCardDataModel panCardDetails, boolean aadharCardDetailsUploaded, @org.jetbrains.annotations.Nullable()
    com.gigforce.core.datamodels.verification.AadharCardDataModel aadharCardDataModel, boolean dlCardDetailsUploaded, @org.jetbrains.annotations.Nullable()
    com.gigforce.core.datamodels.verification.DrivingLicenseDataModel drivingLicenseDataModel, boolean bankDetailsUploaded, @org.jetbrains.annotations.Nullable()
    com.gigforce.core.datamodels.verification.BankDetailsDataModel bankUploadDetailsDataModel, boolean everyDocumentUploaded) {
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
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/gigforce/verification/gigerVerfication/GigerVerificationStatus$Companion;", "", "()V", "STATUS_DOCUMENT_PROCESSING", "", "STATUS_DOCUMENT_RECEIVED_BY_3RD_PARTY", "STATUS_DOCUMENT_UPLOADED", "STATUS_VERIFICATION_FAILED", "STATUS_VERIFIED", "verification_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}