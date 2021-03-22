package com.gigforce.wallet;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 \u000e2\u00020\u0001:\u0001\u000eB\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\b\u0010\t\u001a\u00020\u0004H\u0016J\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\rR\u001a\u0010\u0003\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\b\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u000f"}, d2 = {"Lcom/gigforce/wallet/WalletfirestoreRepository;", "Lcom/gigforce/core/base/basefirestore/BaseFirestoreDBRepository;", "()V", "COLLECTION_NAME", "", "getCOLLECTION_NAME", "()Ljava/lang/String;", "setCOLLECTION_NAME", "(Ljava/lang/String;)V", "getCollectionName", "getPaySlips", "", "Lcom/gigforce/wallet/models/Payslip;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "wallet_debug"})
public final class WalletfirestoreRepository extends com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository {
    @org.jetbrains.annotations.NotNull()
    private java.lang.String COLLECTION_NAME = "Wallets";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String COLLECTION_PAYSLIPS = "Payslips";
    @org.jetbrains.annotations.NotNull()
    public static final com.gigforce.wallet.WalletfirestoreRepository.Companion Companion = null;
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCOLLECTION_NAME() {
        return null;
    }
    
    public final void setCOLLECTION_NAME(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String getCollectionName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getPaySlips(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.gigforce.wallet.models.Payslip>> p0) {
        return null;
    }
    
    @javax.inject.Inject()
    public WalletfirestoreRepository() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/gigforce/wallet/WalletfirestoreRepository$Companion;", "", "()V", "COLLECTION_PAYSLIPS", "", "wallet_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}