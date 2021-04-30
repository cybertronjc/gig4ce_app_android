package com.gigforce.verification.oldverification;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u0007\u0018\u0000 *2\u00020\u0001:\u0001*B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u001d\u001a\u00020\u001eH\u0002J&\u0010\u001f\u001a\u0004\u0018\u00010\u00042\u0006\u0010 \u001a\u00020!2\b\u0010\"\u001a\u0004\u0018\u00010#2\b\u0010$\u001a\u0004\u0018\u00010%H\u0016J\u001a\u0010&\u001a\u00020\u001e2\u0006\u0010\'\u001a\u00020\u00042\b\u0010$\u001a\u0004\u0018\u00010%H\u0016J\b\u0010(\u001a\u00020\u001eH\u0002J\b\u0010)\u001a\u00020\u001eH\u0002R\u001a\u0010\u0003\u001a\u00020\u0004X\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001e\u0010\t\u001a\u00020\n8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR*\u0010\u000f\u001a\u0012\u0012\u0004\u0012\u00020\u00110\u0010j\b\u0012\u0004\u0012\u00020\u0011`\u0012X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u0014\"\u0004\b\u0015\u0010\u0016R\u001a\u0010\u0017\u001a\u00020\u0018X\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u001a\"\u0004\b\u001b\u0010\u001c\u00a8\u0006+"}, d2 = {"Lcom/gigforce/verification/oldverification/AddressUpload;", "Lcom/google/android/material/bottomsheet/BottomSheetDialogFragment;", "()V", "layout", "Landroid/view/View;", "getLayout", "()Landroid/view/View;", "setLayout", "(Landroid/view/View;)V", "navigation", "Lcom/gigforce/core/navigation/INavigation;", "getNavigation", "()Lcom/gigforce/core/navigation/INavigation;", "setNavigation", "(Lcom/gigforce/core/navigation/INavigation;)V", "updates", "Ljava/util/ArrayList;", "Lcom/gigforce/core/datamodels/verification/Address;", "Lkotlin/collections/ArrayList;", "getUpdates", "()Ljava/util/ArrayList;", "setUpdates", "(Ljava/util/ArrayList;)V", "viewModel", "Lcom/gigforce/verification/oldverification/VerificationViewModel;", "getViewModel", "()Lcom/gigforce/verification/oldverification/VerificationViewModel;", "setViewModel", "(Lcom/gigforce/verification/oldverification/VerificationViewModel;)V", "addNewContact", "", "onCreateView", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onViewCreated", "view", "resetLayout", "saveNewContacts", "Companion", "verification_debug"})
@dagger.hilt.android.AndroidEntryPoint()
public final class AddressUpload extends com.google.android.material.bottomsheet.BottomSheetDialogFragment {
    public com.gigforce.verification.oldverification.VerificationViewModel viewModel;
    public android.view.View layout;
    @org.jetbrains.annotations.NotNull()
    private java.util.ArrayList<com.gigforce.core.datamodels.verification.Address> updates;
    @javax.inject.Inject()
    public com.gigforce.core.navigation.INavigation navigation;
    @org.jetbrains.annotations.NotNull()
    public static final com.gigforce.verification.oldverification.AddressUpload.Companion Companion = null;
    private java.util.HashMap _$_findViewCache;
    
    @org.jetbrains.annotations.NotNull()
    public final com.gigforce.verification.oldverification.VerificationViewModel getViewModel() {
        return null;
    }
    
    public final void setViewModel(@org.jetbrains.annotations.NotNull()
    com.gigforce.verification.oldverification.VerificationViewModel p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.view.View getLayout() {
        return null;
    }
    
    public final void setLayout(@org.jetbrains.annotations.NotNull()
    android.view.View p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.ArrayList<com.gigforce.core.datamodels.verification.Address> getUpdates() {
        return null;
    }
    
    public final void setUpdates(@org.jetbrains.annotations.NotNull()
    java.util.ArrayList<com.gigforce.core.datamodels.verification.Address> p0) {
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
    
    private final void addNewContact() {
    }
    
    private final void resetLayout() {
    }
    
    private final void saveNewContacts() {
    }
    
    public AddressUpload() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/gigforce/verification/oldverification/AddressUpload$Companion;", "", "()V", "newInstance", "Lcom/gigforce/verification/oldverification/AddressUpload;", "verification_debug"})
    public static final class Companion {
        
        @org.jetbrains.annotations.NotNull()
        public final com.gigforce.verification.oldverification.AddressUpload newInstance() {
            return null;
        }
        
        private Companion() {
            super();
        }
    }
}