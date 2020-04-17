package com.gigforce.app.modules.preferences.datetime.weekday

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.week_day_fragment.*
import java.util.*
import kotlin.collections.ArrayList


class WeekDayFragment : BaseFragment() {

    companion object {
        fun newInstance() = WeekDayFragment()
    }

    private lateinit var viewModel: WeekDayViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.week_day_fragment, inflater,container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(WeekDayViewModel::class.java)
        listener()
    }

    private fun listener() {
        textView60.setOnClickListener(View.OnClickListener { choiceDaysAlert() })
        textView64.setOnClickListener(View.OnClickListener { choiceSlotsAlert() })
    }

    fun choiceDaysAlert() {
        val items = arrayOf("All", "Monday", "Tuesday", "Wednesday","Thrusday","Friday")
        val selectedList = ArrayList<Int>()
        val builder = AlertDialog.Builder(activity)

        builder.setTitle("Days")
        builder.setMultiChoiceItems(items, null
        ) { dialog, which, isChecked ->
            if (isChecked) {
                selectedList.add(which)
            } else if (selectedList.contains(which)) {
                selectedList.remove(Integer.valueOf(which))
            }
        }

        builder.setPositiveButton("DONE") { dialogInterface, i ->
            val selectedStrings = ArrayList<String>()

            for (j in selectedList.indices) {
                selectedStrings.add(items[selectedList[j]])
            }
            textView62.text = Arrays.toString(selectedStrings.toTypedArray())
        }

        builder.show()

    }
    fun choiceSlotsAlert() {
        val items = arrayOf("All", "06:00 am-10:00 am", "10:00am- 12:00pm", "12:00pm-06:00pm","06:00pm-09:00pm","09:00pm-12:00pm","12:00 am-03:00 am")
        val indexItem = arrayOf(0,1,2,3,4,5,6)
        val selectedList = ArrayList<Int>()
        val builder = AlertDialog.Builder(activity)

        builder.setTitle("Slots")
        builder.setMultiChoiceItems(items, null
        ) { dialog, which, isChecked ->
            if(which==0){
                val dialog = dialog as AlertDialog
                val v: ListView = dialog.listView
                var i = 1
                while (i < items.size) {
                    v.setItemChecked(i, isChecked)
                    i++
                }
                selectedList.addAll(indexItem)

            }else if (isChecked) {
                selectedList.add(which)
                if(selectedList.size==6){
                    selectedList.add(0)
                    val dialog = dialog as AlertDialog
                    val v: ListView = dialog.listView
                    v.setItemChecked(0,true)
                }
            } else if (selectedList.contains(which)) {
                selectedList.remove(Integer.valueOf(which))
                if(selectedList.contains(0))
                    selectedList.remove(Integer.valueOf(0))
                val dialog = dialog as AlertDialog
                val v: ListView = dialog.listView
                v.setItemChecked(0,false)
            }
            selectedList.sort()
            removeDuplicates(selectedList)
        }

        builder.setPositiveButton("DONE") { dialogInterface, i ->
            val selectedStrings = ArrayList<String>()

            for (j in selectedList.indices) {
                selectedStrings.add(items[selectedList[j]])
            }
            var selectedStr = Arrays.toString(selectedStrings.toTypedArray())
            selectedStr = selectedStr.substring(1,selectedStr.length-1)
            textView66.text = selectedStr
        }

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