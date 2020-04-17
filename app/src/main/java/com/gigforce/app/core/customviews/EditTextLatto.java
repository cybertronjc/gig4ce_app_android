package com.gigforce.app.core.customviews;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;


public class EditTextLatto extends AppCompatEditText {
    public EditTextLatto(Context context) {
        super(context);
        init();
    }

    public EditTextLatto(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditTextLatto(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        FontHelper.setTypeFace(this, getContext().getAssets(), "fonts/Lato-Regular.ttf");
    }
}
