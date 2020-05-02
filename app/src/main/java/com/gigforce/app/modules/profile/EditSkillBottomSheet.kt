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
import kotlinx.android.synthetic.main.edit_skill_bottom_sheet.*
import kotlinx.android.synthetic.main.edit_skill_bottom_sheet.view.*
import kotlinx.android.synthetic.main.edit_skill_bottom_sheet.view.skill

class EditSkillBottomSheet: ProfileBaseBottomSheetFragment() {
    companion object {
        fun newInstance() = EditSkillBottomSheet()
    }

    var arrayLocation: String = ""
    var skills: ArrayList<String> = ArrayList()
    var selectedSkill: String = ""
    lateinit var currentSkill: String

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
        inflateView(R.layout.edit_skill_bottom_sheet, inflater, container)
        skills.addAll(listOf("--skill--", "skill1", "skill2", "skill3", "skill4", "skill5", "skill6", "skill7", "skill8"))

        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initialize()
        setListeners()
    }

    private fun initialize() {
        val skillAdapter = ArrayAdapter(this.context!!, R.layout.simple_spinner_dropdown_item, skills)
        val skillSpinner = skill
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

        profileViewModel!!.userProfileData.observe(this, Observer { profile ->
            currentSkill = profile.skills!![arrayLocation.toInt()]
            skill.setSelection(skills.indexOf(currentSkill))
        })
    }

    private fun setListeners() {

        delete.setOnClickListener {
            MaterialDialog(this.context!!).show {
                title(text = "Confirm Delete")
                message(text = "Are you sure to Delete this item?")
                positiveButton(R.string.delete) {
                    profileViewModel!!.removeProfileSkill(currentSkill)
                    findNavController().navigate(R.id.educationExpandedFragment)
                }
                negativeButton(R.string.cancel_text) {

                }
            }
            Log.d("EditSkill", "Skill deleted" + skill)
        }

        save.setOnClickListener {
            if (validateSkill()) {
                profileViewModel!!.removeProfileSkill(currentSkill)
                var skills: ArrayList<String> = ArrayList()
                skills.add(selectedSkill)
                profileViewModel!!.setProfileSkill(skills)
                findNavController().navigate(R.id.educationExpandedFragment)
            } else {
                Toast.makeText(this.context, "Invalid Choice", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun validateSkill(): Boolean {
        if (validation!!.isValidSkill(selectedSkill)) {
            return true
        } else{
            showError(form_error)
            return false
        }
    }
}