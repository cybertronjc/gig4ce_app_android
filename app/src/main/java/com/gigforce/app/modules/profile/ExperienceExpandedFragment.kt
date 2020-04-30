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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_profile_experience_expanded.view.*
import kotlinx.android.synthetic.main.profile_card_background.view.*
import kotlinx.android.synthetic.main.profile_nav_bar.view.*
import java.text.SimpleDateFormat
import java.util.*

class ExperienceExpandedFragment: Fragment() {

    companion object {
        fun newInstance() = ExperienceExpandedFragment()
    }

    lateinit var storage: FirebaseStorage
    lateinit var viewModel: ProfileViewModel
    lateinit var layout: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        storage = FirebaseStorage.getInstance()
        layout = inflater.inflate(R.layout.fragment_profile_experience_expanded, container, false)

        layout.nav_bar.experience_active = true
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        viewModel.userProfileData.observe(this, Observer { profile ->
            var experienceString = ""
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            profile.Experience?.let {
                val experiences = it.sortedByDescending { experience -> experience.startDate  }
                for (exp in experiences) {
                    experienceString += exp.title + "\n"
                    experienceString += exp.company + "\n"
                    experienceString += exp.employmentType + "\n"
                    experienceString += exp.location + "\n"
                    experienceString += format.format(exp.startDate!!) + "-" + format.format(exp.endDate!!) + "\n\n"
                }
            }
            layout.experience_card.nextDestination = R.id.editExperienceBottomSheet
            layout.experience_card.cardTitle = "Experience"
            layout.experience_card.cardContent = experienceString
            layout.experience_card.cardBottom = "Add Experience"

            layout.experience_top_profile.imageName = profile.profileAvatarName
            layout.experience_top_profile.userName = profile.name
        })

        layout.experience_card.card_bottom.setOnClickListener {
            findNavController().navigate(R.id.addExperienceBottomSheet)
        }

    }

}