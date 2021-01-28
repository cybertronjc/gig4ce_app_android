package com.gigforce.client_activation.fragment

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.client_activation.viewmodel.ClientActivationSectionViewModel
import com.gigforce.client_activation.R
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import kotlinx.android.synthetic.main.client_activation_section_fragment.*

class ClientActivationSectionFragment : Fragment() {

    private lateinit var viewModel: ClientActivationSectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.client_activation_section_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ClientActivationSectionViewModel::class.java)
        client_activation_component.bind(FeatureLayoutDVM("", "Explore Gig", getExploreGig()))
    }

    private fun getExploreGig(): List<Any> {
        var exploreGigItems = ArrayList<Any>()
        exploreGigItems.add(
            FeatureItemCardDVM(
                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/client_activation%2F21North%2F21N_cover_pic.jpg?alt=media&token=52dc3fbd-22e4-4ffa-a2d3-2e61915e0291",
                "21North",
                ""
            )
        )
        exploreGigItems.add(
            FeatureItemCardDVM(
                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/client_activation%2F21North%2F21N_cover_pic.jpg?alt=media&token=52dc3fbd-22e4-4ffa-a2d3-2e61915e0291",
                "21North",
                ""
            )
        )
        return exploreGigItems
    }
}