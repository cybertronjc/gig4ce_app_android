package com.gigforce.verification.mainverification.compliance

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.core.IEventTracker
import com.gigforce.core.navigation.INavigation
import com.gigforce.verification.R
import com.gigforce.verification.databinding.ComplianceDocsFragmentBinding
import com.gigforce.verification.databinding.FragmentMyDocumentsBinding
import com.gigforce.verification.mainverification.MyDocumentsPagerAdapter
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ComplianceDocsFragment : Fragment(), IOnBackPressedOverride {

    companion object {
        fun newInstance() = ComplianceDocsFragment()
        const val TAG = "ComplianceDocsFragment"
    }

    private val viewModel : ComplianceDocsViewModel by viewModels()

    private lateinit var viewBinding: ComplianceDocsFragmentBinding

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var eventTracker: IEventTracker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(),
            ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
        )
        viewBinding = ComplianceDocsFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //getIntentData(savedInstanceState)
        initViews()
        initListeners()
    }

    private fun initListeners() {

    }

    private fun initViews() {

    }

    override fun onBackPressed(): Boolean {
        return false
    }

}