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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_education_bottom_sheet.view.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class ProfilePictureOptionsBottomSheetFragment: BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = AddSkillBottomSheetFragment()
    }

    lateinit var layout: View
    var updates: ArrayList<Education> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DEBUG", "ENTERED Profile Education Expanded VIEW")
        layout = inflater.inflate(R.layout.profile_photo_bottom_sheet, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layout.cancel_button.setOnClickListener{
            this.findNavController().navigate(R.id.photoCrop)
        }


        layout.save_button.setOnClickListener{
            addNewEducation()

            Toast.makeText(this.context, "Updated Education Section", Toast.LENGTH_LONG)
            this.findNavController().navigate(R.id.educationExpandedFragment)
        }
    }

    private fun addNewEducation() {
        updates.add(Education(
            institution = layout.institution_name.text.toString(),
            course = layout.course_name.text.toString(),
            degree = layout.degree_name.text.toString(),
            startYear = SimpleDateFormat("dd/MM/yyyy").parse(layout.start_date.text.toString()),
            endYear = SimpleDateFormat("dd/MM/yyyy").parse(layout.end_date.text.toString())
        ))
    }
}