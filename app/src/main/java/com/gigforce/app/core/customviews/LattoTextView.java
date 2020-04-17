package com.gigforce.app.core.customviews;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;


public class LattoTextView extends AppCompatTextView {
    public LattoTextView(Context context) {
        super(context);
        init();
    }

    public void init() {
        FontHelper.setTypeFace(this, getContext().getAssets(), "fonts/Lato-Regular.ttf");
    }

    public LattoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LattoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
}
