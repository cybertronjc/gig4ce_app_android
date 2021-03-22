package com.gigforce.verification.oldverification;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\t0\bJ\b\u0010\u0015\u001a\u00020\u0016H\u0014J\u001e\u0010\u0017\u001a\u00020\u00162\u0016\u0010\u0018\u001a\u0012\u0012\u0004\u0012\u00020\u001a0\u0019j\b\u0012\u0004\u0012\u00020\u001a`\u001bJ\u000e\u0010\u001c\u001a\u00020\u00162\u0006\u0010\u001d\u001a\u00020\u0004J\u001e\u0010\u001e\u001a\u00020\u00162\u0016\u0010\u001f\u001a\u0012\u0012\u0004\u0012\u00020 0\u0019j\b\u0012\u0004\u0012\u00020 `\u001bJ\u001e\u0010!\u001a\u00020\u00162\u0016\u0010\"\u001a\u0012\u0012\u0004\u0012\u00020#0\u0019j\b\u0012\u0004\u0012\u00020#`\u001bJ\u001e\u0010$\u001a\u00020\u00162\u0016\u0010%\u001a\u0012\u0012\u0004\u0012\u00020&0\u0019j\b\u0012\u0004\u0012\u00020&`\u001bJ\u001e\u0010\'\u001a\u00020\u00162\u0016\u0010(\u001a\u0012\u0012\u0004\u0012\u00020)0\u0019j\b\u0012\u0004\u0012\u00020)`\u001bR\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R \u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR\u001a\u0010\u000e\u001a\u00020\u000fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013\u00a8\u0006*"}, d2 = {"Lcom/gigforce/verification/oldverification/VerificationViewModel;", "Landroidx/lifecycle/ViewModel;", "()V", "uid", "", "getUid", "()Ljava/lang/String;", "veriData", "Landroidx/lifecycle/MutableLiveData;", "Lcom/gigforce/core/datamodels/verification/KYCdata;", "getVeriData", "()Landroidx/lifecycle/MutableLiveData;", "setVeriData", "(Landroidx/lifecycle/MutableLiveData;)V", "veriFirebaseRepository", "Lcom/gigforce/verification/oldverification/VeriFirebaseRepository;", "getVeriFirebaseRepository", "()Lcom/gigforce/verification/oldverification/VeriFirebaseRepository;", "setVeriFirebaseRepository", "(Lcom/gigforce/verification/oldverification/VeriFirebaseRepository;)V", "getVerificationData", "onCleared", "", "setBank", "banks", "Ljava/util/ArrayList;", "Lcom/gigforce/core/datamodels/verification/Bank;", "Lkotlin/collections/ArrayList;", "setCardAvatarName", "cardAvatarName", "setVerificationContact", "contacts", "Lcom/gigforce/core/datamodels/verification/Address;", "setVerificationDL", "dls", "Lcom/gigforce/core/datamodels/verification/DL;", "setVerificationPassport", "passports", "Lcom/gigforce/core/datamodels/verification/Passport;", "setVerificationVoterID", "voterids", "Lcom/gigforce/core/datamodels/verification/VoterID;", "verification_debug"})
public final class VerificationViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private com.gigforce.verification.oldverification.VeriFirebaseRepository veriFirebaseRepository;
    @org.jetbrains.annotations.NotNull()
    private androidx.lifecycle.MutableLiveData<com.gigforce.core.datamodels.verification.KYCdata> veriData;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String uid = null;
    
    @org.jetbrains.annotations.NotNull()
    public final com.gigforce.verification.oldverification.VeriFirebaseRepository getVeriFirebaseRepository() {
        return null;
    }
    
    public final void setVeriFirebaseRepository(@org.jetbrains.annotations.NotNull()
    com.gigforce.verification.oldverification.VeriFirebaseRepository p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<com.gigforce.core.datamodels.verification.KYCdata> getVeriData() {
        return null;
    }
    
    public final void setVeriData(@org.jetbrains.annotations.NotNull()
    androidx.lifecycle.MutableLiveData<com.gigforce.core.datamodels.verification.KYCdata> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUid() {
        return null;
    }
    
    public final void setCardAvatarName(@org.jetbrains.annotations.NotNull()
    java.lang.String cardAvatarName) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<com.gigforce.core.datamodels.verification.KYCdata> getVerificationData() {
        return null;
    }
    
    public final void setVerificationContact(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.core.datamodels.verification.Address> contacts) {
    }
    
    public final void setVerificationDL(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.core.datamodels.verification.DL> dls) {
    }
    
    public final void setVerificationVoterID(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.core.datamodels.verification.VoterID> voterids) {
    }
    
    public final void setVerificationPassport(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.core.datamodels.verification.Passport> passports) {
    }
    
    public final void setBank(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.core.datamodels.verification.Bank> banks) {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
    
    public VerificationViewModel() {
        super();
    }
}