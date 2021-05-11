package com.gigforce.profile.onboarding.fragments.interest

import android.os.Bundle
import android.util.Log
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
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.profile.R
import com.gigforce.profile.analytics.OnboardingEvents
import com.gigforce.profile.onboarding.MiddleDividerItemDecoration
import com.gigforce.profile.onboarding.OnFragmentFormCompletionListener
import com.gigforce.profile.onboarding.OnboardingFragmentNew
import com.gigforce.profile.onboarding.models.SkillDetailModel
import com.gigforce.profile.onboarding.models.SkillModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.image_text_item_view.view.*
import kotlinx.android.synthetic.main.interest_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class InterestFragment() :
        Fragment(), OnboardingFragmentNew.FragmentInteractionListener, OnboardingFragmentNew.FragmentSetLastStateListener, OnboardingFragmentNew.SetInterfaceListener {

    companion object {
        fun newInstance() =
                InterestFragment()

        private var allInterestList = ArrayList<InterestDM>()

    }

    @Inject
    lateinit var eventTracker: IEventTracker

    private lateinit var viewModel: InterestViewModel
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
        allInterestList.clear()
        allInterestList.add(InterestDM(R.drawable.ic_driving_wheel, "Driving"))
        allInterestList.add(InterestDM(R.drawable.ic_delivery_truck, DELIVERY_EXECUTIVE))
        allInterestList.add(InterestDM(R.drawable.ic_sale, "Sales"))
        allInterestList.add(InterestDM(R.drawable.ic_technician, "Technician"))
        allInterestList.add(InterestDM(R.drawable.ic_trolley, "Helper"))
        allInterestList.add(InterestDM(R.drawable.ic_security, "Security"))
        allInterestList.add(InterestDM(R.drawable.ic_technician, "Tele Calling"))
        allInterestList.add(InterestDM(R.drawable.ic_supervisor, "Supervisor"))
        allInterestList.add(InterestDM(R.drawable.ic_cleaning, "Cleaner"))
        allInterestList.add(InterestDM(R.drawable.ic_plant_in_hand, "Farmers"))
        listener()
        context?.let {
            all_interests_rv.layoutManager = GridLayoutManager(
                    activity, 4,
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
                                        Toast.LENGTH_SHORT
                                ).show()
                            }

                            if (getSelectedInterestCount() > 0) {
                                formCompletionListener?.enableDisableNextButton(true)
                            } else {
                                formCompletionListener?.enableDisableNextButton(false)
                            }
                        }
                    })

//            all_interests_rv.addItemDecoration(
//                SpaceItemDecoration(
//                    resources.getDimensionPixelSize(R.dimen.size1)
//                )
//            )
            context?.let {
                val itemDecoration = MiddleDividerItemDecoration(it, MiddleDividerItemDecoration.ALL)
                itemDecoration.setDividerColor(ContextCompat.getColor(it, R.color.light_blue_cards))
                all_interests_rv.addItemDecoration(itemDecoration)
//                all_interests_rv.addItemDecoration(SeparatorDecoration(it, Color.GREEN,1f))
            }

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
            if (obj.interestName.equals(DELIVERY_EXECUTIVE)) {
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
            formCompletionListener?.enableDisableNextButton(false)
            validateForm()
        }
        imageTextCardcl_.setOnClickListener {
            resetSelected(icon_iv_1, yes_i_have, imageTextCardcl)
            setSelected(icon_iv1, no_i_dont, imageTextCardcl_)
            experiencedInDeliveryExecutive = false
            clickedOnExperiencedOptions = true
            experienced_in.gone()
            formCompletionListener?.enableDisableNextButton(true)
            validateForm()
        }

        imageTextCardMol.setOnClickListener {
            if (foodSelected) {
                resetSelected(icon, food, imageTextCardMol)
                foodSelected = false
            } else {
                setSelected(icon, food, imageTextCardMol)
                formCompletionListener?.enableDisableNextButton(true)
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
                formCompletionListener?.enableDisableNextButton(true)
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
                formCompletionListener?.enableDisableNextButton(true)
                ecomSelected = true
            }
        }
        imageTextCardMol3.setOnClickListener {
            if (milkSelected) {
                resetSelected(icon2, milk, imageTextCardMol3)
                milkSelected = false
            } else {
                setSelected(icon2, milk, imageTextCardMol3)
                formCompletionListener?.enableDisableNextButton(true)
                milkSelected = true
            }
            validateForm()
        }

    }

    var foodSelected = false
    var grocerySelected = false
    var ecomSelected = false
    var milkSelected = false

    fun getDeliveryExecutiveExperiences(): ArrayList<String> {
        var experiences = ArrayList<String>()
        if (foodSelected) experiences.add("Food")
        if (grocerySelected) experiences.add("Grocery")
        if (ecomSelected) experiences.add("Ecom")
        if (milkSelected) experiences.add("Milk")
        return experiences
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

    fun getselectedInterest(): ArrayList<SkillModel> {
        var skills = ArrayList<SkillModel>()
//        var selectedInterests = ArrayList<String>()
        allInterestList.forEach { interest ->
            if (interest.selected) {
                var skillModelData = SkillModel(id = interest.interestName,skillDetail = getSkillDetailData(interest.interestName))
                skills.add(skillModelData)
            }
        }
        return skills
    }

    private fun getSkillDetailData(interestName: String): SkillDetailModel? {
        if(interestName.equals(DELIVERY_EXECUTIVE)){
            return SkillDetailModel(hasExperience = experiencedInDeliveryExecutive,experiencedIn = getDeliveryExecutiveExperiences())
        }
        return null
    }

    fun validateForm() {
        if (getSelectedInterestCount() > 0) {
            if (isDeliveryExecutiveSelected()) {
                if (!experiencedInDeliveryExecutive || (foodSelected || grocerySelected || ecomSelected || milkSelected)) {
                    formCompletionListener?.enableDisableNextButton(true)
                } else formCompletionListener?.enableDisableNextButton(false)
            } else formCompletionListener?.enableDisableNextButton(true)
        }
    }

    var currentStep = 0
    var DELIVERY_EXECUTIVE = "Delivery Executive"
    override fun nextButtonActionFound(): Boolean {
        when (currentStep) {
            0 -> getselectedInterest().forEach {
                Log.d("test flow","first")
                if (it.id.equals(DELIVERY_EXECUTIVE)) {
                    Log.d("test flow","second")

                    interest_cl.gone()
                    delivery_executive_detail_cl.visible()
                    formCompletionListener?.enableDisableNextButton(false)
                    currentStep = 1
                    return true
                }
            }
            else -> {
                setDeliveryExecutiveInterestTracker()
                Log.d("test flow","third")
                return false
            }
        }
        setMainInterestTracker()
        return false
    }

    fun getSelectedInterestsForAnalytics(): ArrayList<String> {
        var skills = ArrayList<String>()
        allInterestList.forEach { interest ->
            if (interest.selected) {
                skills.add(interest.interestName)
            }
        }
        return skills
    }


    private fun setMainInterestTracker() {
        var map = mapOf("interests" to getSelectedInterestsForAnalytics())
        Log.d("interestMap", map.toString())
        eventTracker.pushEvent(TrackingEventArgs(OnboardingEvents.EVENT_USER_UPDATED_INTREST, map))
        eventTracker.removeUserProperty("DeliveryExperience")
        eventTracker.removeUserProperty("ExperienceIn")
        eventTracker.setUserProperty(map)
        eventTracker.setProfileProperty(ProfilePropArgs("Interests", getSelectedInterestsForAnalytics()))
    }

    fun setDeliveryExecutiveInterestTracker() {
        var map = mapOf("interests" to getSelectedInterestsForAnalytics(), "DeliveryExperience" to (clickedOnExperiencedOptions && !experiencedInDeliveryExecutive), "ExperienceIn" to mapOf("Food" to foodSelected, "Grocery" to grocerySelected, "Ecom" to ecomSelected, "Milk" to milkSelected))
        Log.d("interestDel", map.toString())
        eventTracker.pushEvent(TrackingEventArgs(OnboardingEvents.EVENT_USER_UPDATED_INTREST, map))
        eventTracker.setUserProperty(map)
        eventTracker.setProfileProperty(ProfilePropArgs("Interests", map))
    }

    override fun activeNextButton() {
        when (currentStep) {
            0 -> if (getSelectedInterestCount() > 0) formCompletionListener?.enableDisableNextButton(true)
            1 -> if ((clickedOnExperiencedOptions && !experiencedInDeliveryExecutive) || (foodSelected || grocerySelected || ecomSelected || milkSelected)) {
                formCompletionListener?.enableDisableNextButton(true)
            } else formCompletionListener?.enableDisableNextButton(false)
        }
    }

    override fun lastStateFormFound(): Boolean {
        formCompletionListener?.enableDisableNextButton(true)
        if (currentStep == 1) {
            interest_cl.visible()
            delivery_executive_detail_cl.gone()
            currentStep = 0
            return true
        } else return false
    }
    var formCompletionListener: OnFragmentFormCompletionListener? = null
    override fun setInterface(onFragmentFormCompletionListener: OnFragmentFormCompletionListener) {
        formCompletionListener = formCompletionListener?:onFragmentFormCompletionListener
    }

}