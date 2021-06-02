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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.profile.R
import com.gigforce.profile.analytics.OnboardingEvents
import com.gigforce.profile.models.SkillsDetails
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
        private var skillDetailsList = ArrayList<SkillsDetails>()


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

        //getting skills from DB
        viewModel.getSkillsList().observe(viewLifecycleOwner, Observer {
            allInterestList = it
            Log.d("list", allInterestList.toString())
            setUpRecyclerView(it)
        })

        listener()
    }

    private fun setUpSkillDetailsRV(skillsDetails: List<SkillsDetails>){
        context?.let {
            skill_details_rv.layoutManager = GridLayoutManager(
                activity, 4,
                GridLayoutManager.VERTICAL, false
            )
            skill_details_rv.adapter = SkillDetailsAdapter(
                it,
                skillsDetails,
                object : SkillDetailsAdapter.OnDeliveryExecutiveClickListener {
                    override fun onclick(view: View, position: Int) {
                        var foundSelected = false
                        if (skillDetailsList.get(position).selected) {
                            resetSelected(view.icon_iv, view.interest_name, view)
                            skillDetailsList.get(position).selected = false
                            foundSelected = true
                        }
                        else{
                            setSelected(view.icon_iv, view.interest_name, view)
                            skillDetailsList.get(position).selected = true
                        }
                        validateForm()
                    }
                }
            )

//            all_interests_rv.addItemDecoration(
//                SpaceItemDecoration(
//                    resources.getDimensionPixelSize(R.dimen.size1)
//                )
//            )
            context?.let {
                val itemDecoration =
                    MiddleDividerItemDecoration(it, MiddleDividerItemDecoration.ALL)
                itemDecoration.setDividerColor(ContextCompat.getColor(it, R.color.light_blue_cards))
                skill_details_rv.addItemDecoration(itemDecoration)
//                all_interests_rv.addItemDecoration(SeparatorDecoration(it, Color.GREEN,1f))
            }
        }
    }

    private fun setUpRecyclerView(allInterestList: ArrayList<InterestDM>) {
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
                        if (allInterestList.get(position)?.selected == true) {
                            resetSelected(view.icon_iv, view.interest_name, view)
                            allInterestList.get(position)?.selected = false
                            foundSelected = true
                        }

                        if (getSelectedInterestCount() < 3) {
                            if (!foundSelected) {
                                setSelected(view.icon_iv, view.interest_name, view)
                                allInterestList.get(position)?.selected = true
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
                }
            )

//            all_interests_rv.addItemDecoration(
//                SpaceItemDecoration(
//                    resources.getDimensionPixelSize(R.dimen.size1)
//                )
//            )
            context?.let {
                val itemDecoration =
                    MiddleDividerItemDecoration(it, MiddleDividerItemDecoration.ALL)
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

    private fun isSkillDetailsFound(iDM: InterestDM): Boolean{
            if (iDM.skillDetails?.size != 0){
                return true
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
    }


    fun getDeliveryExecutiveExperiences(): ArrayList<String> {
        var experiences = ArrayList<String>()
        skillDetailsList.forEach {
            if (it.selected){
                experiences.add(it.name)
            }
        }
        return experiences
    }

    private fun resetSelected(icon: ImageView, option: TextView, view: View) {
        context?.let {
            icon.setColorFilter(ContextCompat.getColor(it, R.color.default_color))
            option.setTextColor(ContextCompat.getColor(it, R.color.default_color))
            view.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                            it,
                            R.drawable.rect_light_blue_border
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
            if (interest?.selected == true) {
                var skillModelData = SkillModel(id = interest.skill,skillDetail = getSkillDetailData(interest.skill))
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

    private fun getSelectedSkillDetails(): ArrayList<String>{
        var list = ArrayList<String>()
        for (i in 0..skillDetailsList.size - 1){
            if (skillDetailsList.get(i).selected){
                list.add(skillDetailsList.get(i).name)
            }
        }
        return list
    }

    fun hasSkillDetails(): Boolean{
        for (i in 0..allInterestList.size - 1){
            if(allInterestList.get(i).skillDetails != null){
                return true
            }
        }
        return false
    }

    fun validateForm() {
        if (getSelectedInterestCount() > 0) {
            if (hasSkillDetails()) {
                if (!experiencedInDeliveryExecutive || getSelectedSkillDetails().size > 0) {
                    formCompletionListener?.enableDisableNextButton(true)
                } else formCompletionListener?.enableDisableNextButton(false)
            } else formCompletionListener?.enableDisableNextButton(true)
        }
    }

    var currentStep = 0
    var DELIVERY_EXECUTIVE = "Delivery Executive"
    override fun nextButtonActionFound(): Boolean {
        when (currentStep) {
            0 -> allInterestList.forEach {
                Log.d("test flow","first")
                if (it.selected &&  isSkillDetailsFound(it)) {
                    Log.d("test flow","second")
                    if (it.skillDetails != null){
                        interest_cl.gone()
                        delivery_executive_detail_cl.visible()
                        setUpSkillDetailsRV(it.skillDetails!!)
                        skillDetailsList.clear()
                        skillDetailsList.addAll(it.skillDetails!!)
                        formCompletionListener?.enableDisableNextButton(false)
                        currentStep = 1
                        return true
                    }

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

    fun getSelectedInterestsForAnalytics(): List<String> {
        var skills = ArrayList<String>()
        allInterestList.forEach { interest ->
            if (interest.selected) {
                skills.add(interest.skill)
            }
        }
        return skills.filter {
            it.isNotBlank()
        }
    }


    private fun getExperiencedIn(): List<String> {
        var map = mutableListOf<String>()
        skillDetailsList.forEach {
            if (it.selected){
                map.add(it.name)
            }
        }
        return map.filter {
            it.isNotBlank()
        }
    }

    private fun setMainInterestTracker() {
        val selectedIntrest = getSelectedInterestsForAnalytics()

        var map = mapOf("interests" to selectedIntrest)

        Log.d("interestMap", map.toString())
        eventTracker.pushEvent(TrackingEventArgs(OnboardingEvents.EVENT_USER_UPDATED_INTREST, map))
        eventTracker.removeUserProperty("DeliveryExperience")
        eventTracker.removeUserProperty("ExperienceIn")
        eventTracker.setUserProperty(map)

        if(selectedIntrest.isNotEmpty())
        eventTracker.setProfileProperty(ProfilePropArgs("Interests", selectedIntrest))
    }

    fun setDeliveryExecutiveInterestTracker() {
        var map = mutableMapOf<String,Any>(
                "DeliveryExperience" to (clickedOnExperiencedOptions && !experiencedInDeliveryExecutive),
        )
        getSelectedInterestsForAnalytics().apply {
            if(isNotEmpty()) map.put("interests", this)
        }
        getExperiencedIn().apply {
            if(isNotEmpty()) map.put("ExperienceIn", this)
        }

        Log.d("interestDel", map.toString())
        eventTracker.pushEvent(TrackingEventArgs(OnboardingEvents.EVENT_USER_UPDATED_INTREST, map))
        eventTracker.setUserProperty(map)
        eventTracker.setProfileProperty(ProfilePropArgs("Interests", map))
    }

    override fun activeNextButton() {
        when (currentStep) {
            0 -> if (getSelectedInterestCount() > 0) formCompletionListener?.enableDisableNextButton(true)
            1 -> if ((clickedOnExperiencedOptions && !experiencedInDeliveryExecutive) || (getSelectedSkillDetails().size > 0)) {
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