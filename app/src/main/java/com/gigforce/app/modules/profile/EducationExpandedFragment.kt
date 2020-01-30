package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gigforce.app.R
import com.gigforce.app.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_profile_main_expanded.view.*

class EducationExpandedFragment: Fragment() {

    companion object {
        fun newInstance() = EducationExpandedFragment()
    }

    private lateinit var storage: FirebaseStorage

    private lateinit var layout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DEBUG", "ENTERED Profile Education Expanded VIEW")
        layout = inflater.inflate(R.layout.fragment_profile_education_expanded, container, false)
        return layout
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        storage = FirebaseStorage.getInstance()
//        loadImage("ysharma.jpg")
//    }
//
//    private fun loadImage(Path: String) {
//        val profilePicRef: StorageReference = storage.reference.child("profile_pics").child(Path)
//        GlideApp.with(this.context!!)
//            .load(profilePicRef)
//            .into(layout.profile_avatar)
//    }
}