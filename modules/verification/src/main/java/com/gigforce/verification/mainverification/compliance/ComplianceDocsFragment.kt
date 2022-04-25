package com.gigforce.verification.mainverification.compliance

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.remote.verification.ComplianceDocumentDetailDM
import com.gigforce.core.IEventTracker
import com.gigforce.core.ScopedStorageConstants
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.recyclerView.ItemClickListener
import com.gigforce.core.utils.Lce
import com.gigforce.verification.R
import com.gigforce.verification.databinding.ComplianceDocsFragmentBinding
import com.gigforce.verification.databinding.FragmentMyDocumentsBinding
import com.gigforce.verification.mainverification.MyDocumentsPagerAdapter
import com.gigforce.verification.mainverification.vaccine.mainvaccine.VaccineMainFragment
import com.gigforce.verification.mainverification.vaccine.models.VaccineCertDetailsDM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.vaccine_main_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class ComplianceDocsFragment : Fragment(), IOnBackPressedOverride {

    companion object {
        fun newInstance() = ComplianceDocsFragment()
        const val TAG = "ComplianceDocsFragment"
        private const val REQUEST_STORAGE_PERMISSION = 102
    }

    private val viewModel : ComplianceDocsViewModel by viewModels()

    private lateinit var viewBinding: ComplianceDocsFragmentBinding

    @Inject
    lateinit var navigation: INavigation
    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface


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
        sharedPreAndCommonUtilInterface.saveDataBoolean(
            com.gigforce.common_ui.StringConstants.COMPLIANCE_SP.value,
            false
        )
        initViews()
        initListeners()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.getComplianceData()
        viewModel.complianceLiveData.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it) {
                Lce.Loading -> {
                    progressBar.visible()
                }
                is Lce.Content -> {
                    progressBar.gone()
                    if (it.content.complianceNumbers.isEmpty() && it.content.complianceDocuments.isEmpty()) {
                        viewBinding.noComplianceData.visible()
                        viewBinding.complianceRv.gone()
                        viewBinding.complianceTitle.gone()
                        viewBinding.docsRv.gone()
                    }

                    if (it.content.complianceNumbers.isNotEmpty()) {
                        viewBinding.noComplianceData.gone()
                        viewBinding.complianceTitle.gone()
                        viewBinding.complianceRv.visible()
                        viewBinding.complianceRv.collection = it.content.complianceNumbers
                    } else {
                        viewBinding.complianceTitle.gone()
                        viewBinding.complianceRv.gone()
                    }

                    if (it.content.complianceDocuments.isNotEmpty()) {
                        viewBinding.noComplianceData.gone()
                        viewBinding.complianceTitle.visible()
                        viewBinding.docsRv.visible()
                        viewBinding.docsRv.collection = it.content.complianceDocuments
                    } else {
                        viewBinding.complianceTitle.gone()
                        viewBinding.docsRv.gone()
                    }

                }
                is Lce.Error -> {
                    progressBar.gone()
                    viewBinding.noComplianceData.visible()
                    viewBinding.complianceRv.gone()
                }
            }
        })

    }
    private fun initListeners() {

    }

    private fun initViews() {

    }

    override fun onBackPressed(): Boolean {
        return false
    }

}