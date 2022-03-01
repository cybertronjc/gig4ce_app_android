package com.gigforce.wallet.payouts.payout_list

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.wallet.R
import com.gigforce.wallet.databinding.FragmentPayoutListFilterBinding
import com.gigforce.wallet.payouts.SharedPayoutViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DateFilterForFilterScreen(
    val date: PayoutDateFilter,
    val selected: Boolean
) : Parcelable

@AndroidEntryPoint
class PayoutListFilterFragment : BaseBottomSheetDialogFragment<FragmentPayoutListFilterBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_payout_list_filter
) {
    companion object {
        const val TAG = "PayoutListFilterFragment"
        const val INTENT_EXTRA_DATE_FILTERS = "date_filters"
    }

    private var filters: ArrayList<DateFilterForFilterScreen> = arrayListOf()
    private val viewModel: SharedPayoutViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        getDataFromIntents(savedInstanceState)
    }

    override fun viewCreated(
        viewBinding: FragmentPayoutListFilterBinding,
        savedInstanceState: Bundle?
    ) {
        inflateFilterOptions()
        setViews()
        listeners()
    }

    private fun inflateFilterOptions() = viewBinding.apply {

        for (option in filters) {
            val radioButton = layoutInflater.inflate(
                R.layout.layout_radio_button,
                null,
                false
            ) as RadioButton

            radioButton.id = View.generateViewId()
            radioButton.text = option.date.textForDate
            radioButton.tag = option.date.id
            radioButton.isChecked = option.selected
            radioGroup.addView(radioButton)
        }
    }

    private fun setViews() = viewBinding.apply {


    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            filters = it.getParcelableArrayList(INTENT_EXTRA_DATE_FILTERS) ?: return@let
        }

        arguments?.let {
            filters = it.getParcelableArrayList(INTENT_EXTRA_DATE_FILTERS) ?: return@let
        }
    }

    private fun listeners() = viewBinding.apply {

        applyFilterButton.setOnClickListener {
            val selectedDateIndex = radioGroup.indexOfChild(
                radioGroup.findViewById(radioGroup.checkedRadioButtonId)
            )

            if (selectedDateIndex == -1) {
                return@setOnClickListener
            } else {

                if (selectedDateIndex <= filters.size) {
                    viewModel.filterSelected(filters[selectedDateIndex].date)
                    dismiss()
                }
            }
        }
    }


}