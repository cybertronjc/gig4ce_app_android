package com.gigforce.app.modules.home

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import com.gigforce.app.R
import kotlinx.android.synthetic.main.cmp_top_profilebar.view.*

class TopProfileBar(context: Context, attrs: AttributeSet? = null)
    : CardView(context, attrs)
{
    init {
        LayoutInflater.from(context).inflate(R.layout.cmp_top_profilebar, this, true)

        attrs ?.let {
            val styledAttributes = context.obtainStyledAttributes(it, R.styleable.TopProfileBar, 0, 0)

            val title = styledAttributes.getString(R.styleable.TopProfileBar_title)
            val subtitle = styledAttributes.getString(R.styleable.TopProfileBar_subtitle)

            txt_title.setText(title)
            txt_subtitle.setText(subtitle)

            styledAttributes.recycle()
            txt_title.setOnClickListener {//
//                TopProfileBar@this.findNavController()
//                    .navigate(R.id.profileFragment)

                TopProfileBar@this.findNavController().navigate(R.id.languageSelectFragment)
            }
        }
    }
}