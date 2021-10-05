package com.gigforce.lead_management.ui.joining_list.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.gigforce.core.IViewHolder
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.RecyclerRowJoiningStatusItemBinding
import com.gigforce.lead_management.models.JoiningList2RecyclerItemData
import com.gigforce.lead_management.models.JoiningListRecyclerItemData


class JoiningStatusRecyclerItemView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder {

    private lateinit var viewBinding: RecyclerRowJoiningStatusItemBinding

    init {
        setDefault()
        inflate()
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = RecyclerRowJoiningStatusItemBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun bind(data: Any?) {
        data?.let {
            val shiftNameData = it as JoiningList2RecyclerItemData.JoiningListRecyclerStatusItemData
            viewBinding.statusTv.text = shiftNameData.status
                if (shiftNameData.dropEnabled){
                    viewBinding.dropdownView.setImageDrawable(resources.getDrawable(R.drawable.ic_dropdown_up))
                }else{
                    viewBinding.dropdownView.setImageDrawable(resources.getDrawable(R.drawable.ic_dropdown_drop))
                }
        }
    }
}
