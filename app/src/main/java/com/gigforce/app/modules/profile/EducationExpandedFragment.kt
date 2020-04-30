package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_profile_education_expanded.view.*
import kotlinx.android.synthetic.main.fragment_profile_education_expanded.view.nav_bar
import kotlinx.android.synthetic.main.fragment_profile_experience_expanded.view.*
import kotlinx.android.synthetic.main.profile_card_background.view.*
import kotlinx.android.synthetic.main.profile_nav_bar.view.*
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

        layout.nav_bar.education_active = true
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storage = FirebaseStorage.getInstance()

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        viewModel.userProfileData.observe(this, Observer { profile ->
            var educationString: String = ""
            val format = SimpleDateFormat("dd/MM/yyyy")

            profile.Education?.let {
                val educations = it.sortedByDescending { education -> education.startYear!! }
                for (education in educations) {
                    educationString += education.institution + "\n"
                    educationString += education.degree + " - " + education.course + "\n"
                    educationString += format.format(education.startYear!!) + " - " + format.format(
                        education.endYear!!
                    ) + "\n\n"
                }
            }
            layout.education_card.nextDestination = R.id.editEducationBottomSheet
            layout.education_card.cardTitle = "Education"
            layout.education_card.cardContent = educationString
            layout.education_card.cardBottom = "Add Education"

            var skillString: String = ""
            profile.Skill?.let {
                for (skill in it) {
                    skillString += skill + "\n\n"
                }
            }
            layout.skill_card.nextDestination = R.id.editSkillBottomSheet
            layout.skill_card.hasContentTitles = false
            layout.skill_card.cardTitle = "Skills"
            layout.skill_card.cardContent = skillString
            layout.skill_card.cardBottom = "Add Skill"

            var achievementString: String = ""
            profile.Achievement?.let {
                val achievements = it.sortedByDescending { achievement -> achievement.year }
                for (achievement in achievements) {
                    achievementString += achievement.title + "\n"
                    achievementString += achievement.issuingAuthority + "\n"
                    if (achievement.location != "")
                        achievementString += achievement.location + "\n"
                    achievementString += achievement.year + "\n\n"
                }
            }
            layout.achievement_card.nextDestination = R.id.editAchievementBottomSheet
            layout.achievement_card.cardTitle = "Achievement"
            layout.achievement_card.cardContent = achievementString
            layout.achievement_card.cardBottom = "Add Achievement"

            layout.education_top_profile.userName = profile.name
            layout.education_top_profile.imageName = profile.profileAvatarName

            Log.d("ProfileFragment", profile.rating.toString())
        })


        // Navigate to bottom sheets
        layout.skill_card.card_bottom.setOnClickListener{
            this.findNavController().navigate(R.id.addSkillBottomSheetFragment)
        }
        layout.achievement_card.card_bottom.setOnClickListener{
            this.findNavController().navigate(R.id.addAchievementBottomSheetFragment)
        }
        layout.education_card.card_bottom.setOnClickListener{
            this.findNavController().navigate(R.id.addEducationBottomSheetFragment)
        }
    }
}