package com.gigforce.app.modules.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.landingscreen.LandingPageConstants
import com.gigforce.app.modules.landingscreen.LandingPageConstants.INTENT_EXTRA_CAME_FROM_LANDING_SCREEN
import kotlinx.android.synthetic.main.fragment_profile_experience_expanded.*
import kotlinx.android.synthetic.main.fragment_profile_experience_expanded.view.*
import kotlinx.android.synthetic.main.profile_card_background.view.*
import java.text.SimpleDateFormat
import java.util.*

class ExperienceExpandedFragment: ProfileBaseFragment() {

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
    private var action : Int  = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        arguments?.let {
            cameFromLandingPage = it.getBoolean(INTENT_EXTRA_CAME_FROM_LANDING_SCREEN)
            action = it.getInt(LandingPageConstants.INTENT_EXTRA_ACTION)
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
    }

    private fun initialize() {
        profileViewModel.userProfileData.observe(viewLifecycleOwner, Observer { profile ->
            var experienceString = ""
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            profile.experiences?.let {
                val experiences = it.sortedByDescending { experience -> experience.startDate  }
                for (exp in experiences) {
                    experienceString += exp.title + "\n"
                    experienceString += exp.company + "\n"
                    experienceString += exp.employmentType + "\n"
                    experienceString += exp.location + "\n"
                    experienceString += format.format(exp.startDate!!) + "-"
                    experienceString += if(exp.endDate != null) format.format(exp.endDate!!) + "\n\n"
                    else "current" + "\n\n"
                }
            }
            experience_card.nextDestination = R.id.editExperienceBottomSheet
            experience_card.cardTitle = "Experience"
            experience_card.cardContent = experienceString
            experience_card.cardBottom = "Add experiences"

            experience_top_profile.imageName = profile.profileAvatarName
            experience_top_profile.userName = profile.name
        })

        if(cameFromLandingPage)
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
    }


}