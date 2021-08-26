package com.gigforce.user_preferences.daytime.weekday

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.user_preferences.R
import com.gigforce.common_ui.viewmodels.userpreferences.SharedPreferenceViewModel
import com.gigforce.user_preferences.daytime.OnSlotClickListener
import com.gigforce.user_preferences.daytime.SlotsRecyclerAdapter
import com.gigforce.core.datamodels.user_preferences.PreferencesDataModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_settings_slots.view.*
import kotlinx.android.synthetic.main.week_day_fragment.*
import java.util.*
import kotlin.collections.ArrayList


class WeekDayFragment : Fragment() {

    companion object {
        fun newInstance() = WeekDayFragment()
    }

    private lateinit var viewModel: SharedPreferenceViewModel
    private lateinit var viewDataModel: PreferencesDataModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.week_day_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SharedPreferenceViewModel::class.java)
        initializeViews()
        listener()
        observePreferenceData()
    }

    private fun observePreferenceData() {
        viewModel.preferenceDataModel.observe(viewLifecycleOwner, Observer { preferenceData ->
            viewModel.setPreferenceDataModel(preferenceData)
            initializeViews()
        })

        viewModel.configLiveDataModel.observe(
            viewLifecycleOwner,
            Observer { configDataModel1 ->
                viewModel.setConfiguration(configDataModel1)
                initializeViews()
            })
        viewModel.getConfiguration()
    }

    private fun initializeViews() {
        viewDataModel = viewModel.getPreferenceDataModel()
        switch3.isChecked = viewDataModel.isweekdaysenabled
        textView62.text = getArrayToString(viewDataModel.selecteddays)
        var selectedStrForSubtitle = getArrayToString(
            viewModel.getSelectedSlotsToShow(
                viewDataModel.selectedslots
            )
        )
        textView66.text = selectedStrForSubtitle
        context?.let {
            if (viewDataModel.isweekdaysenabled) {
                textView61.setTextColor(ContextCompat.getColor(it, R.color.black))
                textView65.setTextColor(ContextCompat.getColor(it, R.color.black))
            } else {
                textView61.setTextColor(ContextCompat.getColor(it, R.color.gray_color))
                textView65.setTextColor(ContextCompat.getColor(it, R.color.gray_color))
            }
        }

    }

    private fun getArrayToString(selectedStrings: ArrayList<String>): String {
        if (selectedStrings == null || selectedStrings.size == 0) return "None"
        var selectedStr = Arrays.toString(selectedStrings.toTypedArray())
        selectedStr = selectedStr.substring(1, selectedStr.length - 1)
        if (selectedStr.contains("All"))
            return "All"
        return selectedStr
    }

    private fun listener() {
        rl_days.setOnClickListener(View.OnClickListener {
            showDaysAlert()
        })
        rl_slots.setOnClickListener(View.OnClickListener {
            showSlotsAlert()
        })
        switch3.setOnClickListener { view ->
            var isChecked = (view as Switch).isChecked
            if (isChecked) {
                if (ifWeekdaysNotSelected()) {
                    showDaysAlert()
                } else if (ifSlotsNotSelected()) {
                    showSlotsAlert()
                } else
                    viewModel.setIsWeekdays(isChecked)

            } else
                viewModel.setIsWeekdays(isChecked)
        }
        imageView16.setOnClickListener(View.OnClickListener { activity?.onBackPressed() })
    }

    private fun ifSlotsNotSelected(): Boolean {
        if (getArrayToString(viewDataModel.selectedslots).equals("None")) {
            return true
        }
        return false
    }

    private fun ifWeekdaysNotSelected(): Boolean {
        if (getArrayToString(viewDataModel.selecteddays).equals("None")) {
            return true
        }
        return false

    }

    fun showDaysAlert() {
        val items = arrayOf(getString(R.string.all_pref), getString(R.string.monday_pref), getString(
                    R.string.tuesday_pref), getString(R.string.wednesday_pref), getString(R.string.thursday_pref), getString(
                                R.string.friday_pref))
        val indexItem = arrayOf(0, 1, 2, 3, 4, 5)
        var isSectionSelected = BooleanArray(items.size)
        var selectedList = ArrayList<Int>()
        for (i in 0..items.size - 1) {
            var isfound = false
            for (day in viewDataModel.selecteddays) {
                if (items[i].equals(day)) {
                    isSectionSelected[i] = true
                    isfound = true
                    selectedList.add(i)
                    break
                }
            }
            if (!isfound) {
                isSectionSelected[i] = false
            }
        }


        val builder = MaterialAlertDialogBuilder(requireContext())
        val view = layoutInflater.inflate(R.layout.fragment_settings_slots, null)
        view.dialogTitleTV.text = getString(R.string.days_capital_pref)
        builder.setView(view)

        val rv: RecyclerView = view.findViewById(R.id.slotsRV)
        rv.layoutManager = LinearLayoutManager(requireContext())

        val slotsRecyclerAdapter = SlotsRecyclerAdapter()
        rv.adapter = slotsRecyclerAdapter


        builder.setPositiveButton(getString(R.string.done_pref)) { dialogInterface, i ->
            val selectedStrings = ArrayList<String>()
            for (j in selectedList.indices) {
                selectedStrings.add(items[selectedList[j]])
            }
            var selectedStr = getArrayToString(selectedStrings)
            viewModel.setWorkingDays(selectedStrings)
            if (!selectedStr.equals("None")) {
                if (ifSlotsNotSelected()) {
                    showSlotsAlert()
                } else {
                    viewModel.setIsWeekdays(true)
                }
            } else {
                viewModel.setIsWeekdays(false)
            }

            textView62.text = selectedStr
        }
        builder.setOnDismissListener { dialog -> initializeViews() }
        builder.show()

        slotsRecyclerAdapter.updateSlots(items, isSectionSelected)

        Handler().postDelayed({

            slotsRecyclerAdapter.setOnTransactionClickListener(object : OnSlotClickListener {

                override fun onItemChecked(which: Int, isChecked: Boolean) {

                    if (which == 0) {
                        if (isChecked)
                            selectedList.addAll(indexItem)
                        else {
                            selectedList.clear()
                            for (i in 0..isSectionSelected.size - 1) {
                                isSectionSelected[i] = false
                            }
                        }
                        var i = 1
                        while (i < items.size) {
                            slotsRecyclerAdapter.setItemChecked(i, isChecked)
                            i++
                        }

                    } else if (isChecked) {
                        selectedList.add(which)
                        if (selectedList.size == 5) {
                            selectedList.add(0)
                            slotsRecyclerAdapter.setItemChecked(0, true)
                        }
                    } else if (selectedList.contains(which)) {
                        selectedList.remove(Integer.valueOf(which))
                        isSectionSelected[which] = false
                        if (selectedList.contains(0)) {
                            selectedList.remove(Integer.valueOf(0))
                            slotsRecyclerAdapter.setItemChecked(0, false)
                            isSectionSelected[0] = false
                        }
                    }
                    selectedList.sort()
                    removeDuplicates(selectedList)

                }
            })


        }, 300)

    }


    fun showSlotsAlert() {
        val slots = viewModel.getAllSlotsToShow()
        val items = slots.toTypedArray()
        val indexItem = (0..slots.size - 1).toList().toTypedArray()
        val isSectionSelected = BooleanArray(items.size)
        val selectedList = ArrayList<Int>()
        for (i in 0..items.size - 1) {
            var isfound = false
            if (i == 0 && items.size == (viewDataModel.selectedslots.size + 1)) {
                isfound = true
                isSectionSelected[i] = true
                selectedList.add(0)
            } else
                for (day in viewDataModel.selectedslots) {
                    if (indexItem[i].toString().equals(day)) {
                        isSectionSelected[i] = true
                        isfound = true
                        selectedList.add(i)
                        break
                    }
                }
            if (!isfound) {
                isSectionSelected[i] = false
            }
        }


        val builder = MaterialAlertDialogBuilder(requireContext())
        val view = layoutInflater.inflate(R.layout.fragment_settings_slots, null)
        view.dialogTitleTV.text = getString(R.string.slots_pref)
        builder.setView(view)

        val rv: RecyclerView = view.findViewById(R.id.slotsRV)
        rv.layoutManager = LinearLayoutManager(requireContext())

        val slotsRecyclerAdapter = SlotsRecyclerAdapter()
        rv.adapter = slotsRecyclerAdapter


        builder.setPositiveButton("DONE") { dialogInterface, i ->
            val selectedItemsForDB = ArrayList<String>()
            val selectedItemForView = ArrayList<String>()
            for (j in selectedList.indices) {
                selectedItemForView.add(items[selectedList[j]])
            }
            selectedItemsForDB.addAll(
                viewModel.getSelectedSlotsIds(
                    selectedList
                )
            )
            viewModel.setWorkingSlots(selectedItemsForDB) //selectedString is array for DB

            var selectedStrForSubtitle = getArrayToString(selectedItemForView)
            if (!selectedStrForSubtitle.equals("None")) {
                if (ifWeekdaysNotSelected()) {
                    showDaysAlert()
                } else
                    viewModel.setIsWeekdays(true)
            } else {
                viewModel.setIsWeekdays(false)
            }
            textView66.text = selectedStrForSubtitle
        }
        builder.setOnDismissListener { dialog -> initializeViews() }
        builder.show()

        slotsRecyclerAdapter.updateSlots(items, isSectionSelected)

        Handler().postDelayed({

            slotsRecyclerAdapter.setOnTransactionClickListener(object : OnSlotClickListener {

                override fun onItemChecked(which: Int, isChecked: Boolean) {

                    if (which == 0) {
                        if (isChecked)
                            selectedList.addAll(indexItem)
                        else {
                            selectedList.clear()
                            for (i in 0..isSectionSelected.size - 1) {
                                isSectionSelected[i] = false
                            }
                        }
                        var i = 1
                        while (i < items.size) {
                            slotsRecyclerAdapter.setItemChecked(i, isChecked)
                            i++
                        }

                    } else if (isChecked) {
                        selectedList.add(which)
                        // if only first option is left to be select
                        if (selectedList.size == items.size - 1) {
                            selectedList.add(0)
                            slotsRecyclerAdapter.setItemChecked(0, true)
                        }
                    } else if (selectedList.contains(which)) {

                        selectedList.remove(Integer.valueOf(which))
                        isSectionSelected[which] = false
                        if (selectedList.contains(0)) {
                            selectedList.remove(Integer.valueOf(0))
                            slotsRecyclerAdapter.setItemChecked(0, false)
                            isSectionSelected[0] = false
                        }
                    }
                    selectedList.sort()
                    removeDuplicates(selectedList)

                }
            })


        }, 300)


    }

    fun <T> removeDuplicates(list: ArrayList<T>): ArrayList<T> { // Create a new LinkedHashSet
        val set: MutableSet<T> = LinkedHashSet()
        // Add the elements to set
        set.addAll(list)
        // Clear the list
        list.clear()
        // add the elements of set
        // with no duplicates to the list
        list.addAll(set)
        // return the list
        return list
    }

}