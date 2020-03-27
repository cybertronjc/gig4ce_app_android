package com.gigforce.app.modules.profile

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import com.gigforce.app.R
import kotlinx.android.synthetic.main.profile_nav_bar.view.*

class ProfileNavBar: CardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.profile_nav_bar, this)

        about_me.setOnClickListener {
            findNavController().navigate(R.id.aboutExpandedFragment)
        }
        education.setOnClickListener {
            findNavController().navigate(R.id.educationExpandedFragment)
        }
        experience.setOnClickListener {
            findNavController().navigate(R.id.experienceExpandedFragment)
        }
    }
}