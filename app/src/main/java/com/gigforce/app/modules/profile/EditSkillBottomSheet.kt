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
import com.afollestad.materialdialogs.internal.main.DialogLayout
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Skill
import com.gigforce.app.utils.DropdownAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.delete_confirmation_dialog.*
import kotlinx.android.synthetic.main.edit_skill_bottom_sheet.*
import kotlinx.android.synthetic.main.edit_skill_bottom_sheet.cancel
import kotlinx.android.synthetic.main.edit_skill_bottom_sheet.view.*
import kotlinx.android.synthetic.main.edit_skill_bottom_sheet.view.skill

class EditSkillBottomSheet: ProfileBaseBottomSheetFragment() {
    companion object {
        fun newInstance() = EditSkillBottomSheet()
    }

    var arrayLocation: String = ""
    var skills: ArrayList<String> = ArrayList()
    var selectedSkill: String = ""
    lateinit var currentSkill: Skill

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
        skills.addAll(listOf(
            "Driving", "Cooking", "Shop-keeping", "Managing Catalog",
            "Cashier", "Tele-calling", "Waiter", "Bartender", "Barback",
            "Fleet Management", "Assembly-dismantling", "E-commence delivery",
            "Admin assistant", "Store manager", "In-store promoter",
            "Record keeper", "Barista", "House keeping", "Reception", "Artist"))

        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initialize()
        setListeners()
    }

    private fun initialize() {
        val skillAdapter = DropdownAdapter(this.requireContext(), skills)
        val skillSpinner = skill
        skillSpinner.setAdapter(skillAdapter)

        profileViewModel.userProfileData.observe(this, Observer { profile ->
            currentSkill = profile?.skills!![arrayLocation.toInt()]
            skill.setText(currentSkill.id, false)
        })
    }

    private fun setListeners() {

        delete.setOnClickListener {
            val dialog = getDeleteConfirmationDialog(requireContext())
            dialog.yes.setOnClickListener {
                profileViewModel.removeProfileSkill(currentSkill)
                findNavController().navigate(R.id.educationExpandedFragment)
                dialog .dismiss()
            }
            dialog.show()

            Log.d("EditSkill", "Skill deleted" + skill)
        }

        save.setOnClickListener {
            if (validateSkill()) {
                profileViewModel.removeProfileSkill(currentSkill)
                profileViewModel.setProfileSkill(Skill(skill.text.toString()))
                findNavController().navigate(R.id.educationExpandedFragment)
            }
        }

        cancel.setOnClickListener {
            findNavController().navigate(R.id.educationExpandedFragment)
        }

    }

    private fun validateSkill(): Boolean {
        if (validation!!.isValidSkill(skill.text.toString())) {
            return true
        } else{
            showError(form_error)
            return false
        }
    }
}