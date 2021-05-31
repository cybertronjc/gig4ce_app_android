package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.core.datamodels.profile.Skill
import com.gigforce.common_ui.adapter.DropdownAdapter
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.Lce
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.delete_confirmation_dialog.*
import kotlinx.android.synthetic.main.edit_skill_bottom_sheet.*
import kotlinx.android.synthetic.main.edit_skill_bottom_sheet.cancel
import kotlinx.android.synthetic.main.edit_skill_bottom_sheet.form_error
import kotlinx.android.synthetic.main.edit_skill_bottom_sheet.loading_skills_pb
import kotlinx.android.synthetic.main.edit_skill_bottom_sheet.view.*
import kotlinx.android.synthetic.main.edit_skill_bottom_sheet.view.skill

class EditSkillBottomSheet: ProfileBaseBottomSheetFragment() {
    companion object {
        fun newInstance() = EditSkillBottomSheet()
    }

    var arrayLocation: String = ""
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

        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViewModel()
        setListeners()
    }

    private fun initViewModel() {
        profileViewModel.fetchUserInterestDataState
                .observe(viewLifecycleOwner, {

                    when (it) {
                        Lce.Loading -> {
                            edit_skills_layout.gone()
                            loading_skills_pb.visible()
                        }
                        is Lce.Content -> {
                            loading_skills_pb.gone()
                            edit_skills_layout.visible()

                            initialize(it.content.interest.map {
                                it.skill
                            })
                        }
                        is Lce.Error -> {

                            MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Unable to load Skills")
                                    .setMessage(it.error)
                                    .setPositiveButton("Okay") { _, _ -> profileViewModel.getInterestForUser(null, false) }
                                    .show()
                        }
                        else -> {
                        }
                    }
                })

        profileViewModel.getInterestForUser(null,false)
    }

    private fun initialize(skills : List<String>) {
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