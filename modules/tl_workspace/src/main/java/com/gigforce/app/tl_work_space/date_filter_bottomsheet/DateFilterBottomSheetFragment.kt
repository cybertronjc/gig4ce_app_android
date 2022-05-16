package com.gigforce.app.tl_work_space.date_filter_bottomsheet

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.BotttomsheetDateFilterBinding
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.toastfix.toastcompatwrapper.ToastHandler
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.ZoneOffset

@AndroidEntryPoint
class DateFilterBottomSheetFragment : BaseBottomSheetDialogFragment<BotttomsheetDateFilterBinding>(
    fragmentName = TAG,
    layoutId = R.layout.botttomsheet_date_filter
) {

    companion object {
        const val TAG = "JoiningFilterFragment"
    }

    private lateinit var filterOptions: List<TLWorkSpaceFilterOption>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getDataFromIntents(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun viewCreated(
        viewBinding: BotttomsheetDateFilterBinding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {
            setFilterOptions()
            listeners()
        }
    }

    @SuppressLint("InflateParams")
    private fun setFilterOptions() = viewBinding.radioGroup.apply {

        for (option in filterOptions) {
            val radioButton = layoutInflater.inflate(
                R.layout.common_radio_button,
                null,
                false
            ) as RadioButton

            radioButton.id = View.generateViewId()
            radioButton.text = option.text
            radioButton.tag = option.filterId
            this.addView(radioButton)
        }
    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            filterOptions = it.getParcelableArrayList(
                TLWorkSpaceNavigation.INTENT_EXTRA_DATE_FILTER_OPTIONS
            ) ?: return@let
        }

        arguments?.let {
            filterOptions = it.getParcelableArrayList(
                TLWorkSpaceNavigation.INTENT_EXTRA_DATE_FILTER_OPTIONS
            ) ?: return@let
        }
    }

    private fun listeners() = viewBinding.apply {

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            applyFilterButton.isEnabled = true
        }

        applyFilterButton.setOnClickListener {
            checkForCustomDateRangeElsePublishResults()
        }
    }

    private fun checkForCustomDateRangeElsePublishResults() {
        if (viewBinding.radioGroup.checkedRadioButtonId == -1) {

            ToastHandler.showToast(
                requireContext(),
                "Select an option",
                Toast.LENGTH_SHORT
            )
            return
        }

        val filterOptionSelectedId = viewBinding.radioGroup.findViewById<RadioButton>(
            viewBinding.radioGroup.checkedRadioButtonId
        ).tag.toString()

        val optionSelected = filterOptions.find {
            filterOptionSelectedId == it.filterId
        } ?: return

        if (optionSelected.customDateOrRangeFilter) {
            openDateFilter(optionSelected)
        } else {
            publishFilterResults(optionSelected)
        }
    }

    private fun openDateFilter(
        filterOption: TLWorkSpaceFilterOption,
    ) {
        if (filterOption.selectRangeInFilter) {

            openSelectDateRangeSelectionDialog(
                filterOption.filterId,
                filterOption.defaultSelectedDate ?: LocalDate.now(),
                filterOption.minimumDateAvailableForSelection,
                filterOption.maximumDateAvailableForSelection
            )
        } else {

            openSingleDateSelectionDialog(
                filterOption.filterId,
                filterOption.defaultSelectedDate ?: LocalDate.now(),
                filterOption.minimumDateAvailableForSelection,
                filterOption.maximumDateAvailableForSelection
            )
        }
    }

    private fun openSingleDateSelectionDialog(
        filterId: String,
        defaultDate: LocalDate,
        minDate: LocalDate?,
        maxDate: LocalDate?
    ) {
        DatePickerDialog(
            requireContext(),
            R.style.DatePickerTheme,
            { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->

                val date = LocalDate.of(
                    year,
                    month + 1,
                    dayOfMonth
                )
                publishCustomFilter(
                    filterId,
                    date,
                    date
                )
            },
            defaultDate.year,
            defaultDate.monthValue - 1,
            defaultDate.dayOfMonth
        ).apply {

            if (minDate != null) {
                this.datePicker.minDate = minDate
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            }

            if (maxDate != null) {
                this.datePicker.maxDate = maxDate
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            }

            this.show()
        }
    }

    private fun openSelectDateRangeSelectionDialog(
        filterId: String,
        defaultDate: LocalDate,
        minDate: LocalDate?,
        maxDate: LocalDate?
    ) {

    }

    private fun publishCustomFilter(
        filterId: String,
        startDate: LocalDate,
        endDate: LocalDate?
    ) {
        val optionSelected = filterOptions.find {
            filterId == it.filterId
        } ?: return

        optionSelected.apply {
            this.startDate = startDate
            this.endDate = endDate
        }
        publishFilterResults(optionSelected)
    }

    private fun publishFilterResults(optionSelected: TLWorkSpaceFilterOption) {
        setFragmentResult(
            TLWorkSpaceNavigation.FRAGMENT_RESULT_KEY_DATE_FILTER,
            bundleOf(
                TLWorkSpaceNavigation.INTENT_EXTRA_SELECTED_DATE_FILTER to optionSelected
            )
        )
        dismiss()
    }
}