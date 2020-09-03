package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.GigerVerificationStatus
import com.gigforce.app.modules.landingscreen.LandingPageConstants
import com.gigforce.app.modules.landingscreen.LandingPageConstants.INTENT_EXTRA_CAME_FROM_LANDING_SCREEN
import com.gigforce.app.modules.profile.models.ProfileData
import kotlinx.android.synthetic.main.fragment_profile_education_expanded.*
import kotlinx.android.synthetic.main.fragment_profile_education_expanded.view.*
import kotlinx.android.synthetic.main.profile_card_background.view.*
import kotlinx.android.synthetic.main.top_profile_bar.view.*
import kotlinx.android.synthetic.main.verified_button.view.*
import java.text.SimpleDateFormat

class EducationExpandedFragment : ProfileBaseFragment() {

    companion object {
        fun newInstance() = EducationExpandedFragment()

        const val ACTION_OPEN_EDIT_EDUCATION_BOTTOM_SHEET = 11
        const val ACTION_OPEN_EDIT_SKILLS_BOTTOM_SHEET = 12
        const val ACTION_OPEN_EDIT_ACHIEVEMENTS_BOTTOM_SHEET = 13
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        requireActivity().onBackPressedDispatcher.addCallback(this) {
//            findNavController().navigate(R.id.profileFragment)
//        }
    }

    private var cameFromLandingPage = false
    private var action: Int = -1
    private val gigerVerificationViewModel : GigVerificationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        arguments?.let {
            cameFromLandingPage = it.getBoolean(INTENT_EXTRA_CAME_FROM_LANDING_SCREEN)
            action = it.getInt(LandingPageConstants.INTENT_EXTRA_ACTION)
        }

        savedInstanceState?.let {
            cameFromLandingPage = it.getBoolean(INTENT_EXTRA_CAME_FROM_LANDING_SCREEN)
        }

        Log.d("DEBUG", "ENTERED Profile Education Expanded VIEW")
        var view = inflateView(R.layout.fragment_profile_education_expanded, inflater, container)
        view?.nav_bar?.education_active = true
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(INTENT_EXTRA_CAME_FROM_LANDING_SCREEN, cameFromLandingPage)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setListeners()
        initViewModel()
    }

    private fun initViewModel() {

            gigerVerificationViewModel.gigerVerificationStatus.observe(viewLifecycleOwner, Observer {

                val requiredDocsVerified = it.selfieVideoDataModel?.videoPath != null
                        && it.panCardDetails?.state == GigerVerificationStatus.STATUS_VERIFIED
                        && it.bankUploadDetailsDataModel?.state == GigerVerificationStatus.STATUS_VERIFIED
                        && (it.aadharCardDataModel?.state == GigerVerificationStatus.STATUS_VERIFIED || it.drivingLicenseDataModel?.state == GigerVerificationStatus.STATUS_VERIFIED)

                val requiredDocsUploaded = it.selfieVideoDataModel?.videoPath != null
                        && it.panCardDetails?.panCardImagePath != null
                        && it.bankUploadDetailsDataModel?.passbookImagePath != null
                        && (it.aadharCardDataModel?.frontImage != null || it.drivingLicenseDataModel?.backImage != null)

                if (requiredDocsVerified) {
                    education_top_profile.about_me_verification_layout.verification_status_tv.text = getString(R.string.verified_text)
                    education_top_profile.about_me_verification_layout.verification_status_tv.setTextColor(
                        ResourcesCompat.getColor(resources,R.color.green,null))
                    education_top_profile.about_me_verification_layout.status_iv.setImageResource(R.drawable.ic_check)
                    education_top_profile.about_me_verification_layout.verification_status_cardview.strokeColor = ResourcesCompat.getColor(resources,R.color.green,null)
                } else if (requiredDocsUploaded){
                    education_top_profile.about_me_verification_layout.verification_status_tv.text = getString(R.string.under_verification)
                    education_top_profile.about_me_verification_layout.verification_status_tv.setTextColor(
                        ResourcesCompat.getColor(resources,R.color.app_orange,null))
                    education_top_profile.about_me_verification_layout.status_iv.setImageResource(R.drawable.ic_clock_orange)
                    education_top_profile.about_me_verification_layout.verification_status_cardview.strokeColor = ResourcesCompat.getColor(resources,R.color.app_orange,null)
                } else{
                    education_top_profile.about_me_verification_layout.verification_status_tv.text = "Not Verified"
                    education_top_profile.about_me_verification_layout.verification_status_tv.setTextColor(
                        ResourcesCompat.getColor(resources,R.color.red,null))
                    education_top_profile.about_me_verification_layout.status_iv.setImageResource(R.drawable.ic_cross_red)
                    education_top_profile.about_me_verification_layout.verification_status_cardview.strokeColor = ResourcesCompat.getColor(resources,R.color.red,null)
                }
            })

            gigerVerificationViewModel.startListeningForGigerVerificationStatusChanges()
        }


    private fun initialize() {
        profileViewModel.userProfileData.observe(viewLifecycleOwner, Observer { profileObs->
            val profile: ProfileData = profileObs!!
            var educationString: String = ""
            val format = SimpleDateFormat("dd/MM/yyyy")

            profile?.educations?.let {
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

        if (cameFromLandingPage)
            profileViewModel.getProfileData()

        when (action) {
            ACTION_OPEN_EDIT_EDUCATION_BOTTOM_SHEET -> {
                this.findNavController().navigate(R.id.addEducationBottomSheetFragment)
            }
            ACTION_OPEN_EDIT_SKILLS_BOTTOM_SHEET -> {
                this.findNavController().navigate(R.id.addSkillBottomSheetFragment)
            }
            ACTION_OPEN_EDIT_ACHIEVEMENTS_BOTTOM_SHEET -> {
                this.findNavController().navigate(R.id.addAchievementBottomSheetFragment)
            }
        }

    }

    private fun setListeners() {
        // Navigate to bottom sheets
        skill_card.card_bottom.setOnClickListener {
            this.findNavController().navigate(R.id.addSkillBottomSheetFragment)
        }
        achievement_card.card_bottom.setOnClickListener {
            this.findNavController().navigate(R.id.addAchievementBottomSheetFragment)
        }
        education_card.card_bottom.setOnClickListener {
            this.findNavController().navigate(R.id.addEducationBottomSheetFragment)
        }

        education_top_profile.about_me_verification_layout.setOnClickListener {
            navigate(R.id.gigerVerificationFragment)
        }
    }


}