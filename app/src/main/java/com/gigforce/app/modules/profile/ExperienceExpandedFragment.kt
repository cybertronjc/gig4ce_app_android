package com.gigforce.app.modules.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import kotlinx.android.synthetic.main.fragment_profile_about_expanded.view.*
import kotlinx.android.synthetic.main.fragment_profile_education_expanded.view.*
import kotlinx.android.synthetic.main.fragment_profile_education_expanded.view.profile_nav_to_education
import kotlinx.android.synthetic.main.fragment_profile_experience_expanded.view.*
import kotlinx.android.synthetic.main.fragment_profile_main_expanded.view.*
import java.text.SimpleDateFormat
import java.util.*

class ExperienceExpandedFragment: Fragment() {

    companion object {
        fun newInstance() = ExperienceExpandedFragment()
    }

    lateinit var viewModel: ProfileViewModel
    lateinit var layout: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.fragment_profile_experience_expanded, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        viewModel.userProfileData.observe(this, Observer { profile ->
            var experienceString = ""
            var format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            for (exp in profile.Experience!!) {
                experienceString += exp.company + "\n"
                experienceString += exp.position + "\n"
                experienceString += format.format(exp.startDate!!) + "-" + format.format(exp.endDate!!) + "\n\n"
            }
            layout.experience_exp_experience_content.text = experienceString
        })

        layout.add_experience_button.setOnClickListener {
            findNavController().navigate(R.id.addExperienceBottomSheet)
        }

        layout.profile_nav_to_about.setOnClickListener{
            findNavController().navigate(R.id.aboutExpandedFragment)
        }

        layout.experience_exp_nav_education.setOnClickListener{
            findNavController().navigate(R.id.educationExpandedFragment)
        }

        layout.experience_expanded_back_button.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
    }
}