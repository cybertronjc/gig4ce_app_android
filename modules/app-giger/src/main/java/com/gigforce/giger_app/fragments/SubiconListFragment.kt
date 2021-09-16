package com.gigforce.giger_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.gigforce.core.StringConstants
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.giger_app.R
import com.gigforce.giger_app.repo.IMainNavDataRepository
import kotlinx.android.synthetic.main.subicon_list_fragment.*
import javax.inject.Inject

class SubiconListFragment : Fragment() {

    @Inject
    lateinit var repository: IMainNavDataRepository

    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface
    private val viewModel: SubiconListViewModel by viewModels()

    //    private var viewBinding : SubiconList
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.subicon_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntent(savedInstanceState)
        observer()
    }

    private fun observer() {
        repository.getData().observeForever {
            try {
                var featureList  = ArrayList<FeatureItemCard2DVM>()
                it.forEach{
                    arrayLong?.forEach { subIcons->
                        if(it.index == subIcons){
                            featureList.add(it)
                        }
                    }
                }
                subiconsrv.collection = featureList
            } catch (e: Exception) {
            }
        }
    }
    var arrayLong : LongArray?=null
    private fun getDataFromIntent(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            arrayLong =
                it.getLongArray(StringConstants.SUBICONS.value)

        } ?: run {
            arguments?.let {
                arrayLong =
                    it.getLongArray(StringConstants.SUBICONS.value)
            }
        }

    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, FROM_CLIENT_ACTIVATON)
//    }

}