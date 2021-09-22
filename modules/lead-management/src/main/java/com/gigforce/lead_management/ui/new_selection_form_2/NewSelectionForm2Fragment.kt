package com.gigforce.lead_management.ui.new_selection_form_2

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.DatePicker
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toLocalDate
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentNewSelectionForm2Binding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.gigforce.lead_management.ui.LeadManagementSharedViewModelState
import com.gigforce.lead_management.ui.select_city.SelectCityFragment
import com.gigforce.lead_management.ui.select_reporting_location.SelectReportingLocationFragment
import com.gigforce.lead_management.ui.select_tls.SelectClientTlFragment
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class NewSelectionForm2Fragment : BaseFragment2<FragmentNewSelectionForm2Binding>(
    fragmentName = "NewSelectionForm1Fragment",
    layoutId = R.layout.fragment_new_selection_form2,
    statusBarColor = R.color.lipstick_2
) {

    companion object {

        const val INTENT_EXTRA_JOINING_DATA = "joining_data"
    }

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: NewSelectionForm2ViewModel by viewModels()
    private val leadMgmtSharedViewModel: LeadManagementSharedViewModel by activityViewModels()

    //Data from previous screen
    private lateinit var joiningRequest: SubmitJoiningRequest

    private val expectedStartDatePicker: DatePickerDialog by lazy {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { datePicker: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val newCal = Calendar.getInstance()
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, month)
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                viewBinding.mainForm.selectedDateLabel.text =
                    DateHelper.getDateInDDMMYYYY(newCal.time)
                viewModel.handleEvent(NewSelectionForm2Events.DateOfJoiningSelected(newCal.time.toLocalDate()))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = Date().time
        datePickerDialog
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            joiningRequest = it.getParcelable(INTENT_EXTRA_JOINING_DATA) ?: return@let
        }

        savedInstanceState?.let {
            joiningRequest = it.getParcelable(INTENT_EXTRA_JOINING_DATA) ?: return@let
        }

        viewModel.handleEvent(
            NewSelectionForm2Events.JoiningDataReceivedFromPreviousScreen(
                joiningRequest
            )
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(
            INTENT_EXTRA_JOINING_DATA,
            joiningRequest
        )
    }

    override fun viewCreated(
        viewBinding: FragmentNewSelectionForm2Binding,
        savedInstanceState: Bundle?
    ) {
        initToolbar(viewBinding)
        initListeners(viewBinding)
        initViewModel()
        initSharedViewModel()
    }

    private fun initListeners(
        viewBinding: FragmentNewSelectionForm2Binding
    ) = viewBinding.mainForm.apply {


        selectCityLayout.setOnClickListener {
            viewModel.handleEvent(NewSelectionForm2Events.SelectCityClicked)
        }

        selectReportingLocationLayout.setOnClickListener {
            viewModel.handleEvent(
                NewSelectionForm2Events.SelectReportingLocationClicked(
                    shouldShowLocationsStateWise = this.stateWiseCheckbox.isChecked
                )
            )
        }

        selectClientTlLayout.setOnClickListener {
            viewModel.handleEvent(NewSelectionForm2Events.SelectClientTLClicked)
        }


        changeDateBtn.setOnClickListener {
            expectedStartDatePicker.show()
        }

        bindProgressButton(nextButton)
        nextButton.attachTextChangeAnimator()
        nextButton.setOnClickListener {
            viewModel.handleEvent(NewSelectionForm2Events.SubmitButtonPressed(
                checkChipsSelectedAndNotifyViewModel()
            ))
        }
    }

    private fun checkChipsSelectedAndNotifyViewModel() : MutableList<ShiftTimingItem> {

        val shifts = mutableListOf<ShiftTimingItem>()
        viewBinding.mainForm.apply {

            shiftChipGroup.checkedChipIds.forEach {
                val shiftChip = shiftChipGroup.findViewById<Chip>(it)
                shifts.add(
                    ShiftTimingItem(
                        id = shiftChip.tag.toString(),
                        name = shiftChip.text.toString()
                    )
                )
            }
        }

        return shifts
    }

    private fun initToolbar(
        viewBinding: FragmentNewSelectionForm2Binding
    ) = viewBinding.toolbar.apply {

        this.setBackButtonListener {
            navigation.navigateUp()
        }
    }

    private fun initViewModel() = viewModel
        .viewState
        .observe(viewLifecycleOwner, {
            val state = it ?: return@observe

            when (state) {
                //Loading initial data states
                NewSelectionForm2ViewState.LoadingLocationAndTLData -> loadingBusinessAndJobProfiles()
                is NewSelectionForm2ViewState.LocationAndTlDataLoaded -> showMainForm(state.shiftAndTls)
                is NewSelectionForm2ViewState.ErrorWhileLoadingLocationAndTlData -> showErrorInLoadingBusinessAndJobProfiles(
                    state.error
                )

                //Validation error states
                is NewSelectionForm2ViewState.ValidationError -> handleValidationError(state)

                //Open data selection screen states
                is NewSelectionForm2ViewState.OpenSelectCityScreen -> openSelectCityScreen(
                    ArrayList(state.cities)
                )
                is NewSelectionForm2ViewState.OpenSelectReportingScreen -> openSelectReportingLocationScreen(
                    ArrayList(state.reportingLocations)
                )
                is NewSelectionForm2ViewState.OpenSelectClientTlScreen -> openSelectBusinessTlScreen(
                    ArrayList(state.tls)
                )

                is NewSelectionForm2ViewState.ErrorWhileSubmittingJoiningData -> {
                  viewBinding.mainForm.nextButton.hideProgress("Next")

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Unable to submit joining request")
                        .setMessage(state.error)
                        .setPositiveButton("Okay"){_,_ ->}
                        .show()
                }
                NewSelectionForm2ViewState.JoiningDataSubmitted -> {
                    showToast("data submitted")
                }
                NewSelectionForm2ViewState.SubmittingJoiningData -> {

                    viewBinding.mainForm.nextButton.showProgress {
                        buttonText = "Submitting..."
                        progressColor = Color.WHITE
                    }
                }
            }
        })

    private fun openSelectReportingLocationScreen(
        reportingLocations: ArrayList<ReportingLocationsItem>
    ) {
        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_SELECT_REPORTING_LOCATION,
            bundleOf(SelectReportingLocationFragment.INTENT_EXTRA_REPORTING_LOCATIONS to reportingLocations)
        )
    }

    private fun openSelectBusinessTlScreen(
        businessTls: ArrayList<BusinessTeamLeadersItem>
    ) {
        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_SELECT_CLIENT_TL,
            bundleOf(SelectClientTlFragment.INTENT_EXTRA_CLIENT_TLS to businessTls)
        )
    }

    private fun openSelectCityScreen(
        cities: ArrayList<ReportingLocationsItem>
    ) {
        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_SELECT_CITY,
            bundleOf(SelectCityFragment.INTENT_EXTRA_CITY_LIST to cities)
        )
    }

    private fun handleValidationError(
        errorState: NewSelectionForm2ViewState.ValidationError
    ) = viewBinding.mainForm.apply {

        if (errorState.assignGigsFromError != null) {
            this.expectedDateErrorTv.visible()
            this.expectedDateErrorTv.text = errorState.assignGigsFromError
        } else {
            this.expectedDateErrorTv.gone()
            this.expectedDateErrorTv.text = null
        }

        if (errorState.cityError != null) {
            this.cityErrorTv.visible()
            this.cityErrorTv.text = errorState.cityError
        } else {
            this.cityErrorTv.gone()
            this.cityErrorTv.text = null
        }

        if (errorState.reportingLocationError != null) {
            this.reportingLocationErrorTv.visible()
            this.reportingLocationErrorTv.text = errorState.reportingLocationError
        } else {
            this.reportingLocationErrorTv.gone()
            this.reportingLocationErrorTv.text = null
        }

        if (errorState.shiftsError != null) {
            this.shiftErrorTv.visible()
            this.shiftErrorTv.text = errorState.shiftsError
        } else {
            this.shiftErrorTv.gone()
            this.shiftErrorTv.text = null
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

    private fun showMainForm(
        shiftAndTls: JoiningLocationTeamLeadersShifts
    ) = viewBinding.apply {
        stopShimmer(
            dataLoadingShimmerContainer,
            R.id.shimmer_controller
        )
        dataLoadingShimmerContainer.gone()
        formMainInfoLayout.root.gone()

        mainForm.root.visible()

        //Populating TL Chips
        mainForm.shiftChipGroup.removeAllViews()
        shiftAndTls.shiftTiming.forEach {

            val chip: Chip = layoutInflater.inflate(
                R.layout.shift_chip,
                mainForm.shiftChipGroup,
                false
            ) as Chip
            chip.text = it.name
            chip.tag = it.id
            chip.id = ViewCompat.generateViewId()
            mainForm.shiftChipGroup.addView(chip)
        }

        mainForm.shiftChipGroup.isSelectionRequired = true
        mainForm.shiftChipGroup.isSingleSelection = false
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

    private fun initSharedViewModel() {
        leadMgmtSharedViewModel
            .viewState
            .observe(viewLifecycleOwner, {

                when (it) {
                    is LeadManagementSharedViewModelState.CitySelected -> showSelectedCity(it.city)
                    is LeadManagementSharedViewModelState.ClientTLSelected -> showSelectedTL(it.tlSelected)
                    is LeadManagementSharedViewModelState.ReportingLocationSelected -> showSelectedReportingLocation(
                        it.reportingLocation
                    )
                }
            })

        lifecycleScope.launchWhenCreated {

            leadMgmtSharedViewModel
                .viewStateFlow
                .collect {
                    when (it) {
                        is LeadManagementSharedViewModelState.CitySelected -> showSelectedCity(it.city)
                        is LeadManagementSharedViewModelState.ClientTLSelected -> showSelectedTL(it.tlSelected)
                        is LeadManagementSharedViewModelState.ReportingLocationSelected -> showSelectedReportingLocation(
                            it.reportingLocation
                        )
                    }
                }
        }

    }

    private fun showSelectedCity(
        citySelected: ReportingLocationsItem
    ) = viewBinding.apply {
        mainForm.citySelectedLabel.text = citySelected.name
        viewModel.handleEvent(NewSelectionForm2Events.CitySelected(citySelected))
    }

    private fun showSelectedReportingLocation(
        reportingLocationSelected: ReportingLocationsItem
    ) = viewBinding.mainForm.apply {
        reportingLocationSelectedLabel.text = reportingLocationSelected.name
        viewModel.handleEvent(
            NewSelectionForm2Events.ReportingLocationSelected(
                reportingLocationSelected
            )
        )
    }

    private fun showSelectedTL(
        tlSelected: BusinessTeamLeadersItem
    ) = viewBinding.mainForm.apply {
        selectedClientTlLabel.text = tlSelected.name
        viewModel.handleEvent(NewSelectionForm2Events.ClientTLSelected(tlSelected))
    }
}