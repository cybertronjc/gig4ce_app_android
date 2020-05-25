package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import kotlinx.android.synthetic.main.fragment_profile_education_expanded.*
import kotlinx.android.synthetic.main.fragment_profile_education_expanded.view.*
import kotlinx.android.synthetic.main.fragment_profile_education_expanded.view.nav_bar
import kotlinx.android.synthetic.main.profile_card_background.view.*
import java.text.SimpleDateFormat

class EducationExpandedFragment: ProfileBaseFragment() {

    companion object {
        fun newInstance() = EducationExpandedFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.profileFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DEBUG", "ENTERED Profile Education Expanded VIEW")
        inflateView(R.layout.fragment_profile_education_expanded, inflater, container)
        getFragmentView().nav_bar.education_active = true
        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        setListeners()
    }

    private fun initialize() {
        profileViewModel.userProfileData.observe(viewLifecycleOwner, Observer { profile ->
            var educationString: String = ""
            val format = SimpleDateFormat("dd/MM/yyyy")

            profile.educations?.let {
                val educations = it.sortedByDescending { education -> education.startYear!! }
                for (education in educations) {
                    educationString += education.institution + "\n"
                    educationString += education.degree + " - " + education.course + "\n"
                    educationString += format.format(education.startYear!!) + " - " + format.format(
                        education.endYear!!
                    ) + "\n\n"
                }
            }
            education_card.nextDestination = R.id.editEducationBottomSheet
            education_card.cardTitle = "Education"
            education_card.cardContent = educationString
            education_card.cardBottom = "Add education"

            var skillString: String = ""
            profile.skills?.let {
                for (skill in it) {
                    skillString += skill.id + "\n\n"
                }
            }
            skill_card.nextDestination = R.id.editSkillBottomSheet
            skill_card.hasContentTitles = false
            skill_card.cardTitle = "Skills"
            skill_card.cardContent = skillString
            skill_card.cardBottom = "Add skills"

            var achievementString: String = ""
            profile.achievements?.let {
                val achievements = it.sortedByDescending { achievement -> achievement.year }
                for (achievement in achievements) {
                    achievementString += achievement.title + "\n"
                    achievementString += achievement.issuingAuthority + "\n"
                    if (achievement.location != "")
                        achievementString += achievement.location + "\n"
                    achievementString += achievement.year + "\n\n"
                }
            }
            achievement_card.nextDestination = R.id.editAchievementBottomSheet
            achievement_card.cardTitle = "Achievement"
            achievement_card.cardContent = achievementString
            achievement_card.cardBottom = "Add achievements"

            education_top_profile.userName = profile.name
            education_top_profile.imageName = profile.profileAvatarName
        })

    }

    private fun setListeners() {
        // Navigate to bottom sheets
        skill_card.card_bottom.setOnClickListener{
            this.findNavController().navigate(R.id.addSkillBottomSheetFragment)
        }
        achievement_card.card_bottom.setOnClickListener{
            this.findNavController().navigate(R.id.addAchievementBottomSheetFragment)
        }
        education_card.card_bottom.setOnClickListener{
            this.findNavController().navigate(R.id.addEducationBottomSheetFragment)
        }
    }
}