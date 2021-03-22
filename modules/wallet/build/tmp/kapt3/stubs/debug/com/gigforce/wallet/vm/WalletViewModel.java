package com.gigforce.wallet.vm;

import java.lang.System;

@dagger.hilt.android.lifecycle.HiltViewModel()
@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0016\u001a\u00020\u0017J\b\u0010\u000e\u001a\u00020\u0017H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR \u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\t\"\u0004\b\u000f\u0010\u000bR\u001a\u0010\u0010\u001a\u00020\u0011X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0013\"\u0004\b\u0014\u0010\u0015\u00a8\u0006\u0018"}, d2 = {"Lcom/gigforce/wallet/vm/WalletViewModel;", "Landroidx/lifecycle/ViewModel;", "profileFirebaseRepository", "Lcom/gigforce/core/di/repo/IProfileFirestoreRepository;", "(Lcom/gigforce/core/di/repo/IProfileFirestoreRepository;)V", "userProfileData", "Landroidx/lifecycle/MutableLiveData;", "Lcom/gigforce/core/datamodels/profile/ProfileData;", "getUserProfileData", "()Landroidx/lifecycle/MutableLiveData;", "setUserProfileData", "(Landroidx/lifecycle/MutableLiveData;)V", "userWallet", "Lcom/gigforce/wallet/models/Wallet;", "getUserWallet", "setUserWallet", "walletRepository", "Lcom/gigforce/wallet/WalletfirestoreRepository;", "getWalletRepository", "()Lcom/gigforce/wallet/WalletfirestoreRepository;", "setWalletRepository", "(Lcom/gigforce/wallet/WalletfirestoreRepository;)V", "getProfileData", "", "wallet_debug"})
public final class WalletViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private com.gigforce.wallet.WalletfirestoreRepository walletRepository;
    @org.jetbrains.annotations.NotNull()
    private androidx.lifecycle.MutableLiveData<com.gigforce.wallet.models.Wallet> userWallet;
    @org.jetbrains.annotations.NotNull()
    private androidx.lifecycle.MutableLiveData<com.gigforce.core.datamodels.profile.ProfileData> userProfileData;
    private final com.gigforce.core.di.repo.IProfileFirestoreRepository profileFirebaseRepository = null;
    
    @org.jetbrains.annotations.NotNull()
    public final com.gigforce.wallet.WalletfirestoreRepository getWalletRepository() {
        return null;
    }
    
    public final void setWalletRepository(@org.jetbrains.annotations.NotNull()
    com.gigforce.wallet.WalletfirestoreRepository p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<com.gigforce.wallet.models.Wallet> getUserWallet() {
        return null;
    }
    
    public final void setUserWallet(@org.jetbrains.annotations.NotNull()
    androidx.lifecycle.MutableLiveData<com.gigforce.wallet.models.Wallet> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<com.gigforce.core.datamodels.profile.ProfileData> getUserProfileData() {
        return null;
    }
    
    public final void setUserProfileData(@org.jetbrains.annotations.NotNull()
    androidx.lifecycle.MutableLiveData<com.gigforce.core.datamodels.profile.ProfileData> p0) {
    }
    
    public final void getProfileData() {
    }
    
    private final void getUserWallet() {
    }
    
    @javax.inject.Inject()
    public WalletViewModel(@org.jetbrains.annotations.NotNull()
    com.gigforce.core.di.repo.IProfileFirestoreRepository profileFirebaseRepository) {
        super();
    }
}