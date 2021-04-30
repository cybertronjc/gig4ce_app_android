package com.gigforce.verification.gigerVerfication;

import java.lang.System;

@dagger.hilt.android.lifecycle.HiltViewModel()
@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000v\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0016\b\u0017\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010#\u001a\u00020$J\u001d\u0010%\u001a\u00020&2\n\b\u0002\u0010\'\u001a\u0004\u0018\u00010\nH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010(J\u0012\u0010)\u001a\u00020*2\n\b\u0002\u0010\'\u001a\u0004\u0018\u00010\nJ\b\u0010+\u001a\u00020$H\u0014J\b\u0010,\u001a\u00020\nH\u0002J\u0006\u0010-\u001a\u00020$J,\u0010.\u001a\u00020*2\u0006\u0010/\u001a\u00020\u001a2\b\u00100\u001a\u0004\u0018\u0001012\b\u00102\u001a\u0004\u0018\u0001012\b\u00103\u001a\u0004\u0018\u00010\nJ4\u0010.\u001a\u00020*2\u0006\u0010/\u001a\u00020\u001a2\b\u00100\u001a\u0004\u0018\u0001012\b\u00102\u001a\u0004\u0018\u0001012\b\u00103\u001a\u0004\u0018\u00010\n2\u0006\u0010\'\u001a\u00020\nJ6\u00104\u001a\u00020*2\u0006\u00105\u001a\u00020\u001a2\b\u00106\u001a\u0004\u0018\u0001012\b\u00107\u001a\u0004\u0018\u00010\n2\b\u00108\u001a\u0004\u0018\u00010\n2\b\u00109\u001a\u0004\u0018\u00010\nJ>\u00104\u001a\u00020*2\u0006\u00105\u001a\u00020\u001a2\b\u00106\u001a\u0004\u0018\u0001012\b\u00107\u001a\u0004\u0018\u00010\n2\b\u00108\u001a\u0004\u0018\u00010\n2\b\u00109\u001a\u0004\u0018\u00010\n2\u0006\u0010\'\u001a\u00020\nJ6\u0010:\u001a\u00020*2\u0006\u0010;\u001a\u00020\u001a2\b\u00100\u001a\u0004\u0018\u0001012\b\u00102\u001a\u0004\u0018\u0001012\b\u0010<\u001a\u0004\u0018\u00010\n2\b\u0010=\u001a\u0004\u0018\u00010\nJ>\u0010:\u001a\u00020*2\u0006\u0010;\u001a\u00020\u001a2\b\u00100\u001a\u0004\u0018\u0001012\b\u00102\u001a\u0004\u0018\u0001012\b\u0010<\u001a\u0004\u0018\u00010\n2\b\u0010=\u001a\u0004\u0018\u00010\n2\u0006\u0010\'\u001a\u00020\nJ6\u0010>\u001a\u00020*2\u0006\u0010;\u001a\u00020\u001a2\b\u00100\u001a\u0004\u0018\u0001012\b\u00102\u001a\u0004\u0018\u0001012\b\u0010<\u001a\u0004\u0018\u00010\n2\b\u0010=\u001a\u0004\u0018\u00010\nJ\"\u0010?\u001a\u00020*2\u0006\u0010@\u001a\u00020\u001a2\b\u0010A\u001a\u0004\u0018\u0001012\b\u0010B\u001a\u0004\u0018\u00010\nJ*\u0010?\u001a\u00020*2\u0006\u0010@\u001a\u00020\u001a2\b\u0010A\u001a\u0004\u0018\u0001012\b\u0010B\u001a\u0004\u0018\u00010\n2\u0006\u0010\'\u001a\u00020\nJ\u0018\u0010C\u001a\u00020*2\u0006\u0010;\u001a\u00020\u001a2\b\u00100\u001a\u0004\u0018\u000101J\u0019\u0010D\u001a\u00020\n2\u0006\u0010E\u001a\u000201H\u0082@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010FR\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00070\u000e8F\u00a2\u0006\u0006\u001a\u0004\b\u000f\u0010\u0010R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\n0\u000e8F\u00a2\u0006\u0006\u001a\u0004\b\u0014\u0010\u0010R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\f0\u000e8F\u00a2\u0006\u0006\u001a\u0004\b\u0018\u0010\u0010R\u001a\u0010\u0019\u001a\u00020\u001aX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001b\u0010\u001c\"\u0004\b\u001d\u0010\u001eR\u000e\u0010\u001f\u001a\u00020 X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010!\u001a\u0004\u0018\u00010\"X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006G"}, d2 = {"Lcom/gigforce/verification/gigerVerfication/GigVerificationViewModel;", "Landroidx/lifecycle/ViewModel;", "buildConfig", "Lcom/gigforce/core/di/interfaces/IBuildConfigVM;", "(Lcom/gigforce/core/di/interfaces/IBuildConfigVM;)V", "_documentUploadState", "Lcom/gigforce/core/SingleLiveEvent2;", "Lcom/gigforce/core/utils/Lse;", "_gigerContractStatus", "Landroidx/lifecycle/MutableLiveData;", "", "_gigerVerificationStatus", "Lcom/gigforce/verification/gigerVerfication/GigerVerificationStatus;", "documentUploadState", "Landroidx/lifecycle/LiveData;", "getDocumentUploadState", "()Landroidx/lifecycle/LiveData;", "firebaseStorage", "Lcom/google/firebase/storage/FirebaseStorage;", "gigerContractStatus", "getGigerContractStatus", "gigerVerificationRepository", "Lcom/gigforce/verification/gigerVerfication/GigerVerificationRepository;", "gigerVerificationStatus", "getGigerVerificationStatus", "redirectToNextStep", "", "getRedirectToNextStep", "()Z", "setRedirectToNextStep", "(Z)V", "userEnrollmentRepository", "Lcom/gigforce/core/di/repo/UserEnrollmentRepository;", "verificationChangesListener", "Lcom/google/firebase/firestore/ListenerRegistration;", "checkForSignedContract", "", "getVerificationModel", "Lcom/gigforce/core/datamodels/verification/VerificationBaseModel;", "userId", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getVerificationStatus", "Lkotlinx/coroutines/Job;", "onCleared", "prepareUniqueImageName", "startListeningForGigerVerificationStatusChanges", "updateAadharData", "userHasAadhar", "frontImagePath", "Landroid/net/Uri;", "backImagePath", "aadharCardNumber", "updateBankPassbookImagePath", "userHasPassBook", "passbookImagePath", "ifscCode", "bankName", "accountNo", "updateDLData", "userHasDL", "dlState", "dlNo", "updateDLDataClientActivation", "updatePanImagePath", "userHasPan", "panImage", "panCardNo", "uploadDLCer", "uploadImage", "image", "(Landroid/net/Uri;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "verification_debug"})
public class GigVerificationViewModel extends androidx.lifecycle.ViewModel {
    private final com.gigforce.verification.gigerVerfication.GigerVerificationRepository gigerVerificationRepository = null;
    private final com.google.firebase.storage.FirebaseStorage firebaseStorage = null;
    private final com.gigforce.core.di.repo.UserEnrollmentRepository userEnrollmentRepository = null;
    private boolean redirectToNextStep = false;
    private final androidx.lifecycle.MutableLiveData<com.gigforce.verification.gigerVerfication.GigerVerificationStatus> _gigerVerificationStatus = null;
    private final androidx.lifecycle.MutableLiveData<java.lang.String> _gigerContractStatus = null;
    private final com.gigforce.core.SingleLiveEvent2<com.gigforce.core.utils.Lse> _documentUploadState = null;
    private com.google.firebase.firestore.ListenerRegistration verificationChangesListener;
    private final com.gigforce.core.di.interfaces.IBuildConfigVM buildConfig = null;
    
    public final boolean getRedirectToNextStep() {
        return false;
    }
    
    public final void setRedirectToNextStep(boolean p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<com.gigforce.verification.gigerVerfication.GigerVerificationStatus> getGigerVerificationStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.String> getGigerContractStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<com.gigforce.core.utils.Lse> getDocumentUploadState() {
        return null;
    }
    
    public final void startListeningForGigerVerificationStatusChanges() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job getVerificationStatus(@org.jetbrains.annotations.Nullable()
    java.lang.String userId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updatePanImagePath(boolean userHasPan, @org.jetbrains.annotations.Nullable()
    android.net.Uri panImage, @org.jetbrains.annotations.Nullable()
    java.lang.String panCardNo) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updatePanImagePath(boolean userHasPan, @org.jetbrains.annotations.Nullable()
    android.net.Uri panImage, @org.jetbrains.annotations.Nullable()
    java.lang.String panCardNo, @org.jetbrains.annotations.NotNull()
    java.lang.String userId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateBankPassbookImagePath(boolean userHasPassBook, @org.jetbrains.annotations.Nullable()
    android.net.Uri passbookImagePath, @org.jetbrains.annotations.Nullable()
    java.lang.String ifscCode, @org.jetbrains.annotations.Nullable()
    java.lang.String bankName, @org.jetbrains.annotations.Nullable()
    java.lang.String accountNo) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateBankPassbookImagePath(boolean userHasPassBook, @org.jetbrains.annotations.Nullable()
    android.net.Uri passbookImagePath, @org.jetbrains.annotations.Nullable()
    java.lang.String ifscCode, @org.jetbrains.annotations.Nullable()
    java.lang.String bankName, @org.jetbrains.annotations.Nullable()
    java.lang.String accountNo, @org.jetbrains.annotations.NotNull()
    java.lang.String userId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateAadharData(boolean userHasAadhar, @org.jetbrains.annotations.Nullable()
    android.net.Uri frontImagePath, @org.jetbrains.annotations.Nullable()
    android.net.Uri backImagePath, @org.jetbrains.annotations.Nullable()
    java.lang.String aadharCardNumber) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateDLDataClientActivation(boolean userHasDL, @org.jetbrains.annotations.Nullable()
    android.net.Uri frontImagePath, @org.jetbrains.annotations.Nullable()
    android.net.Uri backImagePath, @org.jetbrains.annotations.Nullable()
    java.lang.String dlState, @org.jetbrains.annotations.Nullable()
    java.lang.String dlNo) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateAadharData(boolean userHasAadhar, @org.jetbrains.annotations.Nullable()
    android.net.Uri frontImagePath, @org.jetbrains.annotations.Nullable()
    android.net.Uri backImagePath, @org.jetbrains.annotations.Nullable()
    java.lang.String aadharCardNumber, @org.jetbrains.annotations.NotNull()
    java.lang.String userId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateDLData(boolean userHasDL, @org.jetbrains.annotations.Nullable()
    android.net.Uri frontImagePath, @org.jetbrains.annotations.Nullable()
    android.net.Uri backImagePath, @org.jetbrains.annotations.Nullable()
    java.lang.String dlState, @org.jetbrains.annotations.Nullable()
    java.lang.String dlNo) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateDLData(boolean userHasDL, @org.jetbrains.annotations.Nullable()
    android.net.Uri frontImagePath, @org.jetbrains.annotations.Nullable()
    android.net.Uri backImagePath, @org.jetbrains.annotations.Nullable()
    java.lang.String dlState, @org.jetbrains.annotations.Nullable()
    java.lang.String dlNo, @org.jetbrains.annotations.NotNull()
    java.lang.String userId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job uploadDLCer(boolean userHasDL, @org.jetbrains.annotations.Nullable()
    android.net.Uri frontImagePath) {
        return null;
    }
    
    private final java.lang.String prepareUniqueImageName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getVerificationModel(@org.jetbrains.annotations.Nullable()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.gigforce.core.datamodels.verification.VerificationBaseModel> p1) {
        return null;
    }
    
    public final void checkForSignedContract() {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
    
    @javax.inject.Inject()
    public GigVerificationViewModel(@org.jetbrains.annotations.NotNull()
    com.gigforce.core.di.interfaces.IBuildConfigVM buildConfig) {
        super();
    }
}