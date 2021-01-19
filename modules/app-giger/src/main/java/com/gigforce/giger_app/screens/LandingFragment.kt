package com.gigforce.giger_app.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.core.recyclerView.CoreRecyclerView
import com.gigforce.giger_app.R
import com.gigforce.giger_app.vm.LandingViewModel


class LandingFragment : Fragment() {
    val viewModel: LandingViewModel by viewModels<LandingViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.allLandingData.observe(this, Observer {
            LandingFragment@ this.view?.findViewById<CoreRecyclerView>(R.id.landing_rv)?.collection =
                it
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_landing, container, false)
    }

}