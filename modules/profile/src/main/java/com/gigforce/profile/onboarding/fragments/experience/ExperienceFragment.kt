package com.gigforce.profile.onboarding.fragments.experience

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.profile.R
import com.gigforce.profile.analytics.OnboardingEvents
import com.gigforce.profile.onboarding.OnboardingFragmentNew
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.experience_item.*
import javax.inject.Inject

@AndroidEntryPoint
class ExperienceFragment(val formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) :
    Fragment() ,OnboardingFragmentNew.FragmentSetLastStateListener,OnboardingFragmentNew.FragmentInteractionListener{

    companion object {
        fun newInstance(formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) =
            ExperienceFragment(formCompletionListener)
    }

    @Inject lateinit var eventTracker: IEventTracker
    private lateinit var viewModel: ExperienceViewModel
    var workStatus = ""
    var istotalExperienceSelected = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.experience_item, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ExperienceViewModel::class.java)
        listener()
    }

    private fun listener() {
        imageTextCardMol.setOnClickListener(View.OnClickListener {
            resetAll()
            setSelected(icon, option, imageTextCardMol)
            workStatus = "Working"
            validateForm()
        })
        imageTextCardMol3.setOnClickListener(View.OnClickListener {
            resetAll()
            setSelected(icon2, option2, imageTextCardMol3)
            workStatus = "Not Working"
            validateForm()
        })

        total_experience_rg.setOnCheckedChangeListener { group, checkedId ->
            istotalExperienceSelected = true
            //view?.findViewById<View>(group.checkedRadioButtonId)?.background = resources.getDrawable(R.drawable.option_selection_border)
            validateForm()
        }

        
    }

    private fun validateForm() {
        if (!workStatus.equals("") && istotalExperienceSelected) {
            formCompletionListener.enableDisableNextButton(true)
        } else {
            formCompletionListener.enableDisableNextButton(false)
        }
    }

    private fun resetAll() {
        resetSelected(icon, option, imageTextCardMol)
        resetSelected(icon2, option2, imageTextCardMol3)

    }

    private fun resetSelected(icon: ImageView, option: TextView, view: View) {
        context?.let {
            icon.setColorFilter(ContextCompat.getColor(it, R.color.default_color))
            option.setTextColor(ContextCompat.getColor(it, R.color.default_color))
            view.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    it,
                    R.drawable.option_default_border
                )
            )
        }
    }

    private fun setSelected(icon: ImageView, option: TextView, view: View) {
        context?.let {
            icon.setColorFilter(ContextCompat.getColor(it, R.color.selected_image_color))
            option.setTextColor(ContextCompat.getColor(it, R.color.selected_text_color))
            view.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    it,
                    R.drawable.option_selection_border
                )
            )
        }

    }

    override fun lastStateFormFound(): Boolean {
        formCompletionListener.enableDisableNextButton(true)
        return false
    }

    override fun nextButtonActionFound(): Boolean {
        val radioButton = total_experience_rg.findViewById( total_experience_rg.checkedRadioButtonId) as RadioButton
        var totalExperience = radioButton.text.toString()
        var map = mapOf("WorkingStatus" to workStatus, "TotalExperience" to totalExperience)
        eventTracker.pushEvent(TrackingEventArgs(OnboardingEvents.EVENT_USER_UPDATED_EXPERIENCE,map))
        eventTracker.pushEvent(TrackingEventArgs(OnboardingEvents.EVENT_ONBOARDING_PROGRESS, map))
        eventTracker.setUserProperty(map)
        return false
    }

    override fun activeNextButton() {
        validateForm()
    }

}