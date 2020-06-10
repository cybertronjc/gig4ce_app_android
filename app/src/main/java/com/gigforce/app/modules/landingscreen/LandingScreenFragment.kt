package com.gigforce.app.modules.landingscreen

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R

class LandingScreenFragment : Fragment() {

    companion object {
        fun newInstance() = LandingScreenFragment()
    }

    private lateinit var viewModel: LandingScreenViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.landingscreen_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LandingScreenViewModel::class.java)
        // TODO: Use the ViewModel
    }

}