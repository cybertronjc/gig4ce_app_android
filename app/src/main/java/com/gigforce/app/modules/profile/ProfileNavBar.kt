package com.gigforce.app.modules.profile

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.profile_nav_bar.view.*

class ProfileNavBar: LinearLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.profile_nav_bar, this)

        about_me.setOnClickListener {
            findNavController().popBackStack(R.id.aboutExpandedFragment, true)
            findNavController().navigate(R.id.aboutExpandedFragment)
        }
        education.setOnClickListener {
            findNavController().popBackStack(R.id.educationExpandedFragment, true)
            findNavController().navigate(R.id.educationExpandedFragment)
        }
        experience.setOnClickListener {
            findNavController().popBackStack(R.id.experienceExpandedFragment, true)
            findNavController().navigate(R.id.experienceExpandedFragment)
        }
    }

    var about_me_active: Boolean = false
        set(value) {
            field = value
            if (value) {
                about_me.setChipBackgroundColorResource(R.color.active_nav_bg)
                about_me.setChipStrokeWidthResource(R.dimen.border_width)
                about_me.setChipStrokeColorResource(R.color.colorAccent)
                about_me.setTextColor(resources.getColor(R.color.colorAccent))
            }
        }

    var education_active: Boolean = false
        set(value) {
            field = value
            if (value) {
                education.setChipBackgroundColorResource(R.color.active_nav_bg)
                education.setChipStrokeWidthResource(R.dimen.border_width)
                education.setChipStrokeColorResource(R.color.colorAccent)
                education.setTextColor(resources.getColor(R.color.colorAccent))
            }
        }

    var experience_active: Boolean = false
        set(value) {
            field = value
            if (value) {
                experience.setChipBackgroundColorResource(R.color.active_nav_bg)
                experience.setChipStrokeWidthResource(R.dimen.border_width)
                experience.setChipStrokeColorResource(R.color.colorAccent)
                experience.setTextColor(resources.getColor(R.color.colorAccent))
            }
        }
}