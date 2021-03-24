package com.gigforce.giger_app.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import com.gigforce.core.navigation.INavigation;
import com.gigforce.giger_app.R;
import com.gigforce.giger_app.repo.ILoginInfoRepo;
import com.gigforce.giger_app.vm.LandingViewModel;
import dagger.hilt.android.AndroidEntryPoint;
import kotlinx.android.synthetic.main.fragment_landing.*;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0015\u001a\u00020\u0016H\u0002J\b\u0010\u0017\u001a\u00020\u0016H\u0002J&\u0010\u0018\u001a\u0004\u0018\u00010\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016J\u001a\u0010 \u001a\u00020\u00162\u0006\u0010!\u001a\u00020\u00192\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016R\u001e\u0010\u0003\u001a\u00020\u00048\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001e\u0010\t\u001a\u00020\n8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001b\u0010\u000f\u001a\u00020\u00108FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0013\u0010\u0014\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006\""}, d2 = {"Lcom/gigforce/giger_app/screens/LandingFragment;", "Landroidx/fragment/app/Fragment;", "()V", "loginInfo", "Lcom/gigforce/giger_app/repo/ILoginInfoRepo;", "getLoginInfo", "()Lcom/gigforce/giger_app/repo/ILoginInfoRepo;", "setLoginInfo", "(Lcom/gigforce/giger_app/repo/ILoginInfoRepo;)V", "navigation", "Lcom/gigforce/core/navigation/INavigation;", "getNavigation", "()Lcom/gigforce/core/navigation/INavigation;", "setNavigation", "(Lcom/gigforce/core/navigation/INavigation;)V", "viewModel", "Lcom/gigforce/giger_app/vm/LandingViewModel;", "getViewModel", "()Lcom/gigforce/giger_app/vm/LandingViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "initViews", "", "listeners", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onViewCreated", "view", "app-giger_debug"})
@dagger.hilt.android.AndroidEntryPoint()
public final class LandingFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    @javax.inject.Inject()
    public com.gigforce.giger_app.repo.ILoginInfoRepo loginInfo;
    @javax.inject.Inject()
    public com.gigforce.core.navigation.INavigation navigation;
    private java.util.HashMap _$_findViewCache;
    
    @org.jetbrains.annotations.NotNull()
    public final com.gigforce.giger_app.vm.LandingViewModel getViewModel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gigforce.giger_app.repo.ILoginInfoRepo getLoginInfo() {
        return null;
    }
    
    public final void setLoginInfo(@org.jetbrains.annotations.NotNull()
    com.gigforce.giger_app.repo.ILoginInfoRepo p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gigforce.core.navigation.INavigation getNavigation() {
        return null;
    }
    
    public final void setNavigation(@org.jetbrains.annotations.NotNull()
    com.gigforce.core.navigation.INavigation p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
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
    
    private final void listeners() {
    }
    
    public LandingFragment() {
        super();
    }
}