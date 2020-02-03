package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_profile_education_expanded.view.*
import kotlinx.android.synthetic.main.fragment_profile_main_expanded.view.*
import java.text.SimpleDateFormat

class EducationExpandedFragment: Fragment() {

    companion object {
        fun newInstance() = EducationExpandedFragment()
    }

    private lateinit var storage: FirebaseStorage
    lateinit var viewModel: ProfileViewModel

    private lateinit var layout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DEBUG", "ENTERED Profile Education Expanded VIEW")
        layout = inflater.inflate(R.layout.fragment_profile_education_expanded, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storage = FirebaseStorage.getInstance()
        loadImage("ysharma.jpg")

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        viewModel.userProfileData.observe(this, Observer { profile ->
            var educationString: String = ""
            var format = SimpleDateFormat("dd/MM/yyyy")
            for (education in profile.Education!!) {
                educationString += education.institution + "\n"
                educationString += education.degree + " - " + education.course + "\n"
                educationString += format.format(education.startYear!!) + " - " + format.format(education.endYear!!) + "\n\n"
            }
            layout.education_exp_education_content.text = educationString

            var skillString: String = ""
            for (skill in profile.Skill!!) {
                skillString += skill.category + "\n"
                skillString += skill.nameOfSkill + "\n\n"
            }
            layout.education_exp_skill_content.text = skillString

            Log.d("ProfileFragment", profile.rating.toString())
        })

        // back page navigation
        layout.education_expanded_back_button.setOnClickListener{
            this.findNavController().navigate(R.id.profileFragment)
        }

        // Navigate to bottom sheets
        layout.add_skill_button.setOnClickListener{
            this.findNavController().navigate(R.id.addSkillBottomSheetFragment)
        }
        layout.add_achievement_button.setOnClickListener{
            this.findNavController().navigate(R.id.addAchievementBottomSheetFragment)
        }
        layout.add_education_button.setOnClickListener{
            this.findNavController().navigate(R.id.addEducationBottomSheetFragment)
        }
    }

    private fun loadImage(Path: String) {
        val profilePicRef: StorageReference = storage.reference.child("profile_pics").child(Path)
        GlideApp.with(this.context!!)
            .load(profilePicRef)
            .into(layout.education_profile_avatar)
    }
}