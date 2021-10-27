package com.gigforce.giger_gigs.tl_login_details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.gigforce.common_ui.StringConstants
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.databinding.FragmentTeamLeaderLoginFilterBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TeamLeaderLoginFilterFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var navigation: INavigation
    private lateinit var viewBinding: FragmentTeamLeaderLoginFilterBinding
    var filterText = ""
    var filterDays = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentTeamLeaderLoginFilterBinding.inflate(inflater, container, false)
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

//        removeFilterButton.setOnClickListener {
//            //remove filter
//            filterDays = -1
//            val navController = findNavController()
//            navController.previousBackStackEntry?.savedStateHandle?.set("filterDays", filterDays)
//            navController.popBackStack()
//        }
    }


    companion object {

        fun newInstance() = TeamLeaderLoginFilterFragment()
        const val TAG = "TeamLeaderLoginFilterFragment"
    }
}