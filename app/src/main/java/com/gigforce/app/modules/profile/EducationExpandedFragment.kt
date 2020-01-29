package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gigforce.app.R
import com.google.firebase.storage.FirebaseStorage

class EducationExpandedFragment: Fragment() {

    companion object {
        fun newInstance() = EducationExpandedFragment()
    }

    private lateinit var layout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DEBUG", "ENTERED Profile Education Expanded VIEW")
        layout = inflater.inflate(R.layout.fragment_profile_education_expanded, container, false)
        return layout
    }
}