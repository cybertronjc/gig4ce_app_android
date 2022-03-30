package com.gigforce.giger_app.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.ext.pushOnclickListener
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_app.R
import com.gigforce.giger_app.vm.FeaturesBSViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.features_bs_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class FeaturesBSFragment : Fragment() {
    val viewModel: FeaturesBSViewModel by activityViewModels()
    @Inject lateinit var navigation : INavigation
    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.features_bs_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel._allBSData.observe(viewLifecycleOwner, Observer {
            bs_rv.collection = it

        })
        initViews()
    }

    private fun initViews() {
        application_version.text =
            getString(R.string.version_app_giger) + " " + sharedPreAndCommonUtilInterface.getCurrentVersion()
        application_version.pushOnclickListener{
            navigation.navigateTo("HelpSectionFragment")
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.state?.let { parcelable ->
            bs_rv?.layoutManager?.onRestoreInstanceState(parcelable)

        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.state =
            bs_rv?.layoutManager?.onSaveInstanceState()
    }

}