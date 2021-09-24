package com.gigforce.giger_app.screens

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.gigforce.core.StringConstants
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.recyclerView.ItemClickListener
import com.gigforce.giger_app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.subicon_folder_bottomsheet.*
import kotlinx.android.synthetic.main.subicon_list_fragment.*
import kotlinx.android.synthetic.main.subicon_list_fragment.subiconsrv
import javax.inject.Inject

@AndroidEntryPoint
class SubiconFolderBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: SubiconFolderBSViewModel by activityViewModels()

    @Inject
    lateinit var navigation: INavigation

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.subicon_folder_bottomsheet, container, false)
    }

    var data: FeatureItemCard2DVM? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntent(savedInstanceState)
        initViews()
        observer()
        listener()
        isCancelable = false

    }

    private fun listener() {
        subiconsrv.itemClickListener = object : ItemClickListener {
            override fun onItemClick(view: View, position: Int, dataModel: Any) {
                if (dataModel is FeatureItemCard2DVM) {
                    dataModel.subicons?.let {
                        indicatorList.add(dataModel)
                        refreshScreen(it)
                    } ?: run {
                        navigate(view, dataModel)
                    }
                }
            }
        }
    }

    private fun refreshScreen(subicons: List<Long>) {
        setIndicatorData()
        recyclerSubList.clear()
        recyclerSubList.addAll(getFilteredList(subicons, allIconsList))
        subiconsrv.adapter?.notifyDataSetChanged()
    }

    private fun backStackIcons(): Boolean {
        if (indicatorList.size > 1) {
            indicatorList.removeAt(indicatorList.size - 1)
            indicatorList.get(indicatorList.size - 1).subicons?.let {
                refreshScreen(it)
            }
            return true
        }
        return false
    }

    private fun setIndicatorData() {
        indicator.removeAllViews()

        var indexTill = getIndexForIndicator()

        indicatorList.forEachIndexed { index, featureItemCard2DVM ->
            if (index <= indicatorList.size - 1 && index >= indicatorList.size - indexTill) {

                var textView = TextView(context)
                if (index == indicatorList.size - indexTill) {
                    textView.text = "..."
                } else {
                    var title = featureItemCard2DVM.title
                    if (index > 0)
                        title = " > $title"
                    textView.text = title
                }


                textView.setOnClickListener {
                    directSwitchToFolder(index)
                }
                indicator.addView(textView)
            }
        }
        (indicator.getChildAt(indicator.childCount - 1) as TextView).setTypeface(
            null,
            Typeface.BOLD
        )
    }

    private fun getIndexForIndicator(): Int {
        var titleLengthFilter = arrayListOf<FeatureItemCard2DVM>()
        titleLengthFilter.addAll(indicatorList)
        var lengthLong = true
        while (lengthLong){
            var completeTitle = ""

            titleLengthFilter.forEach{
                completeTitle+=it.title
            }
            if(completeTitle.length<35)
                lengthLong = false
            else
                titleLengthFilter.removeAt(0)
        }
        return titleLengthFilter.size

    }

    private fun directSwitchToFolder(index: Int) {
        if (index <= indicatorList.size - 1) {
            if (index == 0) {
                var tempList = indicatorList[0]
                indicatorList.clear()
                indicatorList.add(tempList)
            } else {
                var tempList = indicatorList.subList(0, index)
                indicatorList.clear()
                indicatorList.addAll(tempList)
            }


            indicatorList[indicatorList.size - 1].subicons?.let {
                refreshScreen(it)
            }
        }
    }


    var indicatorList = arrayListOf<FeatureItemCard2DVM>()
    private fun initViews() {
        data?.let {
            indicatorList.add(it)
        }
        setIndicatorData()
        subiconsrv.setOrientationAndRows(1, 4)
    }

    var allIconsList = arrayListOf<FeatureItemCard2DVM>()
    var recyclerSubList = arrayListOf<FeatureItemCard2DVM>()
    private fun observer() {
        viewModel.allIconsLiveData.observeForever {
            try {
                allIconsList.addAll(getFilteredList(data?.subicons, it))
                recyclerSubList.addAll(allIconsList)
                subiconsrv.collection = recyclerSubList
            } catch (e: Exception) {
            }
        }
    }

    private fun getFilteredList(
        arrayLong: List<Long>?,
        allIconsList: List<FeatureItemCard2DVM>
    ): ArrayList<FeatureItemCard2DVM> {
        var filteredList = arrayListOf<FeatureItemCard2DVM>()
        allIconsList.forEach {
            arrayLong?.forEach { subIcons ->
                if (it.index == subIcons) {
                    filteredList.add(it)
                }
            }
        }
        return filteredList
    }

    private fun navigate(view: View, data: FeatureItemCard2DVM) {
        data.getNavArgs()?.let {
            it.args?.let {
                it.putString(
                    "title",
                    view.findViewById<TextView>(R.id.feature_title).text.toString()
                )
            }
            navigation.navigateTo(it.navPath, it.args)

        }
    }

    private fun getDataFromIntent(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            data = Gson().fromJson(
                it.getString(StringConstants.ICON.value),
                FeatureItemCard2DVM::class.java
            )
        } ?: run {
            arguments?.let {
                data = Gson().fromJson(
                    it.getString(StringConstants.ICON.value),
                    FeatureItemCard2DVM::class.java
                )
            }
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnKeyListener { _: DialogInterface, keyCode: Int, keyEvent: KeyEvent ->
                if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
                    if (!backStackIcons())
                        dismiss()

                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }
        }
    }
}