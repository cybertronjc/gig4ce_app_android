package com.gigforce.app.tl_work_space.drop_giger

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.DatePicker
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.android_common_utils.extensions.vibrate
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.TLWorkSpaceSharedViewModel
import com.gigforce.app.tl_work_space.databinding.FragmentDropGigerBinding
import com.gigforce.app.tl_work_space.drop_giger.models.DropOption
import com.gigforce.app.tl_work_space.retentions.RetentionFragmentViewEvents
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DropGigerFragment : BaseFragment2<FragmentDropGigerBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_drop_giger,
    statusBarColor = R.color.lipstick_2
) {
    companion object {
        const val TAG = "DropGigerFragment"
    }

    @Inject
    lateinit var tlWorkSpaceNavigation: TLWorkSpaceNavigation
    private val viewModel: DropGigerViewModel by viewModels()
    private val sharedViewModel: TLWorkSpaceSharedViewModel by activityViewModels()

    private val dateFormatter = DateTimeFormatter.ofPattern(
        "dd/MMM/yy",
        Locale.getDefault()
    )

    private val datePicker: DatePickerDialog by lazy {

        val defaultDate = LocalDate.now()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.DatePickerTheme,
            { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val date = LocalDate.of(
                    year,
                    month + 1,
                    dayOfMonth
                )

                viewBinding.mainLayout.selectedDateLabel.text = date.format(
                    dateFormatter
                )
                viewModel.setEvent(
                    DropGigerFragmentViewEvents.LastWorkingDateSelected(
                        date
                    )
                )
            },
            defaultDate.year,
            defaultDate.monthValue - 1,
            defaultDate.dayOfMonth
        )

        datePickerDialog
    }


    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {


            val gigerId = it.getString(TLWorkSpaceNavigation.INTENT_EXTRA_GIGER_ID) ?: return@let
            val jobProfileId =
                it.getString(TLWorkSpaceNavigation.INTENT_EXTRA_JOB_PROFILE_ID) ?: return@let

            viewModel.setKeysReceivedFromPreviousScreen(
                gigerId,
                jobProfileId
            )
        }

        viewModel.setSharedViewModel(sharedViewModel)
        setFragmentListenerForDateFilterSelection()
    }

    private fun setFragmentListenerForDateFilterSelection() {

        setFragmentResultListener(
            "drop_success",
            listener = { requestKey: String, bundle: Bundle ->

                findNavController().navigateUp()
                findNavController().navigateUp()
            }
        )
    }


    override fun viewCreated(
        viewBinding: FragmentDropGigerBinding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {

            initView()
            observeViewStates()
            observeViewEffects()
        }
    }


    private fun initView() = viewBinding.apply {


        appBar.apply {
            setBackButtonListener {

                findNavController().navigateUp()
            }
            changeBackButtonDrawable()
        }


        this.mainLayout.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
        }

        bindProgressButton(mainLayout.nextButton)
        mainLayout.nextButton.attachTextChangeAnimator()

        mainLayout.nextButton.setOnClickListener {
            viewModel.setEvent(
                DropGigerFragmentViewEvents.DropButtonClicked
            )
        }
        mainLayout.changeDateBtn.setOnClickListener {
            datePicker.show()
        }

        viewBinding.infoLayout.infoIv.loadImage(
            R.drawable.ic_dragon_sleeping_animation
        )

        lifecycleScope.launchWhenCreated {

            viewBinding.mainLayout.customReasonEt.getTextChangeAsStateFlow()
                .debounce(300)
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collect {
                    viewModel.handleEvent(
                        DropGigerFragmentViewEvents.CustomReasonEntered(it)
                    )
                }
        }
    }

    private fun observeViewStates() = lifecycleScope.launchWhenCreated {

        viewModel.uiState
            .collect {

                when (it) {
                    is DropGigerFragmentUiState.DroppingGiger -> {
                        showDroppingProgressView()
                    }
                    is DropGigerFragmentUiState.ErrorWhileDroppingGiger -> {
                        hideDroppingProgressView()
                        showAlertDialog(it.error)
                    }
                    is DropGigerFragmentUiState.ErrorWhileLoadingDropOptions -> handleErrorInLoadingData(
                        it.error
                    )
                    is DropGigerFragmentUiState.GigerDroppedWithSuccess -> {

                        requireContext().vibrate()
                        hideDroppingProgressView()

                        tlWorkSpaceNavigation.openDropSuccessBottomSheet()
                    }
                    is DropGigerFragmentUiState.LoadingDropOptionsData -> handleLoadingState()
                    is DropGigerFragmentUiState.ShowOptionsData -> handleDataLoadedState(
                        it.options
                    )
                }
            }
    }

    private fun showAlertDialog(error: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alert")
            .setMessage("Unable to drop giger, $error")
            .setPositiveButton("Okay") { _, _ ->

            }.show()
    }

    private fun hideDroppingProgressView() {
        viewBinding.mainLayout.nextButton.hideProgress("Drop Selection")
        enableSubmitButton()
    }

    private fun showDroppingProgressView() {
        viewBinding.mainLayout.nextButton.showProgress {
            buttonText = "Dropping.."
            progressColor = Color.WHITE
        }
        disableSubmitButton()
    }

    private fun handleErrorInLoadingData(
        error: String
    ) = viewBinding.apply {

        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()

        infoLayout.root.visible()
        infoLayout.infoMessageTv.text = error
    }

    private fun handleDataLoadedState(
        selectedDateDateFilter: List<DropOption>
    ) = viewBinding.apply {

        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()

        this.infoLayout.root.gone()
        this.mainLayout.recyclerView.collection = selectedDateDateFilter
        showOrHideNoDataLayout(
            selectedDateDateFilter.isNotEmpty()
        )
    }

    private fun showOrHideNoDataLayout(
        dataAvailableToShowOnScreen: Boolean
    ) = viewBinding.infoLayout.apply {

        if (dataAvailableToShowOnScreen) {
            root.gone()
            infoMessageTv.text = null
        } else {
            this.root.visible()
            this.infoMessageTv.text = "No Reasons from server"
        }
    }

    private fun handleLoadingState(
    ) = viewBinding.apply {
        this.infoLayout.root.gone()

        startShimmer(
            this.shimmerContainer as LinearLayout,
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

    private fun observeViewEffects() = lifecycleScope.launchWhenCreated {

        viewModel
            .effect
            .collect {

                when (it) {
                    DropGigerFragmentViewUiEffects.DisableSubmitButton -> disableSubmitButton()
                    DropGigerFragmentViewUiEffects.EnableSubmitButton -> enableSubmitButton()
                    DropGigerFragmentViewUiEffects.HideCustomReasonLayout -> viewBinding.mainLayout.customReasonLayout.gone()
                    DropGigerFragmentViewUiEffects.ShowCustomReasonLayout -> viewBinding.mainLayout.customReasonLayout.visible()
                }
            }
    }

    private fun disableSubmitButton() {
        viewBinding.mainLayout.nextButton.isEnabled = false
    }

    private fun enableSubmitButton() {
        viewBinding.mainLayout.nextButton.isEnabled = true
    }
}