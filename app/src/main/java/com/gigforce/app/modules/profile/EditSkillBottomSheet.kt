package com.gigforce.app.modules.profile

import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.gigforce.app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.edit_skill_bottom_sheet.view.*

class EditSkillBottomSheet: BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = EditSkillBottomSheet()
    }

    var arrayLocation: String = ""
    var skills: ArrayList<String> = ArrayList()
    var selectedSkill: String = ""
    lateinit var layout: View
    lateinit var viewModel: ProfileViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            arrayLocation = it.getString("array_location")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.edit_skill_bottom_sheet, container, false)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        skills.addAll(listOf("--skill--", "skill1", "skill2", "skill3", "skill4", "skill5", "skill6", "skill7", "skill8"))

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        lateinit var skill: String
        val skillAdapter = ArrayAdapter(this.context!!, R.layout.simple_spinner_dropdown_item, skills)
        val skillSpinner = layout.skill
        skillSpinner.adapter = skillAdapter
        skillSpinner.onItemSelectedListener = object:
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedSkill = skills[position]
                Log.d("Spinner", "selected " + skills[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //TODO("Not yet implemented")
            }
        }

        viewModel.userProfileData.observe(this, Observer { profile ->
            skill = profile.Skill!![arrayLocation.toInt()]
            layout.skill.setSelection(skills.indexOf(skill))
        })

        layout.delete.setOnClickListener {
            MaterialDialog(this.context!!).show {
                title(text = "Confirm Delete")
                message(text = "Are you sure to Delete this item?")
                positiveButton(R.string.delete) {
                    viewModel.removeProfileSkill(skill)
                    findNavController().navigate(R.id.educationExpandedFragment)
                }
                negativeButton(R.string.cancel_text) {

                }
            }
            Log.d("EditSkill", "Skill deleted" + skill)
        }

        layout.save.setOnClickListener {
            viewModel.removeProfileSkill(skill)
            var skills: ArrayList<String> = ArrayList()
            skills.add(selectedSkill)
            viewModel.setProfileSkill(skills)
            findNavController().navigate(R.id.educationExpandedFragment)
        }
    }

}