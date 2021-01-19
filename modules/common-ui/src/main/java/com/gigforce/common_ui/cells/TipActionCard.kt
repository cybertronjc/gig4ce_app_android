package com.gigforce.common_ui.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.TipActionCardDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible

class TipActionCard(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder {

    private var tv_title: TextView
    private var tv_subtitle: TextView
    private var tv_cta: TextView
    private var tv_cta1: TextView
    private lateinit var img : ImageView

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_card_tip_action, this, true)
        img = this.findViewById(R.id.iv_icon)
        tv_title = this.findViewById(R.id.tv_title)
        tv_subtitle = this.findViewById(R.id.tv_desc)
        tv_cta = this.findViewById(R.id.cta)
        tv_cta1 = this.findViewById(R.id.cta1)
    }

    override fun bind(data: Any?) {
        if (data is TipActionCardDVM) {
            if(data.image is String && data.image.contains("http")){
//                GlideApp.with(context)
//                    .load(data.image)
//                    .error(R.drawable.ic_learning_default_back)
//                    .into(img)
            }
            else if(data.image is Int){
                img.setImageResource(data.image)
            }
            else{
                //layout issue if iv is gone
//                img.gone()
            }
            tv_title.text = data.title
            tv_subtitle.text = data.subtitle

            if (data.action.isNotBlank()) {
                tv_cta.text = data.action
            } else tv_cta.gone()

            if (data.secondAction.isNotBlank()) {
                tv_cta1.visible()
                tv_cta1.text = data.secondAction
            } else tv_cta1.gone()
        }
    }

}