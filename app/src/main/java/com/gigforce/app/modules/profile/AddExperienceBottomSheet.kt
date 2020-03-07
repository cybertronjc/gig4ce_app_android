package com.gigforce.app.modules.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddExperienceBottomSheet: BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = AddExperienceBottomSheet()
    }

    private lateinit var layout: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.add_experience_bottom_sheet, container, false)
        return layout
    }
}