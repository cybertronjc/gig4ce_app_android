package com.gigforce.app.modules.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.modules.preferences.PreferencesScreenItem
import com.gigforce.app.utils.DropdownAdapter
import kotlinx.android.synthetic.main.edit_cover_bottom_sheet.*
import kotlinx.android.synthetic.main.preferences_fragment.*

class EditCoverBottomSheet(): ProfileBaseBottomSheetFragment() {

    companion object {
        fun newInstance() = EditCoverBottomSheet()
    }

    var tagsToRemove: ArrayList<String> = ArrayList()
    var tagsToAdd: ArrayList<String> = ArrayList()
    var allTags: ArrayList<String> = ArrayList()
    var allTagsForPopup: ArrayList<String> = ArrayList()

    var userTags: ArrayList<String> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.edit_cover_bottom_sheet, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initialize()
        setListeners()
    }

    private fun initialize() {

        profileViewModel.getAllTags()
//        val autotextview: AutoCompleteTextView = add_tag_new_tag

        profileViewModel.Tags.observe(this, Observer {Tags->
            for(tag in Tags.tagName!! ) {
                allTags.add(tag)
            }
            allTagsForPopup.addAll(allTags)
//            var adapter = ArrayAdapter<String>(requireContext(),android.R.layout.select_dialog_item,allTagsForPopup)
            var recyclerGenericAdapter =
                RecyclerGenericAdapter<String>(
                    activity?.applicationContext,
                    PFRecyclerViewAdapter.OnViewHolderClick<String> { view, position, item ->
                        add_tag_new_tag.setText(allTagsForPopup.get(position))
                        tag_suggestions_rv.visibility = View.GONE
                    },
                    RecyclerGenericAdapter.ItemInterface<String?> { obj, viewHolder, position ->
                        (viewHolder.getView(R.id.item) as TextView).text = obj
                    })!!
            recyclerGenericAdapter.setList(allTagsForPopup)
            recyclerGenericAdapter.setLayout(R.layout.dropdown_spinner)

            tag_suggestions_rv.layoutManager = LinearLayoutManager(
                activity?.applicationContext,
                LinearLayoutManager.VERTICAL,
                false
            )
            add_tag_new_tag.threshold = 10
//            autotextview.setAdapter(adapter)
            tag_suggestions_rv.adapter = recyclerGenericAdapter
//            getDialog()?.getWindow()?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//            autotextview.setAdapter(DropdownAdapter(this.requireContext(), allTags))
            add_tag_new_tag.addTextChangedListener (object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().length >0) {
                        allTagsForPopup.clear()
                        tag_suggestions_rv.visibility = View.VISIBLE
                        for(str in allTags){
                            if(str.startsWith(s.toString())){
                                allTagsForPopup.add(str)
                            }
                        }
                        if(allTagsForPopup.size>0)
                        recyclerGenericAdapter.notifyDataSetChanged()
                        else
                            tag_suggestions_rv.visibility = View.GONE
                    } else {
                        tag_suggestions_rv.visibility = View.GONE
                    }
                }
            })
        })

        profileViewModel.userProfileData.observe(this, Observer { profile ->
            bio.setText(profile.bio)

            userTags = profile.tags!!

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
        cancel_button.setOnClickListener {
//            findNavController().navigate(R.id.profileFragment)
            this.dismiss()
        }

        bio.addTextChangedListener (object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                //TODO()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length >= 150) {
                    form_error.visibility = View.VISIBLE
                } else {
                    form_error.visibility = View.GONE
                }
            }
        })

        save_button.setOnClickListener {
            var bioValid: Boolean = false
            if (bio.text.toString().length <= 150) {
                profileViewModel.setProfileBio(bio.text.toString())
                bioValid = true
            } else {
                Toast.makeText(requireContext(), getString(R.string.bio_limit), Toast.LENGTH_LONG).show()
            }
            profileViewModel.setProfileTag(tagsToAdd)
            profileViewModel.removeProfileTag(tagsToRemove)
            if (bioValid) {
//                findNavController().navigate(R.id.profileFragment)
                this.dismiss()
            }
        }

        add_button.setOnClickListener {
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
                Toast.makeText(requireContext(), getString(R.string.tag_cannot_be_empty), Toast.LENGTH_LONG).show()
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