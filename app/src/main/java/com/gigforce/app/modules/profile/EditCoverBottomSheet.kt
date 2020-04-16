package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import kotlinx.android.synthetic.main.add_tag_bottom_sheet.view.*
import kotlinx.android.synthetic.main.edit_cover_bottom_sheet.*

class EditCoverBottomSheet(): ProfileBaseBottomSheetFragment() {

    companion object {
        fun newInstance() = EditCoverBottomSheet()
    }

    var tagsToRemove: ArrayList<String> = ArrayList()
    var tagsToAdd: ArrayList<String> = ArrayList()
    var allTags: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        return inflateView(R.layout.edit_cover_bottom_sheet, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initialize()
        setListeners()
    }

    private fun initialize() {
        var layout: View? = getFragmentView()

        profileViewModel!!.getAllTags()
        val autotextview: AutoCompleteTextView = add_tag_new_tag

        profileViewModel!!.Tags.observe(this, Observer {Tags->
            for(tag in Tags.tagName!! ) {
                allTags.add(tag)
            }
            autotextview.setAdapter(ArrayAdapter(this.context!!, R.layout.support_simple_spinner_dropdown_item, allTags))
        })

        profileViewModel!!.userProfileData.observe(this, Observer { profile ->
            bio.setText(profile.bio)

            for (tag in profile.Tags!!) {
                var chip = addCrossableChip(this.context!!, tag)
                tags.addView(chip)
                chip.setOnCloseIconClickListener {
                    Log.d("STATUS", "Deleting tag")
                    tagsToRemove.add(tag)
                    tags.removeView(it)
                }
            }
        })
    }

    private fun setListeners() {
        cancel_button.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }

        save_button.setOnClickListener {
            for (tag in tagsToAdd) {
                profileViewModel!!.setProfileTag(tag)
            }

            for (tag in tagsToRemove) {
                profileViewModel!!.removeProfileTag(tag)
            }

            if (bio_text.text.toString() != "") {
                profileViewModel!!.setProfileBio(bio.text.toString())
            }

            findNavController().navigate(R.id.profileFragment)
        }

        add_button.setOnClickListener {
            var tag = add_tag_new_tag.text.toString()
            if (!allTags.contains(tag)) {
                profileViewModel!!.addNewTag(tag)
            }
            tagsToAdd.add(tag)
            var chip = addCrossableChip(this.context!!, tag)
            tags.addView(chip)
            chip.setOnCloseIconClickListener {
                Log.d("STATUS", "Deleting tag")
                tagsToRemove.add(tag)
                tags.removeView(it)
            }
            add_tag_new_tag.setText("")
        }
    }
}