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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard3DVM
import com.gigforce.core.StringConstants
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
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
class SubiconFolderBottomSheet : BottomSheetDialogFragment(), IOnBackPressedOverride {

    private val viewModel: SubiconFolderBSViewModel by activityViewModels()

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.subicon_folder_bottomsheet, container, false)
    }

    var data: FeatureItemCard3DVM? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntent(savedInstanceState)
        initViews()
        observer()
        listener()
//        isCancelable = false

    }

    private fun listener() {
        subiconsrv.itemClickListener = object : ItemClickListener {
            override fun onItemClick(view: View, position: Int, dataModel: Any) {
                if (dataModel is FeatureItemCard3DVM) {
                    dataModel.subicons?.let {
                        indicatorList.add(dataModel)
                        refreshScreen(it)
                    } ?: run {
                        navigate(view, dataModel)
                    }
                }
            }
        }

        go_back_btn.setOnClickListener{
            activity?.onBackPressed()

        }
    }

    private fun refreshScreen(subicons: List<Long>) {
        setIndicatorData()
        resetTitle()
        recyclerSubList.clear()
        recyclerSubList.addAll(getFilteredList(subicons, allIconsList))
        subiconsrv.adapter?.notifyDataSetChanged()
    }

    private fun resetTitle() {
        indicatorList[indicatorList.size-1].let {
            var title = if (sharedPreAndCommonUtilInterface.getAppLanguageCode() == "hi") {
                it.hi?.title ?: it.title
            } else
                it.title

            heading.text = title
        }
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
                    var title = if (sharedPreAndCommonUtilInterface.getAppLanguageCode() == "hi") {
                        featureItemCard2DVM.hi?.title ?: featureItemCard2DVM.title
                    } else
                        featureItemCard2DVM.title

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
        if (indicator.childCount > 0)
            (indicator.getChildAt(indicator.childCount - 1) as TextView).setTypeface(
                null,
                Typeface.BOLD
            )
    }

    private fun getIndexForIndicator(): Int {
        var titleLengthFilter = arrayListOf<FeatureItemCard3DVM>()
        titleLengthFilter.addAll(indicatorList)
        var lengthLong = true
        while (lengthLong) {
            var completeTitle = ""

            titleLengthFilter.forEach {
                completeTitle += it.title
            }
            if (completeTitle.length < 35)
                lengthLong = false
            else
                titleLengthFilter.removeAt(0)
        }
        return titleLengthFilter.size + 1

    }

    private fun directSwitchToFolder(index: Int) {
        if (index <= indicatorList.size - 1) {
            if (index == 0) {
                var tempList = indicatorList[0]
                indicatorList.clear()
                indicatorList.add(tempList)
            } else {
                var tempList = indicatorList.subList(0, index + 1).toMutableList()
                indicatorList.clear()
                indicatorList.addAll(tempList)
            }


            indicatorList[indicatorList.size - 1].subicons?.let {
                refreshScreen(it)
            }
        }
    }


    var indicatorList = arrayListOf<FeatureItemCard3DVM>()
    private fun initViews() {
        data?.let {
            indicatorList.add(it)
        }
        setIndicatorData()
//        subiconsrv.setOrientationAndRows(0, 1)
        setTitle()
    }

    private fun setTitle() {
        data?.let {
            var title = if (sharedPreAndCommonUtilInterface.getAppLanguageCode() == "hi") {
                it.hi?.title ?: it.title
            } else
                it.title

            heading.text = title
        }

    }

    var allIconsList = arrayListOf<FeatureItemCard3DVM>()
    var recyclerSubList = arrayListOf<FeatureItemCard3DVM>()
    private fun observer() {
        viewModel.allIconsLiveData.observeForever {
            if (it != null)
                try {
                    allIconsList.clear()
                    allIconsList.addAll(it)
                    recyclerSubList.addAll(getFilteredList(data?.subicons, it))
                    subiconsrv.collection = recyclerSubList
                } catch (e: Exception) {
                }
        }
    }

    private fun getFilteredList(
        arrayLong: List<Long>?,
        allIconsList: List<FeatureItemCard3DVM>
    ): ArrayList<FeatureItemCard3DVM> {
        var filteredList = arrayListOf<FeatureItemCard3DVM>()
        allIconsList.forEach {
            arrayLong?.forEach { subIcons ->
                if (it.index == subIcons) {
                    filteredList.add(it)
                }
            }
        }
        return filteredList
    }

    private fun navigate(view: View, data: FeatureItemCard3DVM) {
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
                FeatureItemCard3DVM::class.java
            )
        } ?: run {
            arguments?.let {
                data = Gson().fromJson(
                    it.getString(StringConstants.ICON.value),
                    FeatureItemCard3DVM::class.java
                )
            }
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnKeyListener { _: DialogInterface, keyCode: Int, keyEvent: KeyEvent ->
                if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
//                    if (!backStackIcons())
                        dismiss()

                    return@setOnKeyListener false
                }
                return@setOnKeyListener false
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return backStackIcons()
    }
}