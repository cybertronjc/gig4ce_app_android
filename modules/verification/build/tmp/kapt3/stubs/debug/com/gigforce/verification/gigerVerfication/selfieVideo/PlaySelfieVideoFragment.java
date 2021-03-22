package com.gigforce.verification.gigerVerfication.selfieVideo;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u0000 #2\u00020\u0001:\u0001#B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u0007\u001a\u00020\bH\u0002J\b\u0010\u000b\u001a\u00020\fH\u0002J,\u0010\r\u001a\n \u000f*\u0004\u0018\u00010\u000e0\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u00132\b\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u0016J\b\u0010\u0016\u001a\u00020\fH\u0016J\b\u0010\u0017\u001a\u00020\fH\u0016J\u0010\u0010\u0018\u001a\u00020\f2\u0006\u0010\u0019\u001a\u00020\u0015H\u0016J\b\u0010\u001a\u001a\u00020\fH\u0016J\u001a\u0010\u001b\u001a\u00020\f2\u0006\u0010\u001c\u001a\u00020\u000e2\b\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u0016J\u000e\u0010\u001d\u001a\u00020\f2\u0006\u0010\u0007\u001a\u00020\bJ\u000e\u0010\u001d\u001a\u00020\f2\u0006\u0010\u001e\u001a\u00020\u001fJ\b\u0010 \u001a\u00020\fH\u0002J\u0006\u0010!\u001a\u00020\fJ\u0006\u0010\"\u001a\u00020\fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006$"}, d2 = {"Lcom/gigforce/verification/gigerVerfication/selfieVideo/PlaySelfieVideoFragment;", "Landroidx/fragment/app/Fragment;", "()V", "mPlaySelfieVideoFragmentEventListener", "Lcom/gigforce/verification/gigerVerfication/selfieVideo/PlaySelfieVideoFragmentEventListener;", "player", "Lcom/google/android/exoplayer2/SimpleExoPlayer;", "uri", "Landroid/net/Uri;", "buildMediaSource", "Lcom/google/android/exoplayer2/source/MediaSource;", "initVideoPlayer", "", "onCreateView", "Landroid/view/View;", "kotlin.jvm.PlatformType", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onPause", "onResume", "onSaveInstanceState", "outState", "onStop", "onViewCreated", "view", "playVideo", "file", "Ljava/io/File;", "releasePlayer", "showPlayVideoLayout", "showVideoUploadingProgress", "Companion", "verification_debug"})
public final class PlaySelfieVideoFragment extends androidx.fragment.app.Fragment {
    private com.gigforce.verification.gigerVerfication.selfieVideo.PlaySelfieVideoFragmentEventListener mPlaySelfieVideoFragmentEventListener;
    private com.google.android.exoplayer2.SimpleExoPlayer player;
    private android.net.Uri uri;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TAG = "PlaySelfieVideoFragment";
    private static final java.lang.String INTENT_EXTRA_URI = "uri";
    @org.jetbrains.annotations.NotNull()
    public static final com.gigforce.verification.gigerVerfication.selfieVideo.PlaySelfieVideoFragment.Companion Companion = null;
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
    
    @java.lang.Override()
    public void onSaveInstanceState(@org.jetbrains.annotations.NotNull()
    android.os.Bundle outState) {
    }
    
    @java.lang.Override()
    public void onResume() {
    }
    
    @java.lang.Override()
    public void onPause() {
    }
    
    @java.lang.Override()
    public void onStop() {
    }
    
    private final void initVideoPlayer() {
    }
    
    public final void playVideo(@org.jetbrains.annotations.NotNull()
    java.io.File file) {
    }
    
    public final void playVideo(@org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
    }
    
    private final com.google.android.exoplayer2.source.MediaSource buildMediaSource(android.net.Uri uri) {
        return null;
    }
    
    private final void releasePlayer() {
    }
    
    public final void showVideoUploadingProgress() {
    }
    
    public final void showPlayVideoLayout() {
    }
    
    public PlaySelfieVideoFragment() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/gigforce/verification/gigerVerfication/selfieVideo/PlaySelfieVideoFragment$Companion;", "", "()V", "INTENT_EXTRA_URI", "", "TAG", "getInstance", "Lcom/gigforce/verification/gigerVerfication/selfieVideo/PlaySelfieVideoFragment;", "playSelfieVideoFragmentEventListener", "Lcom/gigforce/verification/gigerVerfication/selfieVideo/PlaySelfieVideoFragmentEventListener;", "uri", "Landroid/net/Uri;", "verification_debug"})
    public static final class Companion {
        
        @org.jetbrains.annotations.NotNull()
        public final com.gigforce.verification.gigerVerfication.selfieVideo.PlaySelfieVideoFragment getInstance(@org.jetbrains.annotations.NotNull()
        com.gigforce.verification.gigerVerfication.selfieVideo.PlaySelfieVideoFragmentEventListener playSelfieVideoFragmentEventListener, @org.jetbrains.annotations.NotNull()
        android.net.Uri uri) {
            return null;
        }
        
        private Companion() {
            super();
        }
    }
}