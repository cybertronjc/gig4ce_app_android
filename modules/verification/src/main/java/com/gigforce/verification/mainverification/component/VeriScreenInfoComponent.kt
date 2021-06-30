package com.gigforce.verification.mainverification.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gigforce.verification.R
import kotlinx.android.synthetic.main.veri_screen_info_component.view.*

class VeriScreenInfoComponent(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.veri_screen_info_component, this, true)
        attrs?.let {
            val styledAttributeSet =
                context.obtainStyledAttributes(
                    it,
                    R.styleable.VeriScreenInfoComponent,
                    0,
                    0
                )
            val uppercaptionstr =
                styledAttributeSet.getString(R.styleable.VeriScreenInfoComponent_uppercaption) ?: ""
            val titlestr =
                styledAttributeSet.getString(R.styleable.VeriScreenInfoComponent_title) ?: ""
            val docinfostr =
                styledAttributeSet.getString(R.styleable.VeriScreenInfoComponent_docinfotext) ?: ""
            val querytextstr =
                styledAttributeSet.getString(R.styleable.VeriScreenInfoComponent_querytext) ?: ""
            val missingdoctext =
                styledAttributeSet.getString(R.styleable.VeriScreenInfoComponent_missingdoctext) ?: ""
            setUpperCaption(uppercaptionstr)
            setTitle(titlestr)
            setDocInfo(docinfostr)
            setQueryStr(querytextstr)
            setMissingDocText(missingdoctext)
        }

    }

    private fun setMissingDocText(missingdoctext: String){
        missingtext.text = missingdoctext
    }

    private fun setQueryStr(querytextstr: String) {
        querytext.text = querytextstr
    }


    private fun setDocInfo(docinfostr: String){
        docdetail.text = docinfostr
    }


    private fun setTitle(titlestr: String) {
        title.text = titlestr
    }

    private fun setUpperCaption(uppercaptionstr: String) {
        uppercaption.text = uppercaptionstr
    }
}