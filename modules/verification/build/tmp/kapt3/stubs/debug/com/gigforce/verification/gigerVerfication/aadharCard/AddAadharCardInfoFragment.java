package com.gigforce.verification.gigerVerfication.aadharCard;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0080\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0011\b\u0007\u0018\u0000 E2\u00020\u00012\u00020\u0002:\u0001EB\u0005\u00a2\u0006\u0002\u0010\u0003J\b\u0010\u001b\u001a\u00020\u001cH\u0002J\b\u0010\u001d\u001a\u00020\u001cH\u0002J\b\u0010\u001e\u001a\u00020\u001cH\u0002J\u0010\u0010\u001f\u001a\u00020\u001c2\u0006\u0010 \u001a\u00020!H\u0002J\b\u0010\"\u001a\u00020\u001cH\u0002J\b\u0010#\u001a\u00020\u001cH\u0002J\b\u0010$\u001a\u00020\u001cH\u0002J\"\u0010%\u001a\u00020\u001c2\u0006\u0010&\u001a\u00020\'2\u0006\u0010(\u001a\u00020\'2\b\u0010)\u001a\u0004\u0018\u00010*H\u0016J\b\u0010+\u001a\u00020,H\u0016J,\u0010-\u001a\n /*\u0004\u0018\u00010.0.2\u0006\u00100\u001a\u0002012\b\u00102\u001a\u0004\u0018\u0001032\b\u00104\u001a\u0004\u0018\u000105H\u0016J\u001a\u00106\u001a\u00020\u001c2\u0006\u00107\u001a\u00020.2\b\u00104\u001a\u0004\u0018\u000105H\u0016J\b\u00108\u001a\u00020\u001cH\u0002J\b\u00109\u001a\u00020\u001cH\u0002J\u0012\u0010:\u001a\u00020\u001c2\b\u0010;\u001a\u0004\u0018\u00010\u0007H\u0002J\u0010\u0010<\u001a\u00020\u001c2\u0006\u0010=\u001a\u00020\u000eH\u0002J\b\u0010>\u001a\u00020\u001cH\u0002J\u0010\u0010?\u001a\u00020\u001c2\u0006\u0010\u0004\u001a\u00020\u0005H\u0002J\b\u0010@\u001a\u00020\u001cH\u0002J\u0010\u0010A\u001a\u00020\u001c2\u0006\u0010\b\u001a\u00020\u0005H\u0002J\b\u0010B\u001a\u00020\u001cH\u0002J\b\u0010C\u001a\u00020\u001cH\u0002J\b\u0010D\u001a\u00020\u001cH\u0002R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u000f\u001a\u00020\u00108\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R\u001b\u0010\u0015\u001a\u00020\u00168BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0019\u0010\u001a\u001a\u0004\b\u0017\u0010\u0018\u00a8\u0006F"}, d2 = {"Lcom/gigforce/verification/gigerVerfication/aadharCard/AddAadharCardInfoFragment;", "Landroidx/fragment/app/Fragment;", "Lcom/gigforce/common_ui/core/IOnBackPressedOverride;", "()V", "aadharBackImagePath", "Landroid/net/Uri;", "aadharCardDataModel", "Lcom/gigforce/core/datamodels/verification/AadharCardDataModel;", "aadharFrontImagePath", "currentlyClickingImageOfSide", "Lcom/gigforce/verification/gigerVerfication/aadharCard/AadharCardSides;", "firebaseStorage", "Lcom/google/firebase/storage/FirebaseStorage;", "gigerVerificationStatus", "Lcom/gigforce/verification/gigerVerfication/GigerVerificationStatus;", "navigation", "Lcom/gigforce/core/navigation/INavigation;", "getNavigation", "()Lcom/gigforce/core/navigation/INavigation;", "setNavigation", "(Lcom/gigforce/core/navigation/INavigation;)V", "viewModel", "Lcom/gigforce/verification/gigerVerfication/GigVerificationViewModel;", "getViewModel", "()Lcom/gigforce/verification/gigerVerfication/GigVerificationViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "disableSubmitButton", "", "documentUploaded", "enableSubmitButton", "errorOnUploadingDocuments", "error", "", "hideAadharImageAndInfoLayout", "initViewModel", "initViews", "onActivityResult", "requestCode", "", "resultCode", "data", "Landroid/content/Intent;", "onBackPressed", "", "onCreateView", "Landroid/view/View;", "kotlin.jvm.PlatformType", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onViewCreated", "view", "openCameraAndGalleryOptionForBackSideImage", "openCameraAndGalleryOptionForFrontSideImage", "setDataOnEditLayout", "it", "setDataOnViewLayout", "gigVerificationStatus", "showAadharImageAndInfoLayout", "showBackAadharCard", "showDetailsUploaded", "showFrontAadharCard", "showImageInfoLayout", "showLoadingState", "showWhyWeNeedThisBottomSheet", "Companion", "verification_debug"})
@dagger.hilt.android.AndroidEntryPoint()
public final class AddAadharCardInfoFragment extends androidx.fragment.app.Fragment implements com.gigforce.common_ui.core.IOnBackPressedOverride {
    private final kotlin.Lazy viewModel$delegate = null;
    private com.gigforce.verification.gigerVerfication.GigerVerificationStatus gigerVerificationStatus;
    private com.gigforce.core.datamodels.verification.AadharCardDataModel aadharCardDataModel;
    private android.net.Uri aadharFrontImagePath;
    private android.net.Uri aadharBackImagePath;
    private com.gigforce.verification.gigerVerfication.aadharCard.AadharCardSides currentlyClickingImageOfSide;
    @javax.inject.Inject()
    public com.gigforce.core.navigation.INavigation navigation;
    private final com.google.firebase.storage.FirebaseStorage firebaseStorage = null;
    public static final int REQUEST_CODE_UPLOAD_AADHAR_IMAGE = 2333;
    @org.jetbrains.annotations.NotNull()
    public static final com.gigforce.verification.gigerVerfication.aadharCard.AddAadharCardInfoFragment.Companion Companion = null;
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
    
    private final void showWhyWeNeedThisBottomSheet() {
    }
    
    private final void initViewModel() {
    }
    
    private final void setDataOnViewLayout(com.gigforce.verification.gigerVerfication.GigerVerificationStatus gigVerificationStatus) {
    }
    
    private final void setDataOnEditLayout(com.gigforce.core.datamodels.verification.AadharCardDataModel it) {
    }
    
    private final void errorOnUploadingDocuments(java.lang.String error) {
    }
    
    private final void documentUploaded() {
    }
    
    private final void showDetailsUploaded() {
    }
    
    private final void showLoadingState() {
    }
    
    @java.lang.Override()
    public boolean onBackPressed() {
        return false;
    }
    
    private final void openCameraAndGalleryOptionForFrontSideImage() {
    }
    
    private final void openCameraAndGalleryOptionForBackSideImage() {
    }
    
    @java.lang.Override()
    public void onActivityResult(int requestCode, int resultCode, @org.jetbrains.annotations.Nullable()
    android.content.Intent data) {
    }
    
    private final void showAadharImageAndInfoLayout() {
    }
    
    private final void hideAadharImageAndInfoLayout() {
    }
    
    private final void enableSubmitButton() {
    }
    
    private final void disableSubmitButton() {
    }
    
    private final void showImageInfoLayout() {
    }
    
    private final void showFrontAadharCard(android.net.Uri aadharFrontImagePath) {
    }
    
    private final void showBackAadharCard(android.net.Uri aadharBackImagePath) {
    }
    
    public AddAadharCardInfoFragment() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/gigforce/verification/gigerVerfication/aadharCard/AddAadharCardInfoFragment$Companion;", "", "()V", "REQUEST_CODE_UPLOAD_AADHAR_IMAGE", "", "verification_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}