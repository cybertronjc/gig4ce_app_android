package com.gigforce.wallet.vm;

import java.lang.System;

@dagger.hilt.android.lifecycle.HiltViewModel()
@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0080\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B!\b\u0007\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ!\u0010 \u001a\u00020\f2\u0006\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\fH\u0082@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010$J\u0016\u0010\u0012\u001a\u00020%2\u0006\u0010&\u001a\u00020\u000f2\u0006\u0010#\u001a\u00020\fJ\u0019\u0010\'\u001a\u00020(2\u0006\u0010)\u001a\u00020\"H\u0082@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010*J\u0006\u0010+\u001a\u00020%J\u0006\u0010,\u001a\u00020-J\b\u0010.\u001a\u00020-H\u0014J\u0018\u0010/\u001a\u0002002\u0006\u00101\u001a\u0002022\u0006\u00103\u001a\u00020\fH\u0002R\u001a\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\r\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R#\u0010\u0016\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\u000b0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0015R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001a\u001a\u0004\u0018\u00010\u001bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R \u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00110\u0013X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001d\u0010\u0015\"\u0004\b\u001e\u0010\u001fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u00064"}, d2 = {"Lcom/gigforce/wallet/vm/PayslipMonthlyViewModel;", "Landroidx/lifecycle/ViewModel;", "walletRepository", "Lcom/gigforce/wallet/WalletfirestoreRepository;", "profileFirebaseRepository", "Lcom/gigforce/core/di/repo/IProfileFirestoreRepository;", "buildConfig", "Lcom/gigforce/core/di/interfaces/IBuildConfigVM;", "(Lcom/gigforce/wallet/WalletfirestoreRepository;Lcom/gigforce/core/di/repo/IProfileFirestoreRepository;Lcom/gigforce/core/di/interfaces/IBuildConfigVM;)V", "_downloadPaySlip", "Landroidx/lifecycle/MutableLiveData;", "Lcom/gigforce/core/utils/Lce;", "Ljava/io/File;", "_monthlySlips", "", "Lcom/gigforce/wallet/models/Payslip;", "_userProfileData", "Lcom/gigforce/core/datamodels/profile/ProfileData;", "downloadPaySlip", "Landroidx/lifecycle/LiveData;", "getDownloadPaySlip", "()Landroidx/lifecycle/LiveData;", "monthlySlips", "getMonthlySlips", "paySlipService", "Lcom/gigforce/core/retrofit/GeneratePaySlipService;", "profileListenerRegistration", "Lcom/google/firebase/firestore/ListenerRegistration;", "userProfileData", "getUserProfileData", "setUserProfileData", "(Landroidx/lifecycle/LiveData;)V", "downloadAndSavePaySlip", "pdfDownloadLink", "", "filesDir", "(Ljava/lang/String;Ljava/io/File;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Lkotlinx/coroutines/Job;", "payslip", "generatePaySlip", "Lcom/gigforce/core/retrofit/PaySlipResponseModel;", "payslipId", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getPaySlips", "getProfileData", "", "onCleared", "writeResponseBodyToDisk", "", "body", "Lokhttp3/ResponseBody;", "destFile", "wallet_debug"})
public final class PayslipMonthlyViewModel extends androidx.lifecycle.ViewModel {
    private com.gigforce.core.retrofit.GeneratePaySlipService paySlipService;
    private com.google.firebase.firestore.ListenerRegistration profileListenerRegistration;
    private final androidx.lifecycle.MutableLiveData<com.gigforce.core.datamodels.profile.ProfileData> _userProfileData = null;
    @org.jetbrains.annotations.NotNull()
    private androidx.lifecycle.LiveData<com.gigforce.core.datamodels.profile.ProfileData> userProfileData;
    private final androidx.lifecycle.MutableLiveData<com.gigforce.core.utils.Lce<java.util.List<com.gigforce.wallet.models.Payslip>>> _monthlySlips = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<com.gigforce.core.utils.Lce<java.util.List<com.gigforce.wallet.models.Payslip>>> monthlySlips = null;
    private final androidx.lifecycle.MutableLiveData<com.gigforce.core.utils.Lce<java.io.File>> _downloadPaySlip = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<com.gigforce.core.utils.Lce<java.io.File>> downloadPaySlip = null;
    private final com.gigforce.wallet.WalletfirestoreRepository walletRepository = null;
    private final com.gigforce.core.di.repo.IProfileFirestoreRepository profileFirebaseRepository = null;
    private final com.gigforce.core.di.interfaces.IBuildConfigVM buildConfig = null;
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<com.gigforce.core.datamodels.profile.ProfileData> getUserProfileData() {
        return null;
    }
    
    public final void setUserProfileData(@org.jetbrains.annotations.NotNull()
    androidx.lifecycle.LiveData<com.gigforce.core.datamodels.profile.ProfileData> p0) {
    }
    
    public final void getProfileData() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<com.gigforce.core.utils.Lce<java.util.List<com.gigforce.wallet.models.Payslip>>> getMonthlySlips() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job getPaySlips() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<com.gigforce.core.utils.Lce<java.io.File>> getDownloadPaySlip() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job downloadPaySlip(@org.jetbrains.annotations.NotNull()
    com.gigforce.wallet.models.Payslip payslip, @org.jetbrains.annotations.NotNull()
    java.io.File filesDir) {
        return null;
    }
    
    private final boolean writeResponseBodyToDisk(okhttp3.ResponseBody body, java.io.File destFile) {
        return false;
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
    
    @javax.inject.Inject()
    public PayslipMonthlyViewModel(@org.jetbrains.annotations.NotNull()
    com.gigforce.wallet.WalletfirestoreRepository walletRepository, @org.jetbrains.annotations.NotNull()
    com.gigforce.core.di.repo.IProfileFirestoreRepository profileFirebaseRepository, @org.jetbrains.annotations.NotNull()
    com.gigforce.core.di.interfaces.IBuildConfigVM buildConfig) {
        super();
    }
}