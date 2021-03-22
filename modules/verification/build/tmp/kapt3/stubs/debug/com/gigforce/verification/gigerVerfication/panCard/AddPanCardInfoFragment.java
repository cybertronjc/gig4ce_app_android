package com.gigforce.verification.gigerVerfication.panCard;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0084\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0010\b\u0007\u0018\u0000 E2\u00020\u00012\u00020\u00022\u00020\u0003:\u0001EB\u0005\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0019\u001a\u00020\u001aH\u0002J\b\u0010\u001b\u001a\u00020\u001aH\u0002J\u0010\u0010\u001c\u001a\u00020\u001a2\u0006\u0010\u001d\u001a\u00020\u001eH\u0002J\b\u0010\u001f\u001a\u00020\u001aH\u0002J\b\u0010 \u001a\u00020\u001aH\u0002J\b\u0010!\u001a\u00020\u001aH\u0002J\b\u0010\"\u001a\u00020\u001aH\u0002J\"\u0010#\u001a\u00020\u001a2\u0006\u0010$\u001a\u00020%2\u0006\u0010&\u001a\u00020%2\b\u0010\'\u001a\u0004\u0018\u00010(H\u0016J\b\u0010)\u001a\u00020*H\u0016J,\u0010+\u001a\n -*\u0004\u0018\u00010,0,2\u0006\u0010.\u001a\u00020/2\b\u00100\u001a\u0004\u0018\u0001012\b\u00102\u001a\u0004\u0018\u000103H\u0016J\u0010\u00104\u001a\u00020\u001a2\u0006\u00105\u001a\u000206H\u0016J\u001a\u00107\u001a\u00020\u001a2\u0006\u00108\u001a\u00020,2\b\u00102\u001a\u0004\u0018\u000103H\u0016J\b\u00109\u001a\u00020\u001aH\u0002J\u0012\u0010:\u001a\u00020\u001a2\b\u0010;\u001a\u0004\u0018\u00010\u0012H\u0002J\u0010\u0010<\u001a\u00020\u001a2\u0006\u0010=\u001a\u00020\nH\u0002J\b\u0010>\u001a\u00020\u001aH\u0002J\b\u0010?\u001a\u00020\u001aH\u0002J\b\u0010@\u001a\u00020\u001aH\u0002J\b\u0010A\u001a\u00020\u001aH\u0002J\u0010\u0010B\u001a\u00020\u001a2\u0006\u0010C\u001a\u00020\u0006H\u0002J\b\u0010D\u001a\u00020\u001aH\u0002R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u000b\u001a\u00020\f8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0013\u001a\u00020\u00148BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0017\u0010\u0018\u001a\u0004\b\u0015\u0010\u0016\u00a8\u0006F"}, d2 = {"Lcom/gigforce/verification/gigerVerfication/panCard/AddPanCardInfoFragment;", "Landroidx/fragment/app/Fragment;", "Lcom/gigforce/verification/gigerVerfication/SelectImageSourceBottomSheetActionListener;", "Lcom/gigforce/common_ui/core/IOnBackPressedOverride;", "()V", "clickedImagePath", "Landroid/net/Uri;", "firebaseStorage", "Lcom/google/firebase/storage/FirebaseStorage;", "gigerVerificationStatus", "Lcom/gigforce/verification/gigerVerfication/GigerVerificationStatus;", "navigation", "Lcom/gigforce/core/navigation/INavigation;", "getNavigation", "()Lcom/gigforce/core/navigation/INavigation;", "setNavigation", "(Lcom/gigforce/core/navigation/INavigation;)V", "panCardDataModel", "Lcom/gigforce/core/datamodels/verification/PanCardDataModel;", "viewModel", "Lcom/gigforce/verification/gigerVerfication/GigVerificationViewModel;", "getViewModel", "()Lcom/gigforce/verification/gigerVerfication/GigVerificationViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "disableSubmitButton", "", "enableSubmitButton", "errorOnUploadingDocuments", "error", "", "hidePanImageAndInfoLayout", "initViewModel", "initViews", "launchSelectImageSourceDialog", "onActivityResult", "requestCode", "", "resultCode", "data", "Landroid/content/Intent;", "onBackPressed", "", "onCreateView", "Landroid/view/View;", "kotlin.jvm.PlatformType", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onImageSourceSelected", "source", "Lcom/gigforce/verification/gigerVerfication/ImageSource;", "onViewCreated", "view", "panCardDocumentUploaded", "setDataOnEditLayout", "it", "setDataOnViewLayout", "gigVerificationStatus", "showDetailsUploaded", "showImageInfoLayout", "showLoadingState", "showPanImageLayout", "showPanInfoCard", "panInfoPath", "showWhyWeNeedThisDialog", "Companion", "verification_debug"})
@dagger.hilt.android.AndroidEntryPoint()
public final class AddPanCardInfoFragment extends androidx.fragment.app.Fragment implements com.gigforce.verification.gigerVerfication.SelectImageSourceBottomSheetActionListener, com.gigforce.common_ui.core.IOnBackPressedOverride {
    private com.gigforce.core.datamodels.verification.PanCardDataModel panCardDataModel;
    private final kotlin.Lazy viewModel$delegate = null;
    private android.net.Uri clickedImagePath;
    private final com.google.firebase.storage.FirebaseStorage firebaseStorage = null;
    private com.gigforce.verification.gigerVerfication.GigerVerificationStatus gigerVerificationStatus;
    @javax.inject.Inject()
    public com.gigforce.core.navigation.INavigation navigation;
    public static final int REQUEST_CODE_UPLOAD_PAN_IMAGE = 2333;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String INTENT_EXTRA_CLICKED_IMAGE_PATH = "clicked_image_path";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String INTENT_EXTRA_PAN = "pan";
    @org.jetbrains.annotations.NotNull()
    public static final com.gigforce.verification.gigerVerfication.panCard.AddPanCardInfoFragment.Companion Companion = null;
    private java.util.HashMap _$_findViewCache;
    
    private final com.gigforce.verification.gigerVerfication.GigVerificationViewModel getViewModel() {
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
    
    private final void initViews() {
    }
    
    private final void showWhyWeNeedThisDialog() {
    }
    
    private final void initViewModel() {
    }
    
    private final void setDataOnViewLayout(com.gigforce.verification.gigerVerfication.GigerVerificationStatus gigVerificationStatus) {
    }
    
    private final void setDataOnEditLayout(com.gigforce.core.datamodels.verification.PanCardDataModel it) {
    }
    
    private final void errorOnUploadingDocuments(java.lang.String error) {
    }
    
    private final void panCardDocumentUploaded() {
    }
    
    private final void showDetailsUploaded() {
    }
    
    private final void showLoadingState() {
    }
    
    @java.lang.Override()
    public boolean onBackPressed() {
        return false;
    }
    
    private final void launchSelectImageSourceDialog() {
    }
    
    @java.lang.Override()
    public void onActivityResult(int requestCode, int resultCode, @org.jetbrains.annotations.Nullable()
    android.content.Intent data) {
    }
    
    private final void showImageInfoLayout() {
    }
    
    private final void showPanImageLayout() {
    }
    
    private final void hidePanImageAndInfoLayout() {
    }
    
    private final void enableSubmitButton() {
    }
    
    private final void disableSubmitButton() {
    }
    
    @java.lang.Override()
    public void onImageSourceSelected(@org.jetbrains.annotations.NotNull()
    com.gigforce.verification.gigerVerfication.ImageSource source) {
    }
    
    private final void showPanInfoCard(android.net.Uri panInfoPath) {
    }
    
    public AddPanCardInfoFragment() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/gigforce/verification/gigerVerfication/panCard/AddPanCardInfoFragment$Companion;", "", "()V", "INTENT_EXTRA_CLICKED_IMAGE_PATH", "", "INTENT_EXTRA_PAN", "REQUEST_CODE_UPLOAD_PAN_IMAGE", "", "verification_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}