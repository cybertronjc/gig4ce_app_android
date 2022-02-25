package com.gigforce.giger_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.gigforce.core.StringConstants
import com.gigforce.giger_app.R
import com.gigforce.giger_app.vm.MainNavigationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.subicon_list_fragment.*

@AndroidEntryPoint
class SubiconListFragment : Fragment() {

//    private val viewModel: MainNavigationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.subicon_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntent(savedInstanceState)
        initViews()
        observer()
        listener()
    }

    private fun initViews() {
        toolbar.setAppBarTitle(title)
        subiconsrv.setOrientationAndRows(1, 4)
    }

    private fun listener() {
        toolbar.apply {
            setBackButtonListener {
                activity?.onBackPressed()
            }
        }
    }

    private fun observer() {

//        viewModel.liveData.observeForever {
//            try {
//
//                val featureList = ArrayList<FeatureItemCard2DVM>()
//                it.forEach {
//                    arrayLong?.forEach { subIcons ->
//                        if (it.index == subIcons) {
//                            featureList.add(it)
//                        }
//                    }
//                }
//                subiconsrv.collection = featureList
//            } catch (e: Exception) {
//            }
//        }
    }

    var arrayLong: ArrayList<Long>? = null
    var title = ""
    private fun getDataFromIntent(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            arrayLong =
                it.get(StringConstants.SUBICONS.value) as ArrayList<Long>
            title = it.getString("title") ?: ""
        } ?: run {
            arguments?.let {
                arrayLong =
                    it.get(StringConstants.SUBICONS.value) as ArrayList<Long>
                title = it.getString("title") ?: ""
            }
        }

    }
}