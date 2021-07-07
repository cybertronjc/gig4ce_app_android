package com.gigforce.giger_app.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.giger_app.R
import com.gigforce.giger_app.vm.FeaturesBSViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.features_bs_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class FeaturesBSFragment : Fragment() {
    val viewModel: FeaturesBSViewModel by viewModels()
    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.features_bs_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel._allBSData.observe(viewLifecycleOwner, Observer {
            bs_rv.collection = it

        })
        initViews()
        listeners()
    }

    private fun initViews() {
        application_version.text =
            getString(R.string.version) + " " + sharedPreAndCommonUtilInterface.getCurrentVersion()
    }

    private fun listeners() {

    }

}