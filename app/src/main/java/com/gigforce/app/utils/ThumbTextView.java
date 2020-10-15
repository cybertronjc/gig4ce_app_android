package com.gigforce.app.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.gigforce.app.R;

public class ThumbTextView extends androidx.appcompat.widget.AppCompatTextView {

    private LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private int width = 0;

    public ThumbTextView(Context context) {
        super(context);
    }

    public ThumbTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void attachToSeekBar(SeekBar seekBar) {
        float contentWidth = this.getPaint().measureText(getText().toString());

        int width = seekBar.getWidth()
                - seekBar.getPaddingLeft()
                - seekBar.getPaddingRight();
        int thumbPos = seekBar.getPaddingLeft()
                + width
                * seekBar.getProgress()
                / seekBar.getMax();

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
        int left = getResources().getDimensionPixelSize(R.dimen.size_4);

        layoutParams.setMargins(thumbPos -left, 0, 0, 0);
        setLayoutParams(layoutParams);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (width == 0)
            width = MeasureSpec.getSize(widthMeasureSpec);
    }
}