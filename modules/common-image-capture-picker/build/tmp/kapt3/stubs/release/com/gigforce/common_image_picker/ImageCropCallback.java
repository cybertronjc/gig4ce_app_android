package com.gigforce.common_image_picker;

import java.lang.System;

@kotlin.Metadata(mv = {1, 4, 1}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\u0014\u0010\u0002\u001a\u00020\u00032\n\u0010\u0004\u001a\u00060\u0005j\u0002`\u0006H&J\u0010\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH&\u00a8\u0006\n"}, d2 = {"Lcom/gigforce/common_image_picker/ImageCropCallback;", "", "errorWhileCapturingOrPickingImage", "", "e", "Ljava/lang/Exception;", "Lkotlin/Exception;", "imageResult", "uri", "Landroid/net/Uri;", "common-image-capture-picker_release"})
public abstract interface ImageCropCallback {
    
    public abstract void errorWhileCapturingOrPickingImage(@org.jetbrains.annotations.NotNull()
    java.lang.Exception e);
    
    public abstract void imageResult(@org.jetbrains.annotations.NotNull()
    android.net.Uri uri);
}