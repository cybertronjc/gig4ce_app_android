package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.gigforce.core.datamodels.profile.Skill
import com.gigforce.common_ui.adapter.DropdownAdapter
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.Lce
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.add_skill_bottom_sheet.*

class AddSkillBottomSheetFragment : ProfileBaseBottomSheetFragment() {
    companion object {
        fun newInstance() = AddSkillBottomSheetFragment()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Log.d("DEBUG", "ENTERED Profile Education Expanded VIEW")
        inflateView(R.layout.add_skill_bottom_sheet, inflater, container)
        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
    }

    private fun initViewModel() {
        profileViewModel.getProfileData()
                .observe(viewLifecycleOwner,{
                    val skillsSize = it.skills?.size ?: 0

                    if(skillsSize >= 3){
                        //Disable buttons
                        add_more_button.isEnabled = false
                        add_skill_save_button.isEnabled = false
                    } else {
                        add_more_button.isEnabled = true
                        add_skill_save_button.isEnabled = true
                    }

                })

        profileViewModel.fetchUserInterestDataState
                .observe(viewLifecycleOwner, {

                    when (it) {
                        Lce.Loading -> {
                            add_intrest_layout.gone()
                            loading_skills_pb.visible()
                        }
                        is Lce.Content -> {
                            loading_skills_pb.gone()
                            add_intrest_layout.visible()

                            setListeners(it.content.interest.map {
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

    private fun setListeners(skills: List<String>) {
        val skillAdapter = DropdownAdapter(this.requireContext(), skills)
        val skillSpinner = add_skill_skill_name
        skillSpinner.setAdapter(skillAdapter)

        add_more_button.setOnClickListener {
            if (validateSkill()) {
                addNewSkill()
                add_skill_skill_name.setText("")
            }
        }

        add_skill_cancel_button.setOnClickListener {
            this.dismiss()
        }

        add_skill_save_button.setOnClickListener {
            if (validateSkill()) {
                addNewSkill()
                this.dismiss()
            }
        }

    }

    private fun addNewSkill() {
        hideError(form_error, add_skill_skill_name)
        profileViewModel.setProfileSkill(
                Skill(
                        add_skill_skill_name.text.toString()
                )
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