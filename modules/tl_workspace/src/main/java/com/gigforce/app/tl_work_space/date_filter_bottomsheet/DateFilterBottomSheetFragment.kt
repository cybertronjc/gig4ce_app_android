package com.gigforce.app.tl_work_space.date_filter_bottomsheet

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.util.Pair
import androidx.fragment.app.setFragmentResult
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceDateFilterOption
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.BotttomsheetDateFilterBinding
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.resources.MaterialAttributes.resolveOrThrow
import com.toastfix.toastcompatwrapper.ToastHandler
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@AndroidEntryPoint
class DateFilterBottomSheetFragment : BaseBottomSheetDialogFragment<BotttomsheetDateFilterBinding>(
    fragmentName = TAG,
    layoutId = R.layout.botttomsheet_date_filter
) {

    companion object {
        const val TAG = "JoiningFilterFragment"
    }

    private lateinit var dateFilterOptions: List<TLWorkSpaceDateFilterOption>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getDataFromIntents(savedInstanceState)
    }

    override fun viewCreated(
        viewBinding: BotttomsheetDateFilterBinding,
        savedInstanceState: Bundle?
    ) {
        val bottomSheet = viewBinding.root.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)

        setFilterOptions()
        listeners()
    }

    @SuppressLint("InflateParams")
    private fun setFilterOptions() = viewBinding.radioGroup.apply {
        removeAllViews()

        for (option in dateFilterOptions) {
            val radioButton = layoutInflater.inflate(
                R.layout.common_radio_button,
                null,
                false
            ) as RadioButton

            radioButton.id = View.generateViewId()
            radioButton.text = option.text
            radioButton.tag = option.filterId
            radioButton.isChecked = option.selected
            this.addView(radioButton)
        }
    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            dateFilterOptions = it.getParcelableArrayList(
                TLWorkSpaceNavigation.INTENT_EXTRA_DATE_FILTER_OPTIONS
            ) ?: return@let
        }

        arguments?.let {
            dateFilterOptions = it.getParcelableArrayList(
                TLWorkSpaceNavigation.INTENT_EXTRA_DATE_FILTER_OPTIONS
            ) ?: return@let
        }
    }

    private fun listeners() = viewBinding.apply {

        radioGroup.setOnCheckedChangeListener { _, _ ->
            applyFilterButton.isEnabled = true
        }

        applyFilterButton.setOnClickListener {
            checkForCustomDateRangeElsePublishResults()
        }

        cancelButton.setOnClickListener {
            dismiss()
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

        val optionSelected = dateFilterOptions.find {
            filterOptionSelectedId == it.filterId
        } ?: return

        if (optionSelected.customDateOrRangeFilter) {
            openDateFilter(optionSelected)
        } else {
            publishFilterResults(optionSelected)
        }
    }

    private fun openDateFilter(
        dateFilterOption: TLWorkSpaceDateFilterOption,
    ) {
        if (dateFilterOption.selectRangeInFilter) {

            openSelectDateRangeSelectionDialog(
                dateFilterOption
            )
        } else {

            openSingleDateSelectionDialog(
                dateFilterOption.filterId,
                dateFilterOption.defaultSelectedDate ?: LocalDate.now(),
                dateFilterOption.minimumDateAvailableForSelection,
                dateFilterOption.maximumDateAvailableForSelection
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

    @SuppressLint("RestrictedApi")
    private fun openSelectDateRangeSelectionDialog(
        dateFilter: TLWorkSpaceDateFilterOption
    ) = dateFilter.apply {

        val dateRangePickerBuilder = MaterialDatePicker.Builder.dateRangePicker()
        val dateRangeValidator = if (maxDaysDifferenceInCaseOfRange > 0) {
            DateRangeValidator(maxDaysDifferenceInCaseOfRange)
        } else {
            null
        }

        val constraints = CalendarConstraints.Builder().apply {

            if (minimumDateAvailableForSelection != null) {
                this.setStart(
                    minimumDateAvailableForSelection!!.atStartOfDay().toInstant(
                        ZoneOffset.UTC
                    ).toEpochMilli()
                )
            }

            if (maximumDateAvailableForSelection != null) {
                this.setEnd(
                    maximumDateAvailableForSelection!!.atStartOfDay().toInstant(
                        ZoneOffset.UTC
                    ).toEpochMilli()
                )
            }

            if (dateRangeValidator != null) {
                setValidator(dateRangeValidator)
            }

            if (defaultSelectedDate != null) {
                this.setOpenAt(
                    defaultSelectedDate!!.atStartOfDay().toInstant(
                        ZoneOffset.UTC
                    ).toEpochMilli()
                )
            }
        }.build()

        dateRangePickerBuilder
            .setCalendarConstraints(constraints)
            .setTheme(
                resolveOrThrow(requireContext(), R.attr.materialCalendarTheme, "")
            ).build().apply {
                dateRangeValidator?.setMaterialDatePicker(this)
                addOnPositiveButtonClickListener {

                    if (it.first != null && it.second != null) {
                        handleDateRangeSelected(
                            filterId,
                            it
                        )
                    }
                }
            }.show(childFragmentManager, "date_range_picker")

    }

    private fun handleDateRangeSelected(
        filterId: String,
        it: Pair<Long, Long>
    ) {
        val startDate = Instant
            .ofEpochMilli(it.first!!)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val endDate = Instant
            .ofEpochMilli(it.second!!)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        publishCustomFilter(
            filterId,
            startDate,
            endDate
        )
    }

    private fun publishCustomFilter(
        filterId: String,
        startDate: LocalDate,
        endDate: LocalDate?
    ) {
        val optionSelected = dateFilterOptions.find {
            filterId == it.filterId
        } ?: return

        optionSelected.apply {
            this.startDate = startDate
            this.endDate = endDate
        }
        publishFilterResults(optionSelected)
    }

    private fun publishFilterResults(optionSelectedDate: TLWorkSpaceDateFilterOption) {
        setFragmentResult(
            TLWorkSpaceNavigation.FRAGMENT_RESULT_KEY_DATE_FILTER,
            bundleOf(
                TLWorkSpaceNavigation.INTENT_EXTRA_SELECTED_DATE_FILTER to optionSelectedDate
            )
        )
        dismiss()
    }
}