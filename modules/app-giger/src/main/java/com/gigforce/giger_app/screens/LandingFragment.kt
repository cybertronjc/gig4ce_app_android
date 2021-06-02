package com.gigforce.giger_app.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_app.R
import com.gigforce.giger_app.vm.LandingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_landing.*
import javax.inject.Inject


@AndroidEntryPoint
class LandingFragment : Fragment() {
    val viewModel: LandingViewModel by viewModels()
    @Inject lateinit var navigation: INavigation
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel._allLandingData.observe(viewLifecycleOwner, Observer {
            landing_rv.collection = it

        })
        listeners()
    }



    private fun listeners() {
        app_bar.setOnClickListener {
            navigation.navigateTo("bottom_sheet")
        }

    }

}