package com.gigforce.profile.onboarding.fragments.interest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.profile.R
import com.gigforce.profile.onboarding.OnboardingFragmentNew
import kotlinx.android.synthetic.main.image_text_item_view.view.*
import kotlinx.android.synthetic.main.interest_fragment.*

class InterestFragment(val formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) :
    Fragment(), OnboardingFragmentNew.FragmentInteractionListener,OnboardingFragmentNew.FragmentSetLastStateListener {

    companion object {
        fun newInstance(formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) =
            InterestFragment(formCompletionListener)
    }

    private lateinit var viewModel: InterestViewModel
    private var allInterestList = ArrayList<InterestDM>()
    var experiencedInDeliveryExecutive = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.interest_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(InterestViewModel::class.java)
        allInterestList.add(InterestDM(R.drawable.ic_driving_wheel, "Driving"))
        allInterestList.add(InterestDM(R.drawable.ic_delivery_truck, "Delivery Executive"))
        allInterestList.add(InterestDM(R.drawable.ic_sale, "Sales"))
        allInterestList.add(InterestDM(R.drawable.ic_technician, "Technician"))
        allInterestList.add(InterestDM(R.drawable.ic_trolley, "Helper"))
        allInterestList.add(InterestDM(R.drawable.ic_security, "Security"))
        allInterestList.add(InterestDM(R.drawable.ic_tele_caller, "Tele Calling"))
        allInterestList.add(InterestDM(R.drawable.ic_supervisor, "Supervisor"))
        allInterestList.add(InterestDM(R.drawable.ic_cleaning, "Cleaner"))
        allInterestList.add(InterestDM(R.drawable.ic_plant_in_hand, "Farmers"))
        listener()
        context?.let {
            all_interests_rv.layoutManager = GridLayoutManager(
                activity, 3,
                GridLayoutManager.VERTICAL, false
            )
            all_interests_rv.adapter = AllInterestAdapter(
                it,
                allInterestList,
                object : AllInterestAdapter.OnDeliveryExecutiveClickListener {
                    override fun onclick(view: View, position: Int) {
                        var foundSelected = false
                        if (allInterestList.get(position).selected) {
                            resetSelected(view.icon_iv, view.interest_name, view)
                            allInterestList.get(position).selected = false
                            foundSelected = true
                        }

                        if (getSelectedInterestCount() < 3) {
                            if (!foundSelected) {
                                setSelected(view.icon_iv, view.interest_name, view)
                                allInterestList.get(position).selected = true
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Maximum three interest can be selected!!",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        if (getSelectedInterestCount() > 0) {
                            formCompletionListener.enableDisableNextButton(true)
                        } else {
                            formCompletionListener.enableDisableNextButton(false)
                        }
                    }
                })

        }
    }

    private fun getSelectedInterestCount(): Int {
        var count = 0
        allInterestList.forEach { obj ->
            if (obj.selected) {
                count++
            }
        }
        return count
    }

    private fun isDeliveryExecutiveSelected(): Boolean {
        allInterestList.forEach { obj ->
            if (obj.interestName.equals("Delivery Executive")) {
                return true
            }
        }
        return false
    }

    var clickedOnExperiencedOptions = false

    private fun listener() {
        imageTextCardcl.setOnClickListener {
            setSelected(icon_iv_1, yes_i_have, imageTextCardcl)
            resetSelected(icon_iv1, no_i_dont, imageTextCardcl_)
            experiencedInDeliveryExecutive = true
            clickedOnExperiencedOptions = true
            experienced_in.visible()
            formCompletionListener.enableDisableNextButton(false)
            validateForm()
        }
        imageTextCardcl_.setOnClickListener {
            resetSelected(icon_iv_1, yes_i_have, imageTextCardcl)
            setSelected(icon_iv1, no_i_dont, imageTextCardcl_)
            experiencedInDeliveryExecutive = false
            clickedOnExperiencedOptions = true
            experienced_in.gone()
            formCompletionListener.enableDisableNextButton(true)
            validateForm()
        }

        imageTextCardMol.setOnClickListener {
            if (foodSelected) {
                resetSelected(icon, food, imageTextCardMol)
                foodSelected = false
            } else {
                setSelected(icon, food, imageTextCardMol)
                formCompletionListener.enableDisableNextButton(true)
                foodSelected = true
            }
            validateForm()
        }

        imageTextCardMol4.setOnClickListener {
            if (grocerySelected) {
                resetSelected(icon1, grocery, imageTextCardMol4)
                grocerySelected = false
            } else {
                setSelected(icon1, grocery, imageTextCardMol4)
                formCompletionListener.enableDisableNextButton(true)
                grocerySelected = true
            }
            validateForm()
        }

        imageTextCardMolfirst.setOnClickListener {
            if (ecomSelected) {
                resetSelected(icon1f, ecom, imageTextCardMolfirst)
                ecomSelected = false
            } else {
                setSelected(icon1f, ecom, imageTextCardMolfirst)
                formCompletionListener.enableDisableNextButton(true)
                ecomSelected = true
            }
        }
        imageTextCardMol3.setOnClickListener {
            if (milkSelected) {
                resetSelected(icon2, milk, imageTextCardMol3)
                milkSelected = false
            } else {
                setSelected(icon2, milk, imageTextCardMol3)
                formCompletionListener.enableDisableNextButton(true)
                milkSelected = true
            }
            validateForm()
        }

    }

    var foodSelected = false
    var grocerySelected = false
    var ecomSelected = false
    var milkSelected = false


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

    fun getselectedInterest(): ArrayList<String> {
        var selectedInterests = ArrayList<String>()
        allInterestList.forEach { interest ->
            if (interest.selected) {
                selectedInterests.add(interest.interestName)
            }
        }
        return selectedInterests
    }

    fun validateForm() {
        if (getSelectedInterestCount() > 0) {
            if (isDeliveryExecutiveSelected()) {
                if (!experiencedInDeliveryExecutive || (foodSelected || grocerySelected || ecomSelected || milkSelected)) {
                    formCompletionListener.enableDisableNextButton(true)
                } else formCompletionListener.enableDisableNextButton(false)
            } else formCompletionListener.enableDisableNextButton(true)
        }
    }

    var currentStep = 0

    override fun nextButtonActionFound(): Boolean {
        when (currentStep) {
            0 -> getselectedInterest().forEach {
                if (it.equals("Delivery Executive")) {
                    interest_cl.gone()
                    delivery_executive_detail_cl.visible()
                    formCompletionListener.enableDisableNextButton(false)
                    currentStep = 1
                    return true
                }
            }
            else -> return false
        }
        return false
    }

    override fun lastStateFormFound() : Boolean {
        formCompletionListener.enableDisableNextButton(true)
        if(currentStep == 1){
            interest_cl.visible()
            delivery_executive_detail_cl.gone()
            currentStep = 0
            return true
        }
        else return false
    }
}