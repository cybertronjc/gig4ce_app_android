package com.gigforce.giger_app.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.giger_app.R
import com.gigforce.giger_app.vm.FeaturesBSViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.features_bs_fragment.*

@AndroidEntryPoint
class FeaturesBSFragment : Fragment() {
    val viewModel: FeaturesBSViewModel by viewModels()

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
        //Todo initialize profile views
    }

    private fun listeners() {

    }

}