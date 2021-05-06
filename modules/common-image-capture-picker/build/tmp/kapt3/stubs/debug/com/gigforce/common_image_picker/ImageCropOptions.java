package com.gigforce.common_image_picker;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\t\u0018\u00002\u00020\u0001:\u0001\u0014B\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u001c\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u001a\u0010\u000b\u001a\u00020\fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010R\u001a\u0010\u0011\u001a\u00020\fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u000e\"\u0004\b\u0013\u0010\u0010\u00a8\u0006\u0015"}, d2 = {"Lcom/gigforce/common_image_picker/ImageCropOptions;", "", "builder", "Lcom/gigforce/common_image_picker/ImageCropOptions$Builder;", "(Lcom/gigforce/common_image_picker/ImageCropOptions$Builder;)V", "outputFileUri", "Landroid/net/Uri;", "getOutputFileUri", "()Landroid/net/Uri;", "setOutputFileUri", "(Landroid/net/Uri;)V", "shouldDetectForFace", "", "getShouldDetectForFace", "()Z", "setShouldDetectForFace", "(Z)V", "shouldOpenImageCropper", "getShouldOpenImageCropper", "setShouldOpenImageCropper", "Builder", "common-image-capture-picker_debug"})
public final class ImageCropOptions {
    private boolean shouldOpenImageCropper;
    private boolean shouldDetectForFace;
    @org.jetbrains.annotations.Nullable()
    private android.net.Uri outputFileUri;
    
    public final boolean getShouldOpenImageCropper() {
        return false;
    }
    
    public final void setShouldOpenImageCropper(boolean p0) {
    }
    
    public final boolean getShouldDetectForFace() {
        return false;
    }
    
    public final void setShouldDetectForFace(boolean p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.net.Uri getOutputFileUri() {
        return null;
    }
    
    public final void setOutputFileUri(@org.jetbrains.annotations.Nullable()
    android.net.Uri p0) {
    }
    
    private ImageCropOptions(com.gigforce.common_image_picker.ImageCropOptions.Builder builder) {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0012\u001a\u00020\u0013J\u000e\u0010\u0007\u001a\u00020\u00002\u0006\u0010\u0003\u001a\u00020\u0004J\u000e\u0010\u0014\u001a\u00020\u00002\u0006\u0010\u0015\u001a\u00020\nJ\u000e\u0010\u0016\u001a\u00020\u00002\u0006\u0010\u0017\u001a\u00020\nR\u001c\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001a\u0010\u000f\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\f\"\u0004\b\u0011\u0010\u000e\u00a8\u0006\u0018"}, d2 = {"Lcom/gigforce/common_image_picker/ImageCropOptions$Builder;", "", "()V", "outputFileUri", "Landroid/net/Uri;", "getOutputFileUri", "()Landroid/net/Uri;", "setOutputFileUri", "(Landroid/net/Uri;)V", "shouldDetectForFace", "", "getShouldDetectForFace", "()Z", "setShouldDetectForFace", "(Z)V", "shouldOpenImageCropper", "getShouldOpenImageCropper", "setShouldOpenImageCropper", "build", "Lcom/gigforce/common_image_picker/ImageCropOptions;", "setShouldEnableFaceDetector", "faceDetector", "shouldOpenImageCrop", "openImageCropper", "common-image-capture-picker_debug"})
    public static final class Builder {
        private boolean shouldOpenImageCropper = false;
        private boolean shouldDetectForFace = false;
        @org.jetbrains.annotations.Nullable()
        private android.net.Uri outputFileUri;
        
        public final boolean getShouldOpenImageCropper() {
            return false;
        }
        
        public final void setShouldOpenImageCropper(boolean p0) {
        }
        
        public final boolean getShouldDetectForFace() {
            return false;
        }
        
        public final void setShouldDetectForFace(boolean p0) {
        }
        
        @org.jetbrains.annotations.Nullable()
        public final android.net.Uri getOutputFileUri() {
            return null;
        }
        
        public final void setOutputFileUri(@org.jetbrains.annotations.Nullable()
        android.net.Uri p0) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.gigforce.common_image_picker.ImageCropOptions build() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.gigforce.common_image_picker.ImageCropOptions.Builder shouldOpenImageCrop(boolean openImageCropper) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.gigforce.common_image_picker.ImageCropOptions.Builder setShouldEnableFaceDetector(boolean faceDetector) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.gigforce.common_image_picker.ImageCropOptions.Builder setOutputFileUri(@org.jetbrains.annotations.NotNull()
        android.net.Uri outputFileUri) {
            return null;
        }
        
        public Builder() {
            super();
        }
    }
}