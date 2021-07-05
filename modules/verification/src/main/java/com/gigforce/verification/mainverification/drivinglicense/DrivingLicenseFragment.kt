package com.gigforce.verification.mainverification.drivinglicense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gigforce.verification.R
import com.gigforce.verification.databinding.DrivingLicenseFragmentBinding

class DrivingLicenseFragment : Fragment() {

    private val viewModel: DrivingLicenseViewModel by viewModels()
    private lateinit var viewBinding: DrivingLicenseFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.driving_license_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}