package com.gigforce.app.modules.preferences.daytime.weekday

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Switch
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.preferences.SharedPreferenceViewModel
import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel
import kotlinx.android.synthetic.main.week_day_fragment.*
import java.util.*
import kotlin.collections.ArrayList


class WeekDayFragment : BaseFragment() {

    companion object {
        fun newInstance() = WeekDayFragment()
    }

    private lateinit var viewModel: SharedPreferenceViewModel
    private lateinit var viewDataModel: PreferencesDataModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.week_day_fragment, inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SharedPreferenceViewModel::class.java)
        initializeViews()
        listener()
        observePreferenceData()
    }

    private fun observePreferenceData() {
        viewModel.preferenceDataModel.observe(this, Observer { preferenceData ->
            viewModel.setPreferenceDataModel(preferenceData)
            initializeViews()
        })
    }

    private fun initializeViews() {
        viewDataModel = viewModel.getPreferenceDataModel()
        switch3.setChecked(viewDataModel.isweekdaysenabled)
        textView62.text = getArrayToString(viewDataModel.selecteddays)
        textView66.text = getArrayToString(viewDataModel.selectedslots)
        if (viewDataModel.isweekdaysenabled) {
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
            if (viewDataModel.isweekdaysenabled) showDaysAlert() else showToast(
                "Please enable weekdays first!!"
            )
        })
        textView64.setOnClickListener(View.OnClickListener {
            if (viewDataModel.isweekdaysenabled) showSlotsAlert() else showToast(
                "Please enable weekdays first!!"
            )
        })
        switch3.setOnClickListener { view ->
            var isChecked = (view as Switch).isChecked
            if(isChecked){
                if(ifWeekdaysNotSelected()){
                    showDaysAlert()
                }
                else if(ifSlotsNotSelected()){
                showSlotsAlert()
                }
                else
                    viewModel.setIsWeekdays(isChecked)

            }
            else
            viewModel.setIsWeekdays(isChecked)
        }
        imageView16.setOnClickListener(View.OnClickListener { activity?.onBackPressed() })
    }

    private fun ifSlotsNotSelected():Boolean{
        if(getArrayToString(viewDataModel.selectedslots).equals("None")){
            return true
        }
        else
            return false
    }

    private fun ifWeekdaysNotSelected(): Boolean {
        if(getArrayToString(viewDataModel.selecteddays).equals("None"))
        {
            return true
        }
        else {
            return false
        }
    }

    fun showDaysAlert() {
        val items = arrayOf("All", "Monday", "Tuesday", "Wednesday", "Thrusday", "Friday")
        val indexItem = arrayOf(0, 1, 2, 3, 4, 5)
        val isSectionSelected = BooleanArray(items.size)
        val selectedList = ArrayList<Int>()
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Days")

        for (i in 0..items.size-1) {
            var isfound = false
            for (day in viewDataModel.selecteddays) {
                if(items[i].equals(day)){
                    isSectionSelected[i] = true
                    isfound = true
                    selectedList.add(i)
                    break
                }
            }
            if(!isfound){
                isSectionSelected[i] = false
            }
        }
        builder.setMultiChoiceItems(
            items, isSectionSelected
        ) { dialog, which, isChecked ->
            if (which == 0) {
                val dialog = dialog as AlertDialog
                val v: ListView = dialog.listView
                var i = 1
                while (i < items.size) {
                    v.setItemChecked(i, isChecked)
                    i++
                }
                selectedList.addAll(indexItem)

            } else if (isChecked) {
                selectedList.add(which)
                if (selectedList.size == 5) {
                    selectedList.add(0)
                    val dialog = dialog as AlertDialog
                    val v: ListView = dialog.listView
                    v.setItemChecked(0, true)
                }
            } else if (selectedList.contains(which)) {
                selectedList.remove(Integer.valueOf(which))
                if (selectedList.contains(0))
                    selectedList.remove(Integer.valueOf(0))
                val dialog = dialog as AlertDialog
                val v: ListView = dialog.listView
                v.setItemChecked(0, false)
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
                viewModel.setWorkingDays(selectedStrings)
            if(!selectedStr.equals("None")) {
                if(ifSlotsNotSelected()){
                    showSlotsAlert()
                }
                else{
                    viewModel.setIsWeekdays(true)
                }
            }
            else{
                viewModel.setIsWeekdays(false)
            }

            textView62.text = selectedStr
        }
        builder.setOnDismissListener { dialog ->initializeViews()  }

        builder.show()
    }

    private fun setAllDaysEnabled( v: ListView, size:Int ) {
        for(i in 0..size-1){
            v.setItemChecked(i,true)
        }
    }

    fun showSlotsAlert() {
        val items = arrayOf(
            "All",
            "06:00 am - 10:00 am",
            "10:00 am - 12:00 pm",
            "12:00 pm - 06:00 pm",
            "06:00 pm - 09:00 pm",
            "09:00 pm - 12:00 pm",
            "12:00 am - 03:00 am"
        )
        val indexItem = arrayOf(0, 1, 2, 3, 4, 5, 6)
        val isSectionSelected = BooleanArray(items.size)
        val selectedList = ArrayList<Int>()
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Slots")
        for (i in 0..items.size-1) {
            var isfound = false
            for (day in viewDataModel.selectedslots) {
                if(items[i].equals(day)){
                    isSectionSelected[i] = true
                    isfound = true
                    selectedList.add(i)
                    break
                }
            }
            if(!isfound){
                isSectionSelected[i] = false
            }
        }
        builder.setMultiChoiceItems(
            items, isSectionSelected
        ) { dialog, which, isChecked ->
            if (which == 0) {
                val dialog = dialog as AlertDialog
                val v: ListView = dialog.listView
                var i = 1
                while (i < items.size) {
                    v.setItemChecked(i, isChecked)
                    i++
                }
                selectedList.addAll(indexItem)

            } else if (isChecked) {
                selectedList.add(which)
                if (selectedList.size == 6) {
                    selectedList.add(0)
                    val dialog = dialog as AlertDialog
                    val v: ListView = dialog.listView
                    v.setItemChecked(0, true)
                }
            } else if (selectedList.contains(which)) {
                selectedList.remove(Integer.valueOf(which))
                if (selectedList.contains(0))
                    selectedList.remove(Integer.valueOf(0))
                val dialog = dialog as AlertDialog
                val v: ListView = dialog.listView
                v.setItemChecked(0, false)
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
                viewModel.setWorkingSlots(selectedStrings) //selectedString is array for DB
            if(!selectedStr.equals("None")) {
                viewModel.setIsWeekdays(true)
            }
            else{
                viewModel.setIsWeekdays(false)
            }
            textView66.text = selectedStr
        }
        builder.setOnDismissListener { dialog ->initializeViews()  }
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