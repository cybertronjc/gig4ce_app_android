package com.gigforce.common_ui.components.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.SimpleCardDVM1
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.simple_card_component1.view.*
import javax.inject.Inject

@AndroidEntryPoint
class SimpleCardComponent1(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder {
    val view: View
    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view = LayoutInflater.from(context).inflate(R.layout.simple_card_component1, this, true)
        attrs?.let {
            val styledAttributeSet =
                context.obtainStyledAttributes(it, R.styleable.SimpleCardComponent1, 0, 0)
            val title = styledAttributeSet.getString(R.styleable.SimpleCardComponent1_title_scc) ?: ""
            setTitle(title)
        }
    }

    @Inject
    lateinit var navigation : INavigation

    fun setTitle(title: String) {
        vaccineTitle.text = title
    }

    override fun bind(data: Any?) {
        if (data is SimpleCardDVM1) {
            data.label?.let {
                setTitle(it)
            }
            data.navPath?.let {navPath->
                card_view.setOnClickListener{
                    navigation.navigateTo(navPath)
                }
            }
        }
    }
}