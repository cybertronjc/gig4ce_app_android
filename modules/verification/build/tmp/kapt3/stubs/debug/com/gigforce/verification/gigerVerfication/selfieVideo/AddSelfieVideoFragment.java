package com.gigforce.verification.gigerVerfication.selfieVideo;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000x\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\b\u0007\u0018\u00002\u00020\u00012\u00020\u00022\u00020\u00032\u00020\u0004B\u0005\u00a2\u0006\u0002\u0010\u0005J\b\u0010\u001c\u001a\u00020\u001dH\u0002J\u0010\u0010\u001e\u001a\u00020\u001d2\u0006\u0010\u001f\u001a\u00020 H\u0002J\b\u0010!\u001a\u00020\u001dH\u0002J\b\u0010\"\u001a\u00020\u001dH\u0002J\b\u0010#\u001a\u00020\u001dH\u0016J\b\u0010$\u001a\u00020\u001dH\u0002J\b\u0010%\u001a\u00020\u001dH\u0002J\b\u0010&\u001a\u00020\u001dH\u0002J\b\u0010\'\u001a\u00020\u001dH\u0002J\b\u0010(\u001a\u00020)H\u0016J,\u0010*\u001a\n ,*\u0004\u0018\u00010+0+2\u0006\u0010-\u001a\u00020.2\b\u0010/\u001a\u0004\u0018\u0001002\b\u00101\u001a\u0004\u0018\u000102H\u0016J\u001a\u00103\u001a\u00020\u001d2\u0006\u00104\u001a\u00020+2\b\u00101\u001a\u0004\u0018\u000102H\u0016J\u0010\u00105\u001a\u00020\u001d2\u0006\u00106\u001a\u00020\rH\u0002J\b\u00107\u001a\u00020\u001dH\u0002J\b\u00108\u001a\u00020\u001dH\u0002J\u0010\u00109\u001a\u00020\u001d2\u0006\u00106\u001a\u00020\rH\u0016R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u000e\u001a\u00020\u000f8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0016\u001a\u00020\u00178BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001a\u0010\u001b\u001a\u0004\b\u0018\u0010\u0019\u00a8\u0006:"}, d2 = {"Lcom/gigforce/verification/gigerVerfication/selfieVideo/AddSelfieVideoFragment;", "Landroidx/fragment/app/Fragment;", "Lcom/gigforce/verification/gigerVerfication/selfieVideo/CaptureVideoFragmentEventListener;", "Lcom/gigforce/common_ui/core/IOnBackPressedOverride;", "Lcom/gigforce/verification/gigerVerfication/selfieVideo/PlaySelfieVideoFragmentEventListener;", "()V", "captureSelfieVideoFragment", "Lcom/gigforce/verification/gigerVerfication/selfieVideo/CaptureSelfieVideoFragment;", "firebaseStorage", "Lcom/google/firebase/storage/FirebaseStorage;", "gigerVerificationStatus", "Lcom/gigforce/verification/gigerVerfication/GigerVerificationStatus;", "mCapturedVideoPath", "Ljava/io/File;", "navigation", "Lcom/gigforce/core/navigation/INavigation;", "getNavigation", "()Lcom/gigforce/core/navigation/INavigation;", "setNavigation", "(Lcom/gigforce/core/navigation/INavigation;)V", "playSelfieVideoFragment", "Lcom/gigforce/verification/gigerVerfication/selfieVideo/PlaySelfieVideoFragment;", "viewModel", "Lcom/gigforce/verification/gigerVerfication/selfieVideo/SelfiVideoViewModel;", "getViewModel", "()Lcom/gigforce/verification/gigerVerfication/selfieVideo/SelfiVideoViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "addCaptureVideoFragment", "", "addPlayVideoFragment", "remoteUri", "Landroid/net/Uri;", "deleteExistingVideoIfExist", "disableSubmitButton", "discardCurrentVideoAndStartRetakingVideo", "documentUploaded", "enableSubmitButton", "initViewModel", "initViews", "onBackPressed", "", "onCreateView", "Landroid/view/View;", "kotlin.jvm.PlatformType", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onViewCreated", "view", "replaceCaptureFragmentWithPreviewFragment", "file", "replacePlayVideoFragmentWithCaptureFragment", "showDetailsUploaded", "videoCaptured", "verification_debug"})
@dagger.hilt.android.AndroidEntryPoint()
public final class AddSelfieVideoFragment extends androidx.fragment.app.Fragment implements com.gigforce.verification.gigerVerfication.selfieVideo.CaptureVideoFragmentEventListener, com.gigforce.common_ui.core.IOnBackPressedOverride, com.gigforce.verification.gigerVerfication.selfieVideo.PlaySelfieVideoFragmentEventListener {
    private final kotlin.Lazy viewModel$delegate = null;
    private java.io.File mCapturedVideoPath;
    private com.gigforce.verification.gigerVerfication.selfieVideo.CaptureSelfieVideoFragment captureSelfieVideoFragment;
    private com.gigforce.verification.gigerVerfication.selfieVideo.PlaySelfieVideoFragment playSelfieVideoFragment;
    private final com.google.firebase.storage.FirebaseStorage firebaseStorage = null;
    private com.gigforce.verification.gigerVerfication.GigerVerificationStatus gigerVerificationStatus;
    @javax.inject.Inject()
    public com.gigforce.core.navigation.INavigation navigation;
    private java.util.HashMap _$_findViewCache;
    
    private final com.gigforce.verification.gigerVerfication.selfieVideo.SelfiVideoViewModel getViewModel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gigforce.core.navigation.INavigation getNavigation() {
        return null;
    }
    
    public final void setNavigation(@org.jetbrains.annotations.NotNull()
    com.gigforce.core.navigation.INavigation p0) {
    }
    
    @java.lang.Override()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void addCaptureVideoFragment() {
    }
    
    private final void addPlayVideoFragment(android.net.Uri remoteUri) {
    }
    
    private final void replaceCaptureFragmentWithPreviewFragment(java.io.File file) {
    }
    
    private final void replacePlayVideoFragmentWithCaptureFragment() {
    }
    
    private final void initViewModel() {
    }
    
    private final void documentUploaded() {
    }
    
    private final void showDetailsUploaded() {
    }
    
    private final void initViews() {
    }
    
    @java.lang.Override()
    public boolean onBackPressed() {
        return false;
    }
    
    private final void enableSubmitButton() {
    }
    
    private final void disableSubmitButton() {
    }
    
    private final void deleteExistingVideoIfExist() {
    }
    
    @java.lang.Override()
    public void videoCaptured(@org.jetbrains.annotations.NotNull()
    java.io.File file) {
    }
    
    @java.lang.Override()
    public void discardCurrentVideoAndStartRetakingVideo() {
    }
    
    public AddSelfieVideoFragment() {
        super();
    }
}