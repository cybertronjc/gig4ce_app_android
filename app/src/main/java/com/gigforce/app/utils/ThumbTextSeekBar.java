package com.gigforce.app.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.gigforce.app.R;

public class ThumbTextSeekBar extends LinearLayout {

    public ThumbTextView tvThumb;
    public SeekBar seekBar;
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener;

    public ThumbTextSeekBar(Context context) {
        super(context);
        init();
    }

    public ThumbTextSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_thumb_text_seekbar, this);
        setOrientation(LinearLayout.VERTICAL);
        tvThumb = findViewById(R.id.tvThumb);
        seekBar = findViewById(R.id.sbProgress);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (onSeekBarChangeListener != null)
                    onSeekBarChangeListener.onStopTrackingTouch(seekBar);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (onSeekBarChangeListener != null)
                    onSeekBarChangeListener.onStartTrackingTouch(seekBar);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (onSeekBarChangeListener != null)
                    onSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
                tvThumb.attachToSeekBar(seekBar);
            }
        });

    }

    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener l) {
        this.onSeekBarChangeListener = l;
    }

    public void setThumbText(String text) {
        tvThumb.setText(text);
    }

    public void setProgress(int progress) {
        if (progress == seekBar.getProgress() && progress == 0) {
            seekBar.setProgress(1);
            seekBar.setProgress(0);
        } else {
            seekBar.setProgress(progress);
        }
    }
}