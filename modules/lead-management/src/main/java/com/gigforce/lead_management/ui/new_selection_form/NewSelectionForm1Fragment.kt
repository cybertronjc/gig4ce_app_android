package com.gigforce.lead_management.ui.new_selection_form

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfilesItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningBusinessAndJobProfilesItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.SubmitJoiningRequest
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentNewSelectionForm1Binding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.gigforce.lead_management.ui.LeadManagementSharedViewModelState
import com.gigforce.lead_management.ui.new_selection_form_2.NewSelectionForm2Fragment
import com.gigforce.lead_management.ui.select_business_screen.SelectBusinessFragment
import com.gigforce.lead_management.ui.select_job_profile_screen.SelectJobProfileFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class NewSelectionForm1Fragment : BaseFragment2<FragmentNewSelectionForm1Binding>(
    fragmentName = "NewSelectionForm1Fragment",
    layoutId = R.layout.fragment_new_selection_form1,
    statusBarColor = R.color.lipstick_2
) {

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: NewSelectionForm1ViewModel by viewModels()
    private val leadMgmtSharedViewModel: LeadManagementSharedViewModel by activityViewModels()

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentNewSelectionForm1Binding,
        savedInstanceState: Bundle?
    ) {
        if (viewCreatedForTheFirstTime){
            attachTextWatcher()
        }

        initToolbar(viewBinding)
        initListeners(viewBinding)
        initViewModel()
        initSharedViewModel()
    }

    private fun requestFocusOnMobileNoEditText() = viewBinding.mainForm.apply {

        mobileNoEt.requestFocus()
        val imm: InputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput( InputMethodManager.SHOW_FORCED,0)
    }

    private fun attachTextWatcher()= viewBinding.apply  {
        lifecycleScope.launchWhenCreated {

            mainForm.mobileNoEt.getTextChangeAsStateFlow()
                .collect {
                    viewModel.handleEvent(NewSelectionForm1Events.ContactNoChanged("+91$it"))
                }
        }

        lifecycleScope.launchWhenCreated {

            mainForm.gigerNameEt.getTextChangeAsStateFlow()
                .collect {
                    viewModel.handleEvent(NewSelectionForm1Events.GigerNameChanged(it))
                }
        }

        lifecycleScope.launchWhenCreated {

            mainForm.clientIdEt.getTextChangeAsStateFlow()
                .collect {
                    viewModel.handleEvent(NewSelectionForm1Events.GigerClientIdChanged(it))
                }
        }
    }

    private fun initListeners(
        viewBinding: FragmentNewSelectionForm1Binding
    ) = viewBinding.apply {

        mainForm.selectBusinessLayout.setOnClickListener {
            viewModel.handleEvent(NewSelectionForm1Events.OpenSelectBusinessScreenSelected)
        }

        mainForm.selectJobProfileLayout.setOnClickListener {
            viewModel.handleEvent(NewSelectionForm1Events.OpenSelectJobProfileScreenSelected)
        }

        mainForm.nextButton.setOnClickListener {
            viewModel.handleEvent(NewSelectionForm1Events.SubmitButtonPressed)
        }
    }

    private fun initToolbar(
        viewBinding: FragmentNewSelectionForm1Binding
    ) = viewBinding.toolbar.apply {
        this.setBackButtonListener {
            activity?.onBackPressed()
        }
        setBackButtonDrawable(R.drawable.ic_chevron)
        this.stepsTextView.setText("Step 1/2")
    }

    private fun initViewModel() = viewModel
        .viewState
        .observe(viewLifecycleOwner, {
            val state = it ?: return@observe

            when (state) {
                //Loading initial data states
                NewSelectionForm1ViewState.LoadingBusinessAndJobProfiles -> loadingBusinessAndJobProfiles()
                NewSelectionForm1ViewState.JobProfilesAndBusinessLoadSuccess -> showMainForm()
                is NewSelectionForm1ViewState.ErrorWhileLoadingBusinessAndJobProfiles -> showErrorInLoadingBusinessAndJobProfiles(
                    state.error
                )

                //Validation error states
                is NewSelectionForm1ViewState.ValidationError -> handleValidationError(state)

                //Checking Mobile no in profile states
                NewSelectionForm1ViewState.CheckingForUserDetailsFromProfiles -> {
                    viewBinding.mainForm.gigerNameEt.isEnabled = false
                    viewBinding.mainForm.nameProgressbar.visible()
                    viewBinding.mainForm.gigerNameEt.setText("")

                    viewBinding.mainForm.contactNoErrorTv.text = null
                    viewBinding.mainForm.contactNoErrorTv.gone()
                }
                is NewSelectionForm1ViewState.ErrorWhileCheckingForUserInProfile -> {
                    viewBinding.mainForm.gigerNameEt.isEnabled = true
                    viewBinding.mainForm.gigerNameEt.setText("")
                    viewBinding.mainForm.nameProgressbar.gone()
                }
                is NewSelectionForm1ViewState.UserDetailsFromProfiles -> {
                    viewBinding.mainForm.nameProgressbar.gone()

                    if (state.profile.name.isBlank()) {
                        viewBinding.mainForm.gigerNameEt.isEnabled = true
                        viewBinding.mainForm.gigerNameEt.setText("")
                    } else {
                        viewBinding.mainForm.gigerNameEt.isEnabled = false
                        viewBinding.mainForm.gigerNameEt.setText(state.profile.name)
                    }
                }

                //Open data selection screen states
                is NewSelectionForm1ViewState.OpenSelectedBusinessScreen -> openSelectBusinessScreen(
                    ArrayList(state.business)
                )
                is NewSelectionForm1ViewState.OpenSelectedJobProfileScreen -> openSelectJobProfileScreen(
                    ArrayList(state.jobProfiles)
                )

                //Data submit states
                is NewSelectionForm1ViewState.NavigateToForm2 -> openForm2(
                    state.submitJoiningRequest
                )
            }
        })

    private fun openForm2(
        submitJoiningRequest: SubmitJoiningRequest
    ) {
        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_SELECTION_FORM_2, bundleOf(
                NewSelectionForm2Fragment.INTENT_EXTRA_JOINING_DATA to submitJoiningRequest
            )
        )
    }

    private fun openSelectJobProfileScreen(
        jobProfiles: ArrayList<JobProfilesItem>
    ) {
        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_SELECT_JOB_PROFILE,
            bundleOf(SelectJobProfileFragment.INTENT_EXTRA_JOB_PROFILES to jobProfiles),
            getNavOptions()
        )
    }

    private fun openSelectBusinessScreen(
        business: ArrayList<JoiningBusinessAndJobProfilesItem>
    ) {
        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_SELECT_BUSINESS,
            bundleOf(SelectBusinessFragment.INTENT_EXTRA_BUSINESS_LIST to business),
            getNavOptions()
        )
    }

    private fun handleValidationError(
        errorState: NewSelectionForm1ViewState.ValidationError
    ) = viewBinding.mainForm.apply {

        if (errorState.invalidMobileNoMessage != null) {
            contactNoErrorTv.visible()
            contactNoErrorTv.text = errorState.invalidMobileNoMessage
        } else {
            contactNoErrorTv.text = null
            contactNoErrorTv.gone()
        }

        gigerNameTextInputLayout.error = errorState.gigerNameError
        gigerClientIdTextInputLayout.error = errorState.gigerClientIdError


        if (errorState.businessError != null) {
            businessErrorTv.visible()
            businessErrorTv.text = errorState.businessError
        } else {
            businessErrorTv.text = null
            businessErrorTv.gone()
        }

        if (errorState.jobProfilesError != null) {
            jobProfileErrorTv.visible()
            jobProfileErrorTv.text = errorState.jobProfilesError
        } else {
            jobProfileErrorTv.text = null
            jobProfileErrorTv.gone()
        }
    }

    private fun loadingBusinessAndJobProfiles() = viewBinding.apply {

        mainForm.root.gone()
        formMainInfoLayout.root.gone()
        dataLoadingShimmerContainer.visible()

        startShimmer(
            this.dataLoadingShimmerContainer,
            ShimmerDataModel(
                minHeight = R.dimen.size_120,
                minWidth = LinearLayout.LayoutParams.MATCH_PARENT,
                marginRight = R.dimen.size_16,
                marginTop = R.dimen.size_1,
                orientation = LinearLayout.VERTICAL
            ),
            R.id.shimmer_controller
        )
    }

    private fun showMainForm() = viewBinding.apply {
        stopShimmer(
            dataLoadingShimmerContainer,
            R.id.shimmer_controller
        )
        dataLoadingShimmerContainer.gone()
        formMainInfoLayout.root.gone()

        mainForm.root.visible()

        if (viewCreatedForTheFirstTime){
            Handler().postDelayed({
                requestFocusOnMobileNoEditText()
            },300)
        }
    }

    private fun showErrorInLoadingBusinessAndJobProfiles(
        error: String
    ) = viewBinding.apply {

        mainForm.root.gone()
        stopShimmer(
            dataLoadingShimmerContainer,
            R.id.shimmer_controller
        )
        dataLoadingShimmerContainer.gone()
        formMainInfoLayout.root.visible()

        formMainInfoLayout.infoIv.loadImage(R.drawable.ic_no_joining_found) //todo change this image
        formMainInfoLayout.infoMessageTv.text = error
    }

    private fun initSharedViewModel() = lifecycleScope.launchWhenCreated {
        leadMgmtSharedViewModel
            .viewStateFlow.collect {
//                showToast(it.toString())
//                if (!isAdded) return@collect

                when (it) {
                    is LeadManagementSharedViewModelState.BusinessSelected -> showSelectedBusiness(
                        it.businessSelected
                    )
                    is LeadManagementSharedViewModelState.JobProfileSelected -> showSelectedJobProfile(
                        it.jobProfileSelected
                    )
                }
            }
    }

    private fun showSelectedBusiness(
        businessSelected: JoiningBusinessAndJobProfilesItem
    ) = viewBinding.mainForm.apply {

        businessSelectedLabel.text = businessSelected.name
        viewModel.handleEvent(NewSelectionForm1Events.BusinessSelected(businessSelected))

        //reseting job profile selected
        selectedJobProfileLabel.text = "Click to select Job Profile"

        businessErrorTv.text = null
        businessErrorTv.gone()
    }

    private fun showSelectedJobProfile(
        jobProfileSelected: JobProfilesItem
    ) = viewBinding.mainForm.apply {

        selectedJobProfileLabel.text = jobProfileSelected.name
        viewModel.handleEvent(NewSelectionForm1Events.JobProfileSelected(jobProfileSelected))

        jobProfileErrorTv.text = null
        jobProfileErrorTv.gone()
    }
}