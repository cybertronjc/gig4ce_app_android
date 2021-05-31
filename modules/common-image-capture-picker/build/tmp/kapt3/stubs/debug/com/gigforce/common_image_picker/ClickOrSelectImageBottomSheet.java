package com.gigforce.common_image_picker;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u0000 \u00122\u00020\u0001:\u0002\u0012\u0013B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0002J,\u0010\t\u001a\n \n*\u0004\u0018\u00010\b0\b2\u0006\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u0016J\u001a\u0010\u0011\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u0016R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/gigforce/common_image_picker/ClickOrSelectImageBottomSheet;", "Lcom/google/android/material/bottomsheet/BottomSheetDialogFragment;", "()V", "listener", "Lcom/gigforce/common_image_picker/ClickOrSelectImageBottomSheet$OnPickOrCaptureImageClickListener;", "initView", "", "view", "Landroid/view/View;", "onCreateView", "kotlin.jvm.PlatformType", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onViewCreated", "Companion", "OnPickOrCaptureImageClickListener", "common-image-capture-picker_debug"})
public final class ClickOrSelectImageBottomSheet extends com.google.android.material.bottomsheet.BottomSheetDialogFragment {
    private com.gigforce.common_image_picker.ClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener listener;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TAG = "ClickOrSelectImageBottomSheet";
    @org.jetbrains.annotations.NotNull()
    public static final com.gigforce.common_image_picker.ClickOrSelectImageBottomSheet.Companion Companion = null;
    private java.util.HashMap _$_findViewCache;
    
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
    
    private final void initView(android.view.View view) {
    }
    
    public ClickOrSelectImageBottomSheet() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\b\u0010\u0004\u001a\u00020\u0003H&J\b\u0010\u0005\u001a\u00020\u0003H&\u00a8\u0006\u0006"}, d2 = {"Lcom/gigforce/common_image_picker/ClickOrSelectImageBottomSheet$OnPickOrCaptureImageClickListener;", "", "onClickPictureThroughCameraClicked", "", "onPickImageThroughCameraClicked", "removeProfilePic", "common-image-capture-picker_debug"})
    public static abstract interface OnPickOrCaptureImageClickListener {
        
        public abstract void onClickPictureThroughCameraClicked();
        
        public abstract void onPickImageThroughCameraClicked();
        
        public abstract void removeProfilePic();
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/gigforce/common_image_picker/ClickOrSelectImageBottomSheet$Companion;", "", "()V", "TAG", "", "launch", "", "childFragmentManager", "Landroidx/fragment/app/FragmentManager;", "isPicturePresent", "", "listener", "Lcom/gigforce/common_image_picker/ClickOrSelectImageBottomSheet$OnPickOrCaptureImageClickListener;", "common-image-capture-picker_debug"})
    public static final class Companion {
        
        public final void launch(@org.jetbrains.annotations.NotNull()
        androidx.fragment.app.FragmentManager childFragmentManager, boolean isPicturePresent, @org.jetbrains.annotations.NotNull()
        com.gigforce.common_image_picker.ClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener listener) {
        }
        
        private Companion() {
            super();
        }
    }
}