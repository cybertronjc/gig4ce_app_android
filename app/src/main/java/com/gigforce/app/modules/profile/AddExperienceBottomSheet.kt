package com.gigforce.app.modules.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Education
import com.gigforce.app.modules.profile.models.Experience
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_education_bottom_sheet.view.*
import kotlinx.android.synthetic.main.add_experience_bottom_sheet.view.*
import java.text.SimpleDateFormat

class AddExperienceBottomSheet: BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = AddExperienceBottomSheet()
    }

    private lateinit var viewModel: ProfileViewModel
    private lateinit var layout: View
    var updates: ArrayList<Experience> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.add_experience_bottom_sheet, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        layout.add_experience_cancel.setOnClickListener{
            this.findNavController().navigate(R.id.experienceExpandedFragment)
        }

        layout.add_experience_add_more.setOnClickListener{
            addNewExperience()
            layout.add_experience_title.setText("")
            layout.add_experience_employment_type.setText("")
            layout.add_experience_location.setText("")
            layout.add_experience_start_date.setText("")
            layout.add_experience_end_date.setText("")
        }

        layout.add_experience_save.setOnClickListener{
            addNewExperience()

            viewModel.setProfileExperience(updates)
            this.findNavController().navigate(R.id.experienceExpandedFragment)
        }

    }

    private fun addNewExperience() {
        updates.add(
            Experience(
                title = layout.add_experience_title.text.toString(),
                employmentType = layout.add_experience_employment_type.text.toString(),
                location = layout.add_experience_location.text.toString(),
                startDate = SimpleDateFormat("dd/MM/yyyy").parse(layout.add_experience_start_date.text.toString()),
                endDate = SimpleDateFormat("dd/MM/yyyy").parse(layout.add_experience_end_date.text.toString())
            )

        )
    }
}