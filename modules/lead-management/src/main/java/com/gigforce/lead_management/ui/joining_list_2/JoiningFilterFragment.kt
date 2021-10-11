package com.gigforce.lead_management.ui.joining_list_2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.gigforce.common_ui.StringConstants
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentJoiningFilterBinding
import com.gigforce.lead_management.databinding.FragmentJoiningList2Binding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class JoiningFilterFragment : BottomSheetDialogFragment(){

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: JoiningList2ViewModel by viewModels()
    private lateinit var viewBinding: FragmentJoiningFilterBinding
    var filterText = ""
    var filterDays = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentJoiningFilterBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(savedInstanceState)
        setViews()
        listeners()
    }

    private fun setViews() = viewBinding.apply{
        if (filterDays != -1){
            when(filterDays){

                0 -> { todayRadio.isChecked = true }
                1 -> { yesterdayRadio.isChecked = true }
                3 -> { threeDaysRadio.isChecked = true }
                7 -> { sevenDaysRadio.isChecked = true }
                30 -> { monthRadio.isChecked = true }

            }
        }
    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            filterDays = it.getInt(StringConstants.INTENT_FILTER_DAYS_NUMBER.value) ?: return@let
        }
        arguments?.let {
            filterDays = it.getInt(StringConstants.INTENT_FILTER_DAYS_NUMBER.value) ?: return@let
        }
    }

    private fun listeners() = viewBinding.apply {

        radioGroup.setOnCheckedChangeListener { radioGroup, i ->

            when(i) {

                R.id.todayRadio -> {
                    filterDays = 0
                }
                R.id.yesterdayRadio -> {
                    filterDays = 1
                }
                R.id.threeDaysRadio -> {
                    filterDays = 3
                }
                R.id.sevenDaysRadio -> {
                    filterDays = 7
                }
                R.id.monthRadio -> {
                    filterDays = 30
                }
            }
        }

        applyFilterButton.setOnClickListener {
            //apply filter
            val navController = findNavController()
            navController.previousBackStackEntry?.savedStateHandle?.set("filterDays", filterDays)
            navController.popBackStack()

        }

        removeFilterButton.setOnClickListener {
            //remove filter
            filterDays = -1
            val navController = findNavController()
            navController.previousBackStackEntry?.savedStateHandle?.set("filterDays", filterDays)
            navController.popBackStack()
        }
    }


    companion object {

        fun newInstance() = JoiningFilterFragment()
        const val TAG = "JoiningFilterFragment"
    }
}