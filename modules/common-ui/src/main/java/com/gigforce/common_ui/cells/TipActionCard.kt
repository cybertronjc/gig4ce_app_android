package com.gigforce.common_ui.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.TipActionCardDVM
import com.gigforce.core.IViewHolder

class TipActionCard(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs), IViewHolder {

    private var tv_title:TextView
    private var tv_subtitle:TextView
    private var tv_cta:TextView

    init {
        this.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_card_tip_action, this, true)

        tv_title = this.findViewById(R.id.tv_title)
        tv_subtitle = this.findViewById(R.id.tv_desc)
        tv_cta = this.findViewById(R.id.cta)
    }

    override fun bind(data: Any?) {
        if(data is TipActionCardDVM){
            tv_title.setText(data.title)
            tv_subtitle.setText(data.subtitle)
            tv_cta.setText(data.action)
        }
    }

}