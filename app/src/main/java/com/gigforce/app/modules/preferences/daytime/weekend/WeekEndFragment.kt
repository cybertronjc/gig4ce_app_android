package com.gigforce.app.modules.preferences.daytime.weekend

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Switch
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.preferences.SharedPreferenceViewModel
import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel
import kotlinx.android.synthetic.main.week_day_fragment.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet

class WeekEndFragment : BaseFragment() {

    companion object {
        fun newInstance() = WeekEndFragment()
    }

    private lateinit var viewModel: SharedPreferenceViewModel
    private lateinit var viewDataModel: PreferencesDataModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.week_end_fragment, inflater, container)
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
    }
    override fun isConfigRequired(): Boolean {
        return true
    }
    private fun initializeViews() {
        viewDataModel = viewModel.getPreferenceDataModel()
        switch3.setChecked(viewDataModel.isweekendenabled)
        textView62.text = getArrayToString(viewDataModel.selectedweekends)
//        textView66.text = getArrayToString(viewDataModel.selectedweekendslots)
        var selectedStrForSubtitle = getArrayToString(
            viewModel.getSelectedSlotsToShow(
                configDataModel,
                viewDataModel.selectedweekendslots
            )
        )
        textView66.text = selectedStrForSubtitle

        if (viewDataModel.isweekendenabled) {
            setTextViewColor(textView61, R.color.black)
            setTextViewColor(textView65, R.color.black)
        } else {
            setTextViewColor(textView61, R.color.gray_color)
            setTextViewColor(textView65, R.color.gray_color)

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
        textView60.setOnClickListener(View.OnClickListener {
            showDaysAlert()
        })
        textView64.setOnClickListener(View.OnClickListener {
            showSlotsAlert()
        })
        switch3.setOnClickListener { view ->
            var isChecked = (view as Switch).isChecked
            if (isChecked) {
                if (ifWeekenddaysNotSelected()) {
                    showDaysAlert()
                } else if (ifSlotsNotSelected()) {
                    showSlotsAlert()
                } else
                    viewModel.setIsWeekend(isChecked)

            } else
                viewModel.setIsWeekend(isChecked)
        }
        imageView16.setOnClickListener(View.OnClickListener { activity?.onBackPressed() })
    }

    private fun ifSlotsNotSelected(): Boolean {
        if (getArrayToString(viewDataModel.selectedweekendslots).equals("None")) {
            return true
        } else
            return false
    }

    private fun ifWeekenddaysNotSelected(): Boolean {
        if (getArrayToString(viewDataModel.selectedweekends).equals("None")) {
            return true
        } else {
            return false
        }
    }

    fun showDaysAlert() {
        val items = arrayOf("All", "Saturday", "Sunday")
        val indexItem = arrayOf(0, 1, 2)
        var isSectionSelected = BooleanArray(items.size)
        var selectedList = ArrayList<Int>()
        val builder = AlertDialog.Builder(activity,R.style.MultiSelectDialogStyle)
        builder.setTitle("Days")
        for (i in 0..items.size - 1) {
            var isfound = false
            for (day in viewDataModel.selectedweekends) {
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

        builder.setMultiChoiceItems(
            items, isSectionSelected
        ) { dialog, which, isChecked ->
            val dialog = dialog as AlertDialog
            val v: ListView = dialog.listView
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
                    v.setItemChecked(i, isChecked)
                    i++
                }

            } else if (isChecked) {
                selectedList.add(which)
                if (selectedList.size == 2) {
                    selectedList.add(0)
                    v.setItemChecked(0, true)
                }
            } else if (selectedList.contains(which)) {
                selectedList.remove(Integer.valueOf(which))
                isSectionSelected[which] = false
                if (selectedList.contains(0)) {
                    selectedList.remove(Integer.valueOf(0))
                    v.setItemChecked(0, false)
                    isSectionSelected[0] = false
                }
            }
            selectedList.sort()
            removeDuplicates(selectedList)
        }
        builder.setPositiveButton("DONE") { dialogInterface, i ->
            val selectedStrings = ArrayList<String>()
            for (j in selectedList.indices) {
                selectedStrings.add(items[selectedList[j]])
            }
            var selectedStr = getArrayToString(selectedStrings)
            viewModel.setWorkendDays(selectedStrings)
            if (!selectedStr.equals("None")) {
                if (ifSlotsNotSelected()) {
                    showSlotsAlert()
                } else {
                    viewModel.setIsWeekend(true)
                }
            } else {
                viewModel.setIsWeekend(false)
            }

            textView62.text = selectedStr
        }
        builder.setOnDismissListener { dialog -> initializeViews() }

        builder.show()
    }



    fun showSlotsAlert() {
        val slots = viewModel.getAllSlotsToShow(configDataModel)
        val items = slots.toTypedArray()
        val indexItem = (0..slots.size - 1).toList().toTypedArray()
        val isSectionSelected = BooleanArray(items.size)
        val selectedList = ArrayList<Int>()
        val builder = AlertDialog.Builder(activity,R.style.MultiSelectDialogStyle)
        builder.setTitle("Slots")
        for (i in 0..items.size - 1) {
            var isfound = false
            if(i==0 && items.size==(viewDataModel.selectedweekendslots.size+1)){
                isfound=true
                isSectionSelected[i] = true
                selectedList.add(0)
            } else
                for (day in viewDataModel.selectedweekendslots) {
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

        builder.setMultiChoiceItems(
            items, isSectionSelected
        ) { dialog, which, isChecked ->
            val dialog = dialog as AlertDialog
            val v: ListView = dialog.listView
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
                    v.setItemChecked(i, isChecked)
                    i++
                }

            } else if (isChecked) {
                selectedList.add(which)
                // if only first option is left to be select
                if (selectedList.size == items.size - 1) {
                    selectedList.add(0)
                    v.setItemChecked(0, true)
                }
            } else if (selectedList.contains(which)) {

                selectedList.remove(Integer.valueOf(which))
                isSectionSelected[which] = false
                if (selectedList.contains(0)) {
                    selectedList.remove(Integer.valueOf(0))
                    v.setItemChecked(0, false)
                    isSectionSelected[0] = false
                }
            }
            selectedList.sort()
            removeDuplicates(selectedList)
        }

        builder.setPositiveButton("DONE") { dialogInterface, i ->
            val selectedItemsForDB = ArrayList<String>()
            val selectedItemForView = ArrayList<String>()
            for (j in selectedList.indices) {
                selectedItemForView.add(items[selectedList[j]])
            }
            selectedItemsForDB.addAll(
                viewModel.getSelectedSlotsIds(
                    selectedList,
                    configDataModel
                )
            )
            viewModel.setWorkendSlots(selectedItemsForDB) //selectedString is array for DB

            var selectedStrForSubtitle = getArrayToString(selectedItemForView)
            if (!selectedStrForSubtitle.equals("None")) {
                if (ifWeekenddaysNotSelected()) {
                    showDaysAlert()
                } else
                    viewModel.setIsWeekend(true)
            } else {
                viewModel.setIsWeekend(false)
            }
            textView66.text = selectedStrForSubtitle
        }
        builder.setOnDismissListener { dialog -> initializeViews() }
        builder.show()

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