package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Education
import com.gigforce.app.modules.profile.models.Skill
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_education_bottom_sheet.view.*
import kotlinx.android.synthetic.main.add_skill_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_profile_education_expanded.view.*
import java.text.SimpleDateFormat

class AddSkillBottomSheetFragment: BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = AddSkillBottomSheetFragment()
    }

    lateinit var layout: View
    var updates: ArrayList<String> = ArrayList()
    lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DEBUG", "ENTERED Profile Education Expanded VIEW")
        layout = inflater.inflate(R.layout.add_skill_bottom_sheet, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        layout.add_skill_cancel_button.setOnClickListener{
            this.findNavController().navigate(R.id.educationExpandedFragment)
        }

        layout.add_skill_add_more_button.setOnClickListener {
            addNewSkill()
            layout.add_skill_category.setText("")
            layout.add_skill_name.setText("")

        }

        layout.add_skill_save_button.setOnClickListener{
            addNewSkill()

            viewModel.setProfileSkill(updates)
            Toast.makeText(this.context, "Updated Skills Section", Toast.LENGTH_LONG)
            this.findNavController().navigate(R.id.educationExpandedFragment)
        }
    }

    private fun addNewSkill() {
        updates.add(
            layout.add_skill_name.text.toString()
//            Skill(
//                //category = layout.add_skill_category.text.toString(),
//                //nameOfSkill = layout.add_skill_name.text.toString()
//            )
        )
    }
}