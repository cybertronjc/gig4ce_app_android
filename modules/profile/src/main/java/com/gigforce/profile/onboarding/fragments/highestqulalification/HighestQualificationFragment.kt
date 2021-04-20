package com.gigforce.profile.onboarding.fragments.highestqulalification

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.gigforce.profile.R
import com.gigforce.profile.onboarding.OnboardingFragmentNew
import kotlinx.android.synthetic.main.highest_qualification_item.*

class HighestQualificationFragment(val formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) : Fragment(),OnboardingFragmentNew.FragmentSetLastStateListener,OnboardingFragmentNew.FragmentInteractionListener {

    companion object {
        fun newInstance(formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) = HighestQualificationFragment(formCompletionListener)
    }

    private lateinit var viewModel: HighestQualificationViewModel
    var selectedHighestQualification = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.highest_qualification_item, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HighestQualificationViewModel::class.java)
        listener()
    }

    private fun listener() {
        imageTextCardMol.setOnClickListener {
            resetAll()
            setSelected(icon, option, imageTextCardMol)
            selectedHighestQualification = "<10th"
            validateForm()
        }
        imageTextCardMol4.setOnClickListener{
            resetAll()
            setSelected(icon1, option1, imageTextCardMol4)
            selectedHighestQualification = "10th pass"
            validateForm()
        }
        imageTextCardMol3.setOnClickListener {
            resetAll()
            setSelected(icon2, option2, imageTextCardMol3)
            selectedHighestQualification = "12th pass"
            validateForm()
        }
        imageTextCardMol_.setOnClickListener{
            resetAll()
            setSelected(icon_, option_, imageTextCardMol_)
            selectedHighestQualification = "Diploma"
            validateForm()
        }
        imageTextCardMol4_.setOnClickListener{
            resetAll()
            setSelected(icon1_, option1_, imageTextCardMol4_)
            selectedHighestQualification = "Graduated"
            validateForm()
        }
        imageTextCardMol3_.setOnClickListener{
            resetAll()
            setSelected(icon2_, option2_, imageTextCardMol3_)
            selectedHighestQualification = "Post Graduated"
            validateForm()
        }
    }

    private fun validateForm() {
        if(!selectedHighestQualification.equals("")){
            formCompletionListener.enableDisableNextButton(true)
        }
        else{
            formCompletionListener.enableDisableNextButton(false)
        }
    }

    private fun resetAll() {
        resetSelected(icon, option, imageTextCardMol)
        resetSelected(icon1,option1, imageTextCardMol4)
        resetSelected(icon2, option2, imageTextCardMol3)
        resetSelected(icon_, option_, imageTextCardMol_ )
        resetSelected(icon1_, option1_, imageTextCardMol4_)
        resetSelected(icon2_, option2_, imageTextCardMol3_)

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
        return false
    }

    override fun activeNextButton() {
        validateForm()
    }

}