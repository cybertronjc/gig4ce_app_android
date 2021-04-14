package com.gigforce.profile.onboarding.fragments.highestqulalification

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.gigforce.profile.R
import kotlinx.android.synthetic.main.highest_qualification_item.*

class HighestQualificationFragment : Fragment() {

    companion object {
        fun newInstance() = HighestQualificationFragment()
    }

    private lateinit var viewModel: HighestQualificationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.highest_qualification_item, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HighestQualificationViewModel::class.java)
        listener()
    }

    private fun listener() {
        imageTextCardMol.setOnClickListener(View.OnClickListener {
            resetAll()
            setSelected(icon, option, imageTextCardMol)
        })
        imageTextCardMol4.setOnClickListener(View.OnClickListener {
            resetAll()
            setSelected(icon1, option1, imageTextCardMol4)
        })
        imageTextCardMol3.setOnClickListener(View.OnClickListener {
            resetAll()
            setSelected(icon2, option2, imageTextCardMol3)
        })
        imageTextCardMol_.setOnClickListener(View.OnClickListener {
            resetAll()
            setSelected(icon_, option_, imageTextCardMol_)
        })
        imageTextCardMol4_.setOnClickListener(View.OnClickListener {
            resetAll()
            setSelected(icon1_, option1_, imageTextCardMol4_)
        })
        imageTextCardMol3_.setOnClickListener(View.OnClickListener {
            resetAll()
            setSelected(icon2_, option2_, imageTextCardMol3_)
        })
    }

    private fun resetAll() {
        resetSelected(icon, option, imageTextCardMol)
        resetSelected(icon1,option1, imageTextCardMol4)
        resetSelected(icon2, option2, imageTextCardMol3)

    }

    private fun resetSelected(icon: ImageView, option: TextView, view: View) {
        context?.let {
            icon.setColorFilter(ContextCompat.getColor(it, R.color.default_color))
            option.setTextColor(ContextCompat.getColor(it, R.color.default_color))
            view.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    it,
                    R.drawable.option_default_border
                )
            )
        }
    }

    private fun setSelected(icon: ImageView, option: TextView, view: View) {
        context?.let {
            icon.setColorFilter(ContextCompat.getColor(it, R.color.selected_image_color))
            option.setTextColor(ContextCompat.getColor(it, R.color.selected_text_color))
            view.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    it,
                    R.drawable.option_selection_border
                )
            )
        }

    }

}