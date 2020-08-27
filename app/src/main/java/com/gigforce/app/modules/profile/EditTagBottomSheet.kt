package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gigforce.app.R
import kotlinx.android.synthetic.main.edit_tags_bs.*
import kotlinx.android.synthetic.main.edit_tags_bs.add_tag_new_tag
import kotlinx.android.synthetic.main.edit_tags_bs.tags

class EditTagBottomSheet: ProfileBaseBottomSheetFragment() {

    companion object {
        fun newInstance() = EditTagBottomSheet()
    }

    var allTags: ArrayList<String> = ArrayList()
    var tagsToRemove: ArrayList<String> = ArrayList()
    var tagsToAdd: ArrayList<String> = ArrayList()
    var userTags: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        inflateView(R.layout.edit_tags_bs, inflater, container)
        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setListeners()
    }

    private fun initialize() {
        profileViewModel.getAllTags()
        profileViewModel.Tags.observe(viewLifecycleOwner, androidx.lifecycle.Observer { Tags ->
            for (tag in Tags.tagName!!) {
                allTags.add(tag)
            }
        })

        profileViewModel.userProfileData.observe(viewLifecycleOwner, androidx.lifecycle.Observer { profile ->
            userTags = profile?.tags!!

            for (tag in profile.tags!!) {
                var chip = addCrossableChip(this.requireContext(), tag)
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
        cancel.setOnClickListener {
            this.dismiss()
        }

        save.setOnClickListener {
            profileViewModel.setProfileTag(tagsToAdd)
            profileViewModel.removeProfileTag(tagsToRemove)
            this.dismiss()
        }
        add_tag.setOnClickListener {
            var tag = add_tag_new_tag.text.toString()
            if (ValidateTag()) {
                if (!allTags.contains(tag)) {
                    profileViewModel.addNewTag(tag)
                }
                if (!userTags.contains(tag) && !tagsToAdd.contains(tag)) {
                    tagsToAdd.add(tag)
                    var chip = addCrossableChip(this.requireContext(), tag)
                    tags.addView(chip)
                    chip.setOnCloseIconClickListener {
                        Log.d("STATUS", "Deleting tag")
                        tagsToRemove.add(tag)
                        tags.removeView(it)
                    }
                }
                add_tag_new_tag.setText("")
            } else {
                Toast.makeText(requireContext(), "Invalid tag, tag can not be empty or contain #", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun ValidateTag(): Boolean {
        if (validation!!.isValidTag(add_tag_new_tag.text.toString())) {
            return true
        }
        return false
    }
}