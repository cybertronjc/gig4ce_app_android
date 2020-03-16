package com.gigforce.app.modules.profile

import android.graphics.Color
import android.net.Uri
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
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_profile_education_expanded.view.*
import kotlinx.android.synthetic.main.fragment_profile_main_expanded.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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
            layout.gigger_rating.text = profile.rating!!.getTotal().toString()
            layout.task_done.text = profile.tasksDone.toString()
            layout.connection_count.text = profile.connections.toString()
            layout.main_expanded_user_name.text = profile.name
            layout.user_about_me.text = profile.aboutMe

            Log.d("ProfileFragment", profile.isVerified.toString())
            if (profile.isVerified) {
                layout.main_expanded_is_verified.setBackgroundColor(Color.parseColor("#00FF00"))
            }

            var tagsString = ""
            for (tag in profile.Tags!!) {
                tagsString += "$tag  "
            }
            //layout.main_tags.text = tagsString

            var educationString = ""
            var format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            for (education in profile.Education!!) {
                educationString += education.institution + "\n"
                educationString += education.degree + " - " + education.course + "\n"
                educationString += format.format(education.startYear!!) + " - " + format.format(education.endYear!!) + "\n\n"
            }
            Log.d("ProfileFragment", educationString)
            layout.education_content.text = educationString

            var experienceString = ""
            for (exp in profile.Experience!!) {
                experienceString += exp.title + "\n"
                experienceString += exp.employmentType + "\n"
                experienceString += exp.location + "\n"
                experienceString += format.format(exp.startDate!!) + "-" + format.format(exp.endDate!!) + "\n\n"
            }
            layout.experience_content.text = experienceString

            layout.about_card_content.text = profile.bio.toString()
            Log.d("ProfileFragment", profile.rating.toString())
        })

        layout.add_tags_button.setOnClickListener{
            this.findNavController().navigate(R.id.addTagBottomSheet)
        }

        layout.about_card_view_more_button.setOnClickListener{
            this.findNavController().navigate(R.id.aboutExpandedFragment)
        }

        layout.education_view_more.setOnClickListener {
            Log.d("CLICK_STATUS", "CLICK HEARD")
            Toast.makeText(this.context, "View More Clicked", Toast.LENGTH_LONG).show()
            this.findNavController().navigate(R.id.educationExpandedFragment)
        }

        layout.experience_card_view_more.setOnClickListener {
            this.findNavController().navigate(R.id.experienceExpandedFragment)
        }

        // back page navigation
        layout.profile_main_expanded_back_button.setOnClickListener{
            this.findNavController().navigate(R.id.homeFragment)
        }
    }

    private fun loadImage(Path: String) {
        var profilePicRef: StorageReference = storage.reference.child("profile_pics").child(Path)
        GlideApp.with(this.context!!)
            .load(profilePicRef)
            .into(layout.profile_avatar)
    }
}
