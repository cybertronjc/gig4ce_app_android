package com.gigforce.app.tl_work_space.custom_tab

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.gigforce.app.android_common_utils.extensions.capitalizeFirstLetter
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.CustomTabItemBinding
import com.gigforce.app.tl_work_space.databinding.CustomTabItemType2Binding
import com.gigforce.app.tl_work_space.home.models.ValueChangeType
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible

open class TabType2CardView(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
), IViewHolder, View.OnClickListener {

    private var viewDataType2: CustomTabDataType2? = null
    private lateinit var viewBinding: CustomTabItemType2Binding

    init {
        //setDefault()
        inflate(context)
        viewBinding.root.setOnClickListener(this)
    }

    private fun inflate(context: Context) {
        viewBinding = CustomTabItemType2Binding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    private fun setDefault() {
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        this.layoutParams = params
    }

    override fun bind(data: Any?) {
        (data as CustomTabDataType2).apply {
            viewDataType2 = this

            viewBinding.titleTextview.text = title.capitalizeFirstLetter()
            viewBinding.textView.text = value.toString()

            if (this.selected) {
                viewBinding.rootConstraintLayout.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.cardview_selected_pink_border
                )
            } else {
                viewBinding.rootConstraintLayout.setBackgroundResource(R.drawable.cardview_not_selected)
            }
        }
    }

    override fun onClick(v: View?) {
        viewDataType2?.viewModel?.handleCustomTabClick(viewDataType2!!)
    }

}