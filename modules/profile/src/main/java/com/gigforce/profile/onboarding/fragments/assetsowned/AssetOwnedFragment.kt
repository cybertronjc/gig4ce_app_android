package com.gigforce.profile.onboarding.fragments.assetsowned

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.profile.R

class AssetOwnedFragment : Fragment() {

    companion object {
        fun newInstance() = AssetOwnedFragment()
    }

    private lateinit var viewModel: AssetOwnedViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.asset_owned_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AssetOwnedViewModel::class.java)
        // TODO: Use the ViewModel
    }

}