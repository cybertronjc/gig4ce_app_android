package com.gigforce.verification.gigerVerfication;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000v\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\u0015\n\u0002\b\u0007\b\u0007\u0018\u00002\u00020\u00012\u00020\u0002B\u0005\u00a2\u0006\u0002\u0010\u0003J\u0012\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u0002J\b\u0010\u0018\u001a\u00020\u0015H\u0002J\u0010\u0010\u0019\u001a\u00020\u00152\u0006\u0010\u001a\u001a\u00020\u001bH\u0002J\b\u0010\u001c\u001a\u00020\u0015H\u0002J\b\u0010\u001d\u001a\u00020\u0015H\u0002J\"\u0010\u001e\u001a\u00020\u00152\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020 2\b\u0010\"\u001a\u0004\u0018\u00010#H\u0016J\b\u0010$\u001a\u00020\rH\u0016J,\u0010%\u001a\n \'*\u0004\u0018\u00010&0&2\u0006\u0010(\u001a\u00020)2\b\u0010*\u001a\u0004\u0018\u00010+2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u0016J-\u0010,\u001a\u00020\u00152\u0006\u0010\u001f\u001a\u00020 2\u000e\u0010-\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u001b0.2\u0006\u0010/\u001a\u000200H\u0016\u00a2\u0006\u0002\u00101J\u0010\u00102\u001a\u00020\u00152\u0006\u00103\u001a\u00020\u0017H\u0016J\u001a\u00104\u001a\u00020\u00152\u0006\u00105\u001a\u00020&2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u0016J\b\u00106\u001a\u00020\u0015H\u0002R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0006\u001a\u00020\u00078\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR\u000e\u0010\f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u000e\u001a\u00020\u000f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0012\u0010\u0013\u001a\u0004\b\u0010\u0010\u0011\u00a8\u00067"}, d2 = {"Lcom/gigforce/verification/gigerVerfication/GigerVerificationFragment;", "Landroidx/fragment/app/Fragment;", "Lcom/gigforce/common_ui/core/IOnBackPressedOverride;", "()V", "gigerVerificationStatus", "Lcom/gigforce/verification/gigerVerfication/GigerVerificationStatus;", "navigation", "Lcom/gigforce/core/navigation/INavigation;", "getNavigation", "()Lcom/gigforce/core/navigation/INavigation;", "setNavigation", "(Lcom/gigforce/core/navigation/INavigation;)V", "showActionButtons", "", "viewModel", "Lcom/gigforce/verification/gigerVerfication/GigVerificationViewModel;", "getViewModel", "()Lcom/gigforce/verification/gigerVerfication/GigVerificationViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "checkForBundleData", "", "savedInstanceState", "Landroid/os/Bundle;", "checkForContract", "downloadCertificate", "url", "", "initView", "initViewModel", "onActivityResult", "requestCode", "", "resultCode", "data", "Landroid/content/Intent;", "onBackPressed", "onCreateView", "Landroid/view/View;", "kotlin.jvm.PlatformType", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "onRequestPermissionsResult", "permissions", "", "grantResults", "", "(I[Ljava/lang/String;[I)V", "onSaveInstanceState", "outState", "onViewCreated", "view", "setListeners", "verification_debug"})
@dagger.hilt.android.AndroidEntryPoint()
public final class GigerVerificationFragment extends androidx.fragment.app.Fragment implements com.gigforce.common_ui.core.IOnBackPressedOverride {
    private final kotlin.Lazy viewModel$delegate = null;
    private com.gigforce.verification.gigerVerfication.GigerVerificationStatus gigerVerificationStatus;
    private boolean showActionButtons = false;
    @javax.inject.Inject()
    public com.gigforce.core.navigation.INavigation navigation;
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
    
    private final void checkForBundleData(android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    public void onSaveInstanceState(@org.jetbrains.annotations.NotNull()
    android.os.Bundle outState) {
    }
    
    private final void checkForContract() {
    }
    
    @java.lang.Override()
    public void onRequestPermissionsResult(int requestCode, @org.jetbrains.annotations.NotNull()
    java.lang.String[] permissions, @org.jetbrains.annotations.NotNull()
    int[] grantResults) {
    }
    
    @java.lang.Override()
    public void onActivityResult(int requestCode, int resultCode, @org.jetbrains.annotations.Nullable()
    android.content.Intent data) {
    }
    
    private final void initView() {
    }
    
    @java.lang.Override()
    public boolean onBackPressed() {
        return false;
    }
    
    private final void setListeners() {
    }
    
    private final void initViewModel() {
    }
    
    private final void downloadCertificate(java.lang.String url) {
    }
    
    public GigerVerificationFragment() {
        super();
    }
}