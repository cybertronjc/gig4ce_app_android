package com.gigforce.lead_management.ui.new_selection_form

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.contacts.ContactsDelegate
import com.gigforce.common_ui.contacts.PhoneContact
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.showToast
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class NewSelectionForm1Fragment : BaseFragment2<FragmentNewSelectionForm1Binding>(
    fragmentName = "NewSelectionForm1Fragment",
    layoutId = R.layout.fragment_new_selection_form1,
    statusBarColor = R.color.lipstick_2
) {

    companion object {

        const val TAG = "NewSelectionForm1Fragment"
    }

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: NewSelectionForm1ViewModel by viewModels()
    private val leadMgmtSharedViewModel: LeadManagementSharedViewModel by activityViewModels()

    private val contactsDelegate: ContactsDelegate by lazy {
        ContactsDelegate(requireContext().contentResolver)
    }

    @SuppressLint("NewApi")
    private val pickContactContract = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) {
        if(it == null) return@registerForActivityResult

        contactsDelegate.parseResults(
            uri = it,
            onSuccess = { contacts ->
                showContactNoOnMobileNo(contacts)
            }, onFailure = { exception ->
                logger.e(TAG, "while picking contact", exception)
                showToast(getString(R.string.unable_to_pick_contact))
            })
    }

    private fun showContactNoOnMobileNo(contacts: List<PhoneContact>) {
        if (contacts.isEmpty()) return

        val pickedContact = contacts.first()
        if(pickedContact.phoneNumbers.isEmpty()) return

        if (pickedContact.phoneNumbers.size > 1) {
            //show choose no dialog

            val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setTitle("Select Phone Number")
            builder.setItems(pickedContact.phoneNumbers.toTypedArray()) { _, item ->
                val number = pickedContact.phoneNumbers[item]
                viewBinding.mainForm.mobileNoEt.setText(number)

                viewBinding.mainForm.mobileNoEt.post {
                    viewBinding.mainForm.mobileNoEt.setSelection(viewBinding.mainForm.mobileNoEt.length())
                }
                viewModel.handleEvent(NewSelectionForm1Events.ContactNoChanged("+91" +number))
            }
            builder.show()
        } else {
            viewBinding.mainForm.mobileNoEt.setText(pickedContact.phoneNumbers.first())

            viewBinding.mainForm.mobileNoEt.post {
                viewBinding.mainForm.mobileNoEt.setSelection(viewBinding.mainForm.mobileNoEt.length())
            }
            viewModel.handleEvent(NewSelectionForm1Events.ContactNoChanged("+91" + pickedContact.phoneNumbers.first()))
        }
    }

    @SuppressLint("NewApi")
    private val requestContactPermissionContract = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { contactPermissionGranted ->

        if (contactPermissionGranted) {
            pickContactContract.launch(null)
        } else {
            val hasUserOptedForDoNotAskAgain = requireActivity().shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS).not()
            if(hasUserOptedForDoNotAskAgain){

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.read_contact_permission_required))
                    .setMessage(getString(R.string.please_grant_read_permissions_to))
                    .setPositiveButton(getString(R.string.okay_common_ui)){_,_ -> openSettingsPage() }
                    .setNegativeButton(getString(R.string.cancel_common_ui)) { _, _ ->}
                    .show()
            }
        }
    }

    private fun openSettingsPage() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentNewSelectionForm1Binding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {
            attachTextWatcher()
        }

        initToolbar(viewBinding)
        initListeners(viewBinding)
        initViewModel()
        initSharedViewModel()
    }

    private fun requestFocusOnMobileNoEditText() = viewBinding.mainForm.apply {

        mobileNoEt.requestFocus()
        val imm: InputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun attachTextWatcher() = viewBinding.apply {
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

        mainForm.pickContactsButton.setOnClickListener {

            if (readContactsPermissionsGranted()) {
                pickContactContract.launch(null)
            } else {

                requestContactPermissionContract.launch(
                    Manifest.permission.READ_CONTACTS
                )
            }
        }
    }

    private fun initToolbar(
        viewBinding: FragmentNewSelectionForm1Binding
    ) = viewBinding.toolbar.apply {
        this.setBackButtonListener {
            activity?.onBackPressed()
        }
        setBackButtonDrawable(R.drawable.ic_chevron)
        this.stepsTextView.setText(context.getString(R.string.step_1_2))
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

                    viewBinding.mainForm.contactNoError.errorTextview.text = null
                    viewBinding.mainForm.contactNoError.root.gone()
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

            viewBinding.mainForm.contactNoError.root.visible()
            viewBinding.mainForm.contactNoError.errorTextview.text = errorState.invalidMobileNoMessage
        } else {
            viewBinding.mainForm.contactNoError.errorTextview.text = null
            viewBinding.mainForm.contactNoError.root.gone()
        }

        if (errorState.gigerNameError != null) {
            viewBinding.mainForm.gigerNameError.root.visible()
            viewBinding.mainForm.gigerNameError.errorTextview.text = errorState.gigerNameError
        } else {
            viewBinding.mainForm.gigerNameError.errorTextview.text = null
            viewBinding.mainForm.gigerNameError.root.gone()
        }


        if (errorState.businessError != null) {

            viewBinding.mainForm.businessError.root.visible()
            viewBinding.mainForm.businessError.errorTextview.text = errorState.businessError
        } else {
            viewBinding.mainForm.businessError.errorTextview.text = null
            viewBinding.mainForm.businessError.root.gone()
        }

        if (errorState.jobProfilesError != null) {
            viewBinding.mainForm.jobProfileError.root.visible()
            viewBinding.mainForm.jobProfileError.errorTextview.text = errorState.jobProfilesError
        } else {
            viewBinding.mainForm.jobProfileError.errorTextview.text = null
            viewBinding.mainForm.jobProfileError.root.gone()
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

        if (viewCreatedForTheFirstTime) {
            Handler().postDelayed({
                requestFocusOnMobileNoEditText()
            }, 300)
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

        formMainInfoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
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
        businessSelectedLabel.typeface = Typeface.DEFAULT_BOLD
        viewModel.handleEvent(NewSelectionForm1Events.BusinessSelected(businessSelected))

        //reseting job profile selected
        selectedJobProfileLabel.text = getString(R.string.click_to_select_job_profile)
        selectedJobProfileLabel.typeface = Typeface.DEFAULT

        viewBinding.mainForm.businessError.errorTextview.text = null
        viewBinding.mainForm.businessError.root.gone()
    }

    private fun showSelectedJobProfile(
        jobProfileSelected: JobProfilesItem
    ) = viewBinding.mainForm.apply {

        selectedJobProfileLabel.text = jobProfileSelected.name
        selectedJobProfileLabel.typeface = Typeface.DEFAULT_BOLD
        viewModel.handleEvent(NewSelectionForm1Events.JobProfileSelected(jobProfileSelected))

        viewBinding.mainForm.jobProfileError.errorTextview.text = null
        viewBinding.mainForm.jobProfileError.root.gone()
    }

    private fun readContactsPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
    }
}