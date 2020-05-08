package com.gigforce.app.modules.profile

import android.os.Bundle
import android.os.DropBoxManager
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
import com.gigforce.app.utils.DropdownAdapter
import kotlinx.android.synthetic.main.add_skill_bottom_sheet.*

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

        skills.addAll(listOf("skill1", "skill2", "skill3", "skill4", "skill5", "skill6", "skill7", "skill8"))
        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    private fun setListeners() {
        val skillAdapter = DropdownAdapter(this.requireContext(), skills)
        val skillSpinner = add_skill_skill_name
        skillSpinner.setAdapter(skillAdapter)

        add_more_button.setOnClickListener {
            if (validateSkill()) {
                addNewSkill()
                add_skill_skill_name.setText("")
            }
        }

        add_skill_cancel_button.setOnClickListener{
            this.findNavController().navigate(R.id.educationExpandedFragment)
        }

        add_skill_save_button.setOnClickListener{
            if (validateSkill()) {
                addNewSkill()

                profileViewModel!!.setProfileSkill(updates)
                this.findNavController().navigate(R.id.educationExpandedFragment)
            }
        }

    }

    private fun addNewSkill() {
        hideError(form_error, add_skill_skill_name)
        updates.add(
            add_skill_skill_name.text.toString()
        )
    }

    private fun validateSkill(): Boolean {
        Log.d("AddSkill", "validating skill " + add_skill_skill_name.text.toString())
        if (validation!!.isValidSkill(add_skill_skill_name.text.toString()))
            return true
        else {
            add_skill_skill_name.setHintTextColor(resources.getColor(R.color.colorError))
            showError(form_error, add_skill_skill_name)
            return false
        }
    }
}