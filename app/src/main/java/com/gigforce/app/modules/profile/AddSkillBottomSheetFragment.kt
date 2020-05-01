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
import kotlinx.android.synthetic.main.add_skill_bottom_sheet.*
import kotlinx.android.synthetic.main.add_skill_bottom_sheet.view.*
import kotlinx.android.synthetic.main.add_skill_bottom_sheet.view.add_skill_name
import kotlinx.android.synthetic.main.edit_skill_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_profile_education_expanded.view.*
import java.text.SimpleDateFormat

class AddSkillBottomSheetFragment: ProfileBaseBottomSheetFragment() {
    companion object {
        fun newInstance() = AddSkillBottomSheetFragment()
    }

    var updates: ArrayList<String> = ArrayList()
    var skills: ArrayList<String> = ArrayList()
    var selectedSkill: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DEBUG", "ENTERED Profile Education Expanded VIEW")
        inflateView(R.layout.add_skill_bottom_sheet, inflater, container)

        skills.addAll(listOf("--skill--", "skill1", "skill2", "skill3", "skill4", "skill5", "skill6", "skill7", "skill8"))
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    private fun setListeners() {
        val skillAdapter = ArrayAdapter(this.context!!, R.layout.simple_spinner_dropdown_item, skills)
        val skillSpinner = add_skill_name
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

        add_more_button.setOnClickListener {
            if (validateSkill()) {
                addNewSkill()
                add_skill_name.setSelection(0)
            }
        }

        add_skill_cancel_button.setOnClickListener{
            this.findNavController().navigate(R.id.educationExpandedFragment)
        }

        add_skill_save_button.setOnClickListener{
            if (validateSkill()) {
                addNewSkill()

                profileViewModel!!.setProfileSkill(updates)
                Toast.makeText(this.context, "Updated Skills Section", Toast.LENGTH_LONG).show()
                this.findNavController().navigate(R.id.educationExpandedFragment)
            }
        }

    }

    private fun addNewSkill() {
        showError(form_error)
        updates.add(
            selectedSkill
        )
    }

    private fun validateSkill(): Boolean {
        if (validation!!.isValidSkill(selectedSkill))
            return true
        else {
            showError(form_error)
            return false
        }
    }
}