package com.gigforce.app.modules.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.add_tag_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_profile_main_expanded.view.*

class AddTagBottomSheet: BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = AddTagBottomSheet()
    }

    var tags: ArrayList<String> = ArrayList()
    lateinit var layout: View
    lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.add_tag_bottom_sheet, container, false)
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getAllTags()
        val autotextview: AutoCompleteTextView = layout.add_tag_new_tag

        viewModel.Tags.observe(this, Observer {Tags->
            for(tag in Tags.tagName!! ) {
                tags.add(tag)
            }
            autotextview.setAdapter(ArrayAdapter(this.context!!, R.layout.support_simple_spinner_dropdown_item, tags))
        })

        viewModel.userProfileData.observe (this, Observer {profile ->
            layout.all_tags.removeAllViews()
            for (tag in profile.Tags!!) {
                val chip = Chip(this.context)
                chip.text = " $tag "
                chip.isClickable = false
                layout.all_tags.addView(chip)
            }
        })

        layout.add_tag_button.setOnClickListener {
            setProfileTag(layout.add_tag_new_tag.text.toString())
            layout.add_tag_new_tag.setText("")
        }

        layout.add_tag_back_button.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
    }

    fun setProfileTag(tag: String) {
        viewModel.setProfileTag(tag)
    }
}