package com.gigforce.lead_management.ui.select_job_profile_screen

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.gigforce.common_ui.components.cells.SearchTextChangeListener
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.common_ui.navigation.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentSelectJobProfileBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SelectJobProfileFragment : BaseFragment2<FragmentSelectJobProfileBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_select_job_profile,
    statusBarColor = R.color.lipstick_2
), JobProfileAdapter.OnJobProfileSelectedListener {

    companion object {
        private const val TAG = "SelectJobProfileFragment"
        const val INTENT_EXTRA_SELECTED_BUSINESS = "selected_business"
        const val INTENT_EXTRA_JOB_PROFILES = "job_profiles"
    }

    @Inject
    lateinit var navigation: INavigation
    private val sharedViewModel: LeadManagementSharedViewModel by activityViewModels()
    private var jobProfiles: ArrayList<JobProfilesItem> = arrayListOf()
    private lateinit var selectedBusiness: JoiningBusinessAndJobProfilesItem

    private val glide: RequestManager by lazy {
        Glide.with(requireContext())
    }

    private val jobProfileAdapter: JobProfileAdapter by lazy {
        JobProfileAdapter(requireContext(), glide).apply {
            setOnJobProfileSelectedListener(this@SelectJobProfileFragment)
        }
    }

    override fun viewCreated(
        viewBinding: FragmentSelectJobProfileBinding,
        savedInstanceState: Bundle?
    ) {
        getDataFrom(
            arguments,
            savedInstanceState
        )
        initListeners()
        setDataOnView()
    }

    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {

        arguments?.let {
            selectedBusiness = it.getParcelable(INTENT_EXTRA_SELECTED_BUSINESS) ?: return@let
            jobProfiles = it.getParcelableArrayList(INTENT_EXTRA_JOB_PROFILES) ?: return@let
        }

        savedInstanceState?.let {
            selectedBusiness = it.getParcelable(INTENT_EXTRA_SELECTED_BUSINESS) ?: return@let
            jobProfiles = it.getParcelableArrayList(INTENT_EXTRA_JOB_PROFILES) ?: return@let
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(
            INTENT_EXTRA_JOB_PROFILES,
            jobProfiles
        )
        outState.putParcelable(
            INTENT_EXTRA_SELECTED_BUSINESS,
            selectedBusiness
        )
    }


    private fun initListeners() = viewBinding.apply {
        toolbar.apply {
            titleText.text = getString(R.string.select_job_profile1_lead)
            setBackButtonListener {
                navigation.popBackStack()
            }
            setBackButtonDrawable(R.drawable.ic_chevron)
            searchTextChangeListener = object : SearchTextChangeListener {

                override fun onSearchTextChanged(text: String) {
                    jobProfileAdapter.filter.filter(text)
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = jobProfileAdapter

        okayButton.setOnClickListener {
            val selectedJobProfile = jobProfileAdapter.getSelectedBusiness() ?: return@setOnClickListener
            sharedViewModel.jobProfileSelected(selectedBusiness,selectedJobProfile)

            navigation.popBackStack(
                LeadManagementNavDestinations.FRAGMENT_SELECTION_FORM_1,
                false
            )
        }
    }

    private fun setDataOnView() = viewBinding.apply {
        if (jobProfiles.isEmpty()) {
            this.infoLayout.root.visible()
            this.infoLayout.infoMessageTv.text = getString(R.string.no_job_profile_to_show_lead)
            this.infoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        } else {
            this.infoLayout.root.gone()
            jobProfileAdapter.setData(jobProfiles)
        }

        okayButton.isEnabled = jobProfiles.find { it.selected } != null
    }

    override fun onJobProfileSelected(jobProfileSelected: JobProfilesItem) {
        viewBinding.okayButton.isEnabled = true
    }

    override fun onJobProfileFiltered(
        jobProfileCountVisibleAfterFiltering: Int,
        selectedJobProfileVisible: Boolean
    ) {
        if(jobProfileCountVisibleAfterFiltering != 0){
            viewBinding.infoLayout.root.gone()
            viewBinding.okayButton.isEnabled = selectedJobProfileVisible
        } else{
            viewBinding.okayButton.isEnabled = false
            viewBinding.infoLayout.root.visible()
            viewBinding.infoLayout.infoMessageTv.text = getString(R.string.no_job_profile_to_show_lead)
            viewBinding.infoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        }
    }
}