package com.gigforce.app.core.components;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class SeekBarDependentCanvas extends View {
    private Canvas canvas;
    public SeekBarDependentCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public Canvas getCanvas(){
        return canvas;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
    }
}
