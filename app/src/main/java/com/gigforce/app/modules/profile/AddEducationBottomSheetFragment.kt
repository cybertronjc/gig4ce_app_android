package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_education_bottom_sheet.view.*

class AddEducationBottomSheetFragment: BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = AddSkillBottomSheetFragment()
    }

    lateinit var layout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DEBUG", "ENTERED Profile Education Expanded VIEW")
        layout = inflater.inflate(R.layout.add_education_bottom_sheet, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layout.cancel_button.setOnClickListener{
            this.findNavController().navigate(R.id.educationExpandedFragment)
        }
    }
}