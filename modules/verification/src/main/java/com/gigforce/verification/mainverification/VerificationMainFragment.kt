package com.gigforce.verification.mainverification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.core.navigation.INavigation
import com.gigforce.verification.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.verification_main_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class VerificationMainFragment : Fragment() {

    companion object {
        fun newInstance() = VerificationMainFragment()
    }
    @Inject
    lateinit var navigation : INavigation
    private val viewModel: VerificationMainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.verification_main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observer()
        listener()
    }

    private fun listener() {
        next.setOnClickListener{
            navigation.navigateTo("verification/aadhaarOptionsFragment")
        }
    }

    private fun observer() {
        viewModel.allDocumentsData.observe(viewLifecycleOwner, Observer{
            allDocsRV.collection = it
        })
    }

}