package com.gigforce.verification.gigerVerfication;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0012\u001a\u00020\u0013H\u0002J)\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00132\u0006\u0010\u0017\u001a\u00020\u00132\u0006\u0010\u0018\u001a\u00020\u0013H\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0019J(\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u00132\b\u0010\u001d\u001a\u0004\u0018\u00010\u001e2\u0006\u0010\u0017\u001a\u00020\u00132\u0006\u0010\u0018\u001a\u00020\u0013J\u0019\u0010\u001f\u001a\u00020\u00132\u0006\u0010 \u001a\u00020\u001eH\u0082@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010!R!\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u00048BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\b\u0010\t\u001a\u0004\b\u0006\u0010\u0007R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00050\u00048F\u00a2\u0006\u0006\u001a\u0004\b\u000b\u0010\u0007R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u000e\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\""}, d2 = {"Lcom/gigforce/verification/gigerVerfication/UploadDrivingCertificateViewmodel;", "Landroidx/lifecycle/ViewModel;", "()V", "_documentUploadState", "Lcom/gigforce/core/SingleLiveEvent;", "Lcom/gigforce/core/utils/Lse;", "get_documentUploadState", "()Lcom/gigforce/core/SingleLiveEvent;", "_documentUploadState$delegate", "Lkotlin/Lazy;", "documentUploadState", "getDocumentUploadState", "firebaseStorage", "Lcom/google/firebase/storage/FirebaseStorage;", "repository", "Lcom/gigforce/verification/gigerVerfication/UploadDrivingCertificateRepository;", "getRepository", "()Lcom/gigforce/verification/gigerVerfication/UploadDrivingCertificateRepository;", "prepareUniqueImageName", "", "setInJPApplication", "Lcom/gigforce/core/datamodels/client_activation/JpApplication;", "jobProfileID", "type", "title", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadDLCer", "Lkotlinx/coroutines/Job;", "mJobProfileId", "frontImagePath", "Landroid/net/Uri;", "uploadImage", "image", "(Landroid/net/Uri;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "verification_debug"})
public final class UploadDrivingCertificateViewmodel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.gigforce.verification.gigerVerfication.UploadDrivingCertificateRepository repository = null;
    private final kotlin.Lazy _documentUploadState$delegate = null;
    private final com.google.firebase.storage.FirebaseStorage firebaseStorage = null;
    
    @org.jetbrains.annotations.NotNull()
    public final com.gigforce.verification.gigerVerfication.UploadDrivingCertificateRepository getRepository() {
        return null;
    }
    
    private final com.gigforce.core.SingleLiveEvent<com.gigforce.core.utils.Lse> get_documentUploadState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gigforce.core.SingleLiveEvent<com.gigforce.core.utils.Lse> getDocumentUploadState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job uploadDLCer(@org.jetbrains.annotations.NotNull()
    java.lang.String mJobProfileId, @org.jetbrains.annotations.Nullable()
    android.net.Uri frontImagePath, @org.jetbrains.annotations.NotNull()
    java.lang.String type, @org.jetbrains.annotations.NotNull()
    java.lang.String title) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object setInJPApplication(@org.jetbrains.annotations.NotNull()
    java.lang.String jobProfileID, @org.jetbrains.annotations.NotNull()
    java.lang.String type, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.gigforce.core.datamodels.client_activation.JpApplication> p3) {
        return null;
    }
    
    private final java.lang.String prepareUniqueImageName() {
        return null;
    }
    
    public UploadDrivingCertificateViewmodel() {
        super();
    }
}