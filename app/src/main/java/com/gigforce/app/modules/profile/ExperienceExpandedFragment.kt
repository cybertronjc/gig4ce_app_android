package com.gigforce.app.modules.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
//import com.gigforce.landing_screen.landingscreen.LandingPageConstants
//import com.gigforce.landing_screen.landingscreen.LandingPageConstants.INTENT_EXTRA_CAME_FROM_LANDING_SCREEN
import com.gigforce.common_ui.datamodels.GigerVerificationStatus
import com.gigforce.common_ui.viewmodels.GigVerificationViewModel
import com.gigforce.core.AppConstants.INTENT_EXTRA_ACTION
import com.gigforce.core.AppConstants.INTENT_EXTRA_CAME_FROM_LANDING_SCREEN
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_profile_about_expanded.*
import kotlinx.android.synthetic.main.fragment_profile_experience_expanded.*
import kotlinx.android.synthetic.main.fragment_profile_experience_expanded.nav_bar
import kotlinx.android.synthetic.main.fragment_profile_experience_expanded.view.*
import kotlinx.android.synthetic.main.profile_card_background.view.*
import kotlinx.android.synthetic.main.top_profile_bar.view.*
import kotlinx.android.synthetic.main.verified_button.view.*
import java.text.SimpleDateFormat
import java.util.*


class ExperienceExpandedFragment : ProfileBaseFragment() {

    companion object {
        fun newInstance() = ExperienceExpandedFragment()

        const val ACTION_OPEN_EDIT_EXPERIENCE_BOTTOM_SHEET = 21
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        requireActivity().onBackPressedDispatcher.addCallback(this) {
//            findNavController().navigate(R.id.profileFragment)
//        }
    }

    private var cameFromLandingPage = false
    private var action: Int = -1
    private val gigerVerificationViewModel: GigVerificationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        arguments?.let {
            cameFromLandingPage = it.getBoolean(INTENT_EXTRA_CAME_FROM_LANDING_SCREEN)
            action = it.getInt(INTENT_EXTRA_ACTION)
        }

        savedInstanceState?.let {
            cameFromLandingPage = it.getBoolean(INTENT_EXTRA_CAME_FROM_LANDING_SCREEN)
        }

        var view = inflateView(R.layout.fragment_profile_experience_expanded, inflater, container)
        view?.nav_bar?.experience_active = true
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

            if (it.requiredDocsVerified) {
                experience_top_profile.about_me_verification_layout.verification_status_tv.text = getString(R.string.verified_text)
                experience_top_profile.about_me_verification_layout.verification_status_tv.setTextColor(
                        ResourcesCompat.getColor(resources, R.color.green, null))
                experience_top_profile.about_me_verification_layout.status_iv.setImageResource(R.drawable.ic_check)
                experience_top_profile.about_me_verification_layout.verification_status_cardview.strokeColor = ResourcesCompat.getColor(resources, R.color.green, null)
            } else if (it.requiredDocsUploaded) {
                experience_top_profile.about_me_verification_layout.verification_status_tv.text = getString(R.string.under_verification)
                experience_top_profile.about_me_verification_layout.verification_status_tv.setTextColor(
                        ResourcesCompat.getColor(resources, R.color.app_orange, null))
                experience_top_profile.about_me_verification_layout.status_iv.setImageResource(R.drawable.ic_clock_orange)
                experience_top_profile.about_me_verification_layout.verification_status_cardview.strokeColor = ResourcesCompat.getColor(resources, R.color.app_orange, null)
            } else {
                experience_top_profile.about_me_verification_layout.verification_status_tv.text = "Not Verified"
                experience_top_profile.about_me_verification_layout.verification_status_tv.setTextColor(
                        ResourcesCompat.getColor(resources, R.color.red, null))
                experience_top_profile.about_me_verification_layout.status_iv.setImageResource(R.drawable.ic_cross_red)
                experience_top_profile.about_me_verification_layout.verification_status_cardview.strokeColor = ResourcesCompat.getColor(resources, R.color.red, null)
            }
        })

        gigerVerificationViewModel.startListeningForGigerVerificationStatusChanges()
    }

    private fun initialize() {
        profileViewModel.userProfileData.observe(viewLifecycleOwner, Observer { profile ->
            var experienceString = ""
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            profile.experiences?.let {
                val experiences = it.sortedByDescending { experience -> experience.startDate }
                for (exp in experiences) {
                    experienceString += exp.title + "\n"
                    experienceString += exp.company + "\n"
                    experienceString += exp.employmentType + "\n"
                    experienceString += exp.location + "\n"

                    if (exp.startDate != null) {
                        experienceString += format.format(exp.startDate!!) + "-"
                        experienceString += if (exp.endDate != null) format.format(exp.endDate!!) + "\n\n"
                        else "current" + "\n\n"
                    }
                }
            }
            experience_card.nextDestination = R.id.editExperienceBottomSheet
            experience_card.cardTitle = getString(R.string.experience)
            experience_card.cardContent = experienceString
            experience_card.cardBottom = getString(R.string.add_experience)

            experience_top_profile.imageName = profile.profileAvatarName
            experience_top_profile.userName = profile.name
        })

        if (cameFromLandingPage)
            profileViewModel.getProfileData()

        when (action) {
            ACTION_OPEN_EDIT_EXPERIENCE_BOTTOM_SHEET -> {
                this.findNavController().navigate(R.id.addExperienceBottomSheet)
            }
        }
    }

    private fun setListeners() {
        experience_card.card_bottom.setOnClickListener {
            findNavController().navigate(R.id.addExperienceBottomSheet)
        }

        experience_top_profile.about_me_verification_layout.setOnClickListener {
            navigate(R.id.gigerVerificationFragment)
        }

        experience_top_profile.back_button.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}