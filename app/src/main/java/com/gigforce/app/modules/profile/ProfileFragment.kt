package com.gigforce.app.modules.profile

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_profile_main_expanded.view.*
import java.text.SimpleDateFormat

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var viewModel: ProfileViewModel
    private lateinit var storage: FirebaseStorage
    private lateinit var layout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        storage = FirebaseStorage.getInstance()
        Log.d("DEBUG", "ENTERED PROFILE VIEW")
        layout = inflater.inflate(R.layout.fragment_profile_main_expanded, container, false)

        loadImage("ysharma.jpg")

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        // load user data
        viewModel.userProfileData.observe(this, Observer { profile ->
            layout.gigger_rating.text = profile.rating.toString()
            layout.task_done.text = profile.tasksDone.toString()
            layout.connection_count.text = profile.connections.toString()

            var education_string: String = ""
            var format = SimpleDateFormat("dd/MM/yyyy")
            for (education in profile.Education!!) {
                education_string += education.institution + "\n"
                education_string += education.degree + " - " + education.course + "\n"
                education_string += format.format(education.startYear) + " - " + format.format(education.endYear) + "\n\n"
            }
            layout.education_content.text = education_string
            Log.d("ProfileFragment", profile.rating.toString())
        })

        layout.education_view_more.setOnClickListener {
            Log.d("CLICK_STATUS", "CLICK HEARD")
            Toast.makeText(this.context, "View More Clicked", Toast.LENGTH_LONG).show()
            this.findNavController().navigate(R.id.educationExpandedFragment)
        }
    }

    private fun loadImage(Path: String) {
        var profilePicRef: StorageReference = storage.reference.child("profile_pics").child(Path)
        GlideApp.with(this.context!!)
            .load(profilePicRef)
            .into(layout.profile_avatar)
    }
}
