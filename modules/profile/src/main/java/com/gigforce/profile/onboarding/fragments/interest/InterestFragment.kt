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

class InterestFragment(val formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) : Fragment() {

    companion object {
        fun newInstance(formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) = InterestFragment(formCompletionListener)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(InterestViewModel::class.java)
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Driving"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Delivery Executive"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Sales"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Technician"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Helper"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Security"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Tele Calling"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Supervisor"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Cleaner"))
        allInterestList.add(InterestDM(R.drawable.ic_chat, "Farmers"))
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
                                if (allInterestList.get(position).interestName.equals("Delivery Executive")) {
                                    interest_cl.gone()
                                    delivery_executive_detail_cl.visible()
                                }
                            } else {
                                Toast.makeText(context, "Maximum three interest can be selected!!", Toast.LENGTH_LONG).show()
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

    private fun isDeliveryExecutiveSelected():Boolean{
        allInterestList.forEach { obj ->
            if (obj.interestName.equals("Delivery Executive")) {
                return true
            }
        }
        return false
    }

    private fun listener() {
        imageTextCardcl.setOnClickListener {
            setSelected(icon_iv_1, yes_i_have, imageTextCardcl)
            resetSelected(icon_iv1, no_i_dont, imageTextCardcl_)
            experiencedInDeliveryExecutive = true
            validateForm()
        }
        imageTextCardcl_.setOnClickListener {
            resetSelected(icon_iv_1, yes_i_have, imageTextCardcl)
            setSelected(icon_iv1, no_i_dont, imageTextCardcl_)
            experiencedInDeliveryExecutive = false
            validateForm()
        }

        imageTextCardMol.setOnClickListener {
            if (foodSelected) {
                resetSelected(icon, food, imageTextCardMol)
                foodSelected = false
            } else {
                setSelected(icon, food, imageTextCardMol)
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
                grocerySelected = true
            }
            validateForm()
        }

        imageTextCardMolfirst.setOnClickListener{
            if (ecomSelected) {
                resetSelected(icon1f, ecom, imageTextCardMolfirst)
                ecomSelected = false
            } else {
                setSelected(icon1f, ecom, imageTextCardMolfirst)
                ecomSelected = true
            }
        }
        imageTextCardMol3.setOnClickListener{
            if (milkSelected) {
                resetSelected(icon2, milk, imageTextCardMol3)
                milkSelected = false
            } else {
                setSelected(icon2, milk, imageTextCardMol3)
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

    fun validateForm(){
        if(getSelectedInterestCount()>0 ){
            if(isDeliveryExecutiveSelected()){
                if(!experiencedInDeliveryExecutive || (foodSelected || grocerySelected || ecomSelected || milkSelected)){
                    formCompletionListener.formcompleted(true)
                }
                else formCompletionListener.formcompleted(false)
            }
            else formCompletionListener.formcompleted(true)
        }
    }
}