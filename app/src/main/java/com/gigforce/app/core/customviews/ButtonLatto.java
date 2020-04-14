package com.gigforce.app.core.customviews;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;


public class ButtonLatto extends AppCompatButton {
    public ButtonLatto(Context context) {
        super(context);
        init();
    }

    public ButtonLatto(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonLatto(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        FontHelper.setTypeFace(this, getContext().getAssets(), "fonts/Lato-Regular.ttf");
    }
}
