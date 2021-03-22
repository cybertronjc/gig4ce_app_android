package com.gigforce.verification.gigerVerfication.selfieVideo;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0015\n\u0002\b\u000b\u0018\u0000 .2\u00020\u0001:\u0002-.B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\f\u001a\u00020\u0004H\u0002J\b\u0010\r\u001a\u00020\u000eH\u0002J\"\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00112\b\u0010\u0013\u001a\u0004\u0018\u00010\u0014H\u0016J,\u0010\u0015\u001a\n \u0017*\u0004\u0018\u00010\u00160\u00162\u0006\u0010\u0018\u001a\u00020\u00192\b\u0010\u001a\u001a\u0004\u0018\u00010\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u001dH\u0016J\b\u0010\u001e\u001a\u00020\u000eH\u0016J+\u0010\u001f\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\"0!2\u0006\u0010#\u001a\u00020$H\u0016\u00a2\u0006\u0002\u0010%J\b\u0010&\u001a\u00020\u000eH\u0016J\u001a\u0010\'\u001a\u00020\u000e2\u0006\u0010(\u001a\u00020\u00162\b\u0010\u001c\u001a\u0004\u0018\u00010\u001dH\u0016J\b\u0010)\u001a\u00020\u000eH\u0002J\b\u0010*\u001a\u00020\u000eH\u0002J\b\u0010+\u001a\u00020\u000eH\u0002J\b\u0010,\u001a\u00020\u000eH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082.\u00a2\u0006\u0002\n\u0000R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006/"}, d2 = {"Lcom/gigforce/verification/gigerVerfication/selfieVideo/CaptureSelfieVideoFragment;", "Landroidx/fragment/app/Fragment;", "()V", "cameraInitiated", "", "capturingVideo", "mCaptureVideoFragmentEventListener", "Lcom/gigforce/verification/gigerVerfication/selfieVideo/CaptureVideoFragmentEventListener;", "timer", "Landroid/os/CountDownTimer;", "getTimer", "()Landroid/os/CountDownTimer;", "hasCameraPermissions", "initCamera", "", "onActivityResult", "requestCode", "", "resultCode", "data", "Landroid/content/Intent;", "onCreateView", "Landroid/view/View;", "kotlin.jvm.PlatformType", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onRequestPermissionsResult", "permissions", "", "", "grantResults", "", "(I[Ljava/lang/String;[I)V", "onResume", "onViewCreated", "view", "requestCameraPermission", "showPermissionLayout", "startRecordingVideo", "startTimer", "CameraListener", "Companion", "verification_debug"})
public final class CaptureSelfieVideoFragment extends androidx.fragment.app.Fragment {
    private com.gigforce.verification.gigerVerfication.selfieVideo.CaptureVideoFragmentEventListener mCaptureVideoFragmentEventListener;
    private boolean capturingVideo = false;
    private boolean cameraInitiated = false;
    @org.jetbrains.annotations.NotNull()
    private final android.os.CountDownTimer timer = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TAG = "CaptureSelfieVideoFragment";
    private static final int REQUEST_CAMERA_PERMISSION = 2321;
    private static final int REQUEST_CAMERA_MANUAL = 2322;
    private static final int SELFIE_VIDEO_TIME = 10000;
    @org.jetbrains.annotations.NotNull()
    public static final com.gigforce.verification.gigerVerfication.selfieVideo.CaptureSelfieVideoFragment.Companion Companion = null;
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
    
    @org.jetbrains.annotations.NotNull()
    public final android.os.CountDownTimer getTimer() {
        return null;
    }
    
    private final void startTimer() {
    }
    
    private final boolean hasCameraPermissions() {
        return false;
    }
    
    private final void requestCameraPermission() {
    }
    
    @java.lang.Override()
    public void onResume() {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @java.lang.Override()
    public void onRequestPermissionsResult(int requestCode, @org.jetbrains.annotations.NotNull()
    java.lang.String[] permissions, @org.jetbrains.annotations.NotNull()
    int[] grantResults) {
    }
    
    private final void initCamera() {
    }
    
    private final void showPermissionLayout() {
    }
    
    private final void startRecordingVideo() {
    }
    
    @java.lang.Override()
    public void onActivityResult(int requestCode, int resultCode, @org.jetbrains.annotations.Nullable()
    android.content.Intent data) {
    }
    
    public CaptureSelfieVideoFragment() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0082\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0016\u00a8\u0006\u0007"}, d2 = {"Lcom/gigforce/verification/gigerVerfication/selfieVideo/CaptureSelfieVideoFragment$CameraListener;", "Lcom/otaliastudios/cameraview/CameraListener;", "(Lcom/gigforce/verification/gigerVerfication/selfieVideo/CaptureSelfieVideoFragment;)V", "onVideoTaken", "", "result", "Lcom/otaliastudios/cameraview/VideoResult;", "verification_debug"})
    final class CameraListener extends com.otaliastudios.cameraview.CameraListener {
        
        @java.lang.Override()
        public void onVideoTaken(@org.jetbrains.annotations.NotNull()
        com.otaliastudios.cameraview.VideoResult result) {
        }
        
        public CameraListener() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/gigforce/verification/gigerVerfication/selfieVideo/CaptureSelfieVideoFragment$Companion;", "", "()V", "REQUEST_CAMERA_MANUAL", "", "REQUEST_CAMERA_PERMISSION", "SELFIE_VIDEO_TIME", "TAG", "", "getInstance", "Lcom/gigforce/verification/gigerVerfication/selfieVideo/CaptureSelfieVideoFragment;", "captureVideoFragmentEventListener", "Lcom/gigforce/verification/gigerVerfication/selfieVideo/CaptureVideoFragmentEventListener;", "verification_debug"})
    public static final class Companion {
        
        @org.jetbrains.annotations.NotNull()
        public final com.gigforce.verification.gigerVerfication.selfieVideo.CaptureSelfieVideoFragment getInstance(@org.jetbrains.annotations.NotNull()
        com.gigforce.verification.gigerVerfication.selfieVideo.CaptureVideoFragmentEventListener captureVideoFragmentEventListener) {
            return null;
        }
        
        private Companion() {
            super();
        }
    }
}