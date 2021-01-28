package com.gigforce.giger_app.screens

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.giger_app.R

class FeaturesBSFragment : Fragment() {

    companion object {
        fun newInstance() = FeaturesBSFragment()
    }

    private lateinit var viewModel: FeaturesBViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.features_bs_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FeaturesBViewModel::class.java)
        // TODO: Use the ViewModel
    }

}