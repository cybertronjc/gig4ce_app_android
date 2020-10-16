package com.gigforce.app.utils

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.gigforce.app.R

class ThumbTextSeekBar : LinearLayout {
    lateinit var tvThumb: ThumbTextView
    lateinit var seekBar: SeekBar
    private var onSeekBarChangeListener: OnSeekBarChangeListener? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.layout_thumb_text_seekbar, this)
        orientation = VERTICAL
        tvThumb = findViewById<ThumbTextView>(R.id.tvThumb)
        seekBar = findViewById<SeekBar>(R.id.sbProgress)

        val lp = tvThumb.layoutParams as LayoutParams
        lp.setMargins(seekBar.paddingLeft-(getViewWidth(tvThumb) / 2), resources.getDimensionPixelSize(R.dimen.size_9), 0, 0)
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (onSeekBarChangeListener != null) onSeekBarChangeListener!!.onStopTrackingTouch(
                    seekBar
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                if (onSeekBarChangeListener != null) onSeekBarChangeListener!!.onStartTrackingTouch(
                    seekBar
                )
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (onSeekBarChangeListener != null) onSeekBarChangeListener!!.onProgressChanged(
                    seekBar,
                    progress,
                    fromUser
                )
                tvThumb.attachToSeekBar(seekBar)
            }
        })
    }

    fun setOnSeekBarChangeListener(l: OnSeekBarChangeListener?) {
        onSeekBarChangeListener = l
    }

    fun setThumbText(text: String?) {
        tvThumb!!.text = text
    }

    fun setProgress(progress: Int) {
        if (progress == seekBar!!.progress && progress == 0) {
            seekBar!!.progress = 1
            seekBar!!.progress = 0
        } else {
            seekBar!!.progress = progress
        }
    }
}