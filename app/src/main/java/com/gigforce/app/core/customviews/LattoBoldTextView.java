package com.gigforce.app.core.customviews;

import android.content.Context;
import android.util.AttributeSet;


public class LattoBoldTextView extends LattoTextView {
    public LattoBoldTextView(Context context) {
        super(context);
    }

    public LattoBoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LattoBoldTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init() {
        FontHelper.setTypeFace(this, getContext().getAssets(), "fonts/Lato-Bold.ttf");
    }
}
