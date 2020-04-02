package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Education
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_education_bottom_sheet.view.*
import kotlinx.android.synthetic.main.edit_education_bottom_sheet.*
import kotlinx.android.synthetic.main.edit_education_bottom_sheet.view.*
import kotlinx.android.synthetic.main.edit_education_bottom_sheet.view.end_date
import kotlinx.android.synthetic.main.edit_education_bottom_sheet.view.start_date
import java.text.SimpleDateFormat

class EditEducationBottomSheet: BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = EditEducationBottomSheet()
    }

    lateinit var layout: View
    lateinit var viewModel: ProfileViewModel

    var education: Education? = null
    var arrayLocation: String? = ""

    var degrees: ArrayList<String> = ArrayList()
    var selectedDegree: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            arrayLocation = it.getString("array_location")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("LOCATION", "MYLOC" + arrayLocation.toString())
        layout = inflater.inflate(R.layout.edit_education_bottom_sheet, container, false)
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        degrees.addAll(listOf("--degree--", "Btech", "BA", "MA", "MS", "polytech"))
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val format = SimpleDateFormat("dd/MM/yyyy")
        lateinit var education: Education
        viewModel.userProfileData.observe(this, Observer { profile ->
            if (profile!!.Education!!.size >= 0) {
                education = profile!!.Education!![arrayLocation!!.toInt()]
                layout.institution.setText(education.institution)
                layout.course.setText(education.course)
                selectedDegree = education.degree
                layout.degree.setSelection(degrees.indexOf(education.degree))
                layout.start_date.setText(format.format(education.startYear!!))
                layout.end_date.setText(format.format(education.endYear!!))
            }
        })

        val degreeAdapter = ArrayAdapter(this.context!!, R.layout.simple_spinner_dropdown_item, degrees)
        val degreeSpinner = layout.degree
        degreeSpinner.adapter = degreeAdapter
        degreeSpinner.onItemSelectedListener = object:
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedDegree = degrees[position]
                Log.d("Spinner", "selected " + degrees[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //TODO("Not yet implemented")
            }
        }

        layout.delete.setOnClickListener {
            Log.d("EditEducation", "VOILA!")
            viewModel.removeProfileEducation(education)
            findNavController().navigate(R.id.educationExpandedFragment)
        }

        layout.save.setOnClickListener {
            Log.d("EditEducation", "updating")
            viewModel.removeProfileEducation(education)
            var newEducation = ArrayList<Education>()
            newEducation.add(Education(
                institution = layout.institution.text.toString(),
                course = layout.course.text.toString(),
                degree = selectedDegree.toString(),
                startYear = SimpleDateFormat("dd/MM/yyyy").parse(layout.start_date.text.toString()),
                endYear = SimpleDateFormat("dd/MM/yyyy").parse(layout.end_date.text.toString())
            ))
            viewModel.setProfileEducation(newEducation)
            findNavController().navigate(R.id.educationExpandedFragment)
        }
    }
}