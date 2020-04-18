package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Education
import com.gigforce.app.modules.profile.models.Skill
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_education_bottom_sheet.view.*
import kotlinx.android.synthetic.main.add_skill_bottom_sheet.view.*
import kotlinx.android.synthetic.main.edit_skill_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_profile_education_expanded.view.*
import java.text.SimpleDateFormat

class AddSkillBottomSheetFragment: BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = AddSkillBottomSheetFragment()
    }

    lateinit var layout: View
    var updates: ArrayList<String> = ArrayList()
    lateinit var viewModel: ProfileViewModel
    var skills: ArrayList<String> = ArrayList()
    var selectedSkill: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DEBUG", "ENTERED Profile Education Expanded VIEW")
        layout = inflater.inflate(R.layout.add_skill_bottom_sheet, container, false)

        skills.addAll(listOf("--skill--", "skill1", "skill2", "skill3", "skill4", "skill5", "skill6", "skill7", "skill8"))
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        val skillAdapter = ArrayAdapter(this.context!!, R.layout.simple_spinner_dropdown_item, skills)
        val skillSpinner = layout.add_skill_name
        skillSpinner.adapter = skillAdapter
        skillSpinner.onItemSelectedListener = object:
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedSkill = if (position != 0) skills[position] else ""
                Log.d("Spinner", "selected " + skills[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //TODO("Not yet implemented")
            }
        }

        layout.add_skill_cancel_button.setOnClickListener{
            this.findNavController().navigate(R.id.educationExpandedFragment)
        }

        layout.add_skill_add_more_button.setOnClickListener {
            if (validateSkill()) {
                addNewSkill()
                layout.add_skill_name.setSelection(0)
            }
            else {
                Toast.makeText(this.context, "Invalid Choice", Toast.LENGTH_LONG).show()
            }

        }

        layout.add_skill_save_button.setOnClickListener{
            if (validateSkill()) {
                addNewSkill()

                viewModel.setProfileSkill(updates)
                Toast.makeText(this.context, "Updated Skills Section", Toast.LENGTH_LONG).show()
                this.findNavController().navigate(R.id.educationExpandedFragment)
            }
            else {
                Toast.makeText(this.context, "Invalid Choice", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addNewSkill() {
        updates.add(
            selectedSkill
        )
    }

    private fun validateSkill(): Boolean {
        if (selectedSkill == "")
            return false
        return true
    }
}