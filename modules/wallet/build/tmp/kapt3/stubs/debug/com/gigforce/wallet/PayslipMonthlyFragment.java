package com.gigforce.wallet;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0014\u001a\u00020\u0015H\u0002J\b\u0010\u0016\u001a\u00020\u0015H\u0002J,\u0010\u0017\u001a\n \u0019*\u0004\u0018\u00010\u00180\u00182\u0006\u0010\u001a\u001a\u00020\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016J\u001a\u0010 \u001a\u00020\u00152\u0006\u0010!\u001a\u00020\u00182\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016J\u0010\u0010\"\u001a\u00020\u00152\u0006\u0010#\u001a\u00020$H\u0002J\b\u0010%\u001a\u00020\u0015H\u0002J\u0010\u0010&\u001a\u00020\u00152\u0006\u0010\'\u001a\u00020(H\u0002J\u0010\u0010)\u001a\u00020\u00152\u0006\u0010\'\u001a\u00020(H\u0002J\b\u0010*\u001a\u00020\u0015H\u0002J\u0016\u0010+\u001a\u00020\u00152\f\u0010,\u001a\b\u0012\u0004\u0012\u00020.0-H\u0002R\u001e\u0010\u0003\u001a\u00020\u00048\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001b\u0010\t\u001a\u00020\n8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\r\u0010\u000e\u001a\u0004\b\u000b\u0010\fR\u001b\u0010\u000f\u001a\u00020\u00108BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0013\u0010\u000e\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006/"}, d2 = {"Lcom/gigforce/wallet/PayslipMonthlyFragment;", "Landroidx/fragment/app/Fragment;", "()V", "buildConfig", "Lcom/gigforce/core/di/interfaces/IBuildConfig;", "getBuildConfig", "()Lcom/gigforce/core/di/interfaces/IBuildConfig;", "setBuildConfig", "(Lcom/gigforce/core/di/interfaces/IBuildConfig;)V", "mAdapter", "Lcom/gigforce/wallet/adapters/MonthlyPayslipsAdapter;", "getMAdapter", "()Lcom/gigforce/wallet/adapters/MonthlyPayslipsAdapter;", "mAdapter$delegate", "Lkotlin/Lazy;", "viewModel", "Lcom/gigforce/wallet/vm/PayslipMonthlyViewModel;", "getViewModel", "()Lcom/gigforce/wallet/vm/PayslipMonthlyViewModel;", "viewModel$delegate", "initView", "", "initViewModel", "onCreateView", "Landroid/view/View;", "kotlin.jvm.PlatformType", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onViewCreated", "view", "openPdf", "file", "Ljava/io/File;", "showDownloadingDialog", "showErrorDialog", "error", "", "showErrorInLoadingPayslips", "showPayslipsLoadingLayout", "showPayslipsOnView", "content", "", "Lcom/gigforce/wallet/models/Payslip;", "wallet_debug"})
@dagger.hilt.android.AndroidEntryPoint()
public final class PayslipMonthlyFragment extends androidx.fragment.app.Fragment {
    private final kotlin.Lazy viewModel$delegate = null;
    @javax.inject.Inject()
    public com.gigforce.core.di.interfaces.IBuildConfig buildConfig;
    private final kotlin.Lazy mAdapter$delegate = null;
    private java.util.HashMap _$_findViewCache;
    
    private final com.gigforce.wallet.vm.PayslipMonthlyViewModel getViewModel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gigforce.core.di.interfaces.IBuildConfig getBuildConfig() {
        return null;
    }
    
    public final void setBuildConfig(@org.jetbrains.annotations.NotNull()
    com.gigforce.core.di.interfaces.IBuildConfig p0) {
    }
    
    private final com.gigforce.wallet.adapters.MonthlyPayslipsAdapter getMAdapter() {
        return null;
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
    
    private final void initView() {
    }
    
    private final void initViewModel() {
    }
    
    private final void showDownloadingDialog() {
    }
    
    private final void showPayslipsLoadingLayout() {
    }
    
    private final void showPayslipsOnView(java.util.List<com.gigforce.wallet.models.Payslip> content) {
    }
    
    private final void showErrorInLoadingPayslips(java.lang.String error) {
    }
    
    private final void openPdf(java.io.File file) {
    }
    
    private final void showErrorDialog(java.lang.String error) {
    }
    
    public PayslipMonthlyFragment() {
        super();
    }
}