package com.gigforce.app.modules.explore_by_role

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.HorizontaltemDecoration
import com.gigforce.app.utils.StringConstants
import com.gigforce.app.utils.ViewModelProviderFactory
import kotlinx.android.synthetic.main.layout_role_details_fragment.*

class RoleDetailsFragment : BaseFragment() {
    private val viewModelFactory by lazy {
        ViewModelProviderFactory(RoleDetailsVIewModel(RoleDetailsRepository()))
    }
    private val viewModel: RoleDetailsVIewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(RoleDetailsVIewModel::class.java)
    }
    private val adapterPreferredLocation: AdapterPreferredLocation by lazy {
        AdapterPreferredLocation()
    }
    private lateinit var mRoleID: String;
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflateView(R.layout.layout_role_details_fragment, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromSavedState(savedInstanceState)
        setupPreferredLocationRv()
        initObservers()
    }

    private fun initObservers() {

        viewModel.observerRole.observe(viewLifecycleOwner, Observer { role ->
            run {
                tv_role_role_details.text = role?.role_title
                tv_what_content_role_details.text = role?.about
                tv_what_read_more_details.text =
                    "${getString(R.string.what_does_a)} ${role?.role_title} ${getString(
                        R.string.do_question_mark
                    )}"
                adapterPreferredLocation.addData(role?.top_locations ?: mutableListOf())
                ll_earnings_role_details.setOnClickListener {
                    ll_earn_role_details.removeAllViews()
                    role?.payments_and_benefits?.forEachIndexed() { index, element ->
                        if (index < 2) {
                            val textView = AppCompatTextView(requireContext())
                            textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.shape_circle_lipstick,
                                0,
                                0,
                                0
                            )
                            textView.compoundDrawablePadding =
                                resources.getDimensionPixelSize(R.dimen.size_8)
                            textView.text = element
                            ll_earnings_role_details.addView(textView)
                        }
                    }
                    if (role?.payments_and_benefits?.size!! > 1) {
                        val textView = AppCompatTextView(requireContext())
                        textView.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_add,
                            0,
                            0,
                            0
                        )
                        textView.compoundDrawablePadding =
                            resources.getDimensionPixelSize(R.dimen.size_8)
                        textView.text = getString(R.string.more)
                        ll_earnings_role_details.addView(textView)
                    }

                }

            }


        })
        viewModel.observerError.observe(viewLifecycleOwner, Observer {
            showToast(it ?: "")
        })

        viewModel.getRoleDetails(mRoleID)
    }

    private fun setupPreferredLocationRv() {
        rv_preferred_locations_role_details.adapter = adapterPreferredLocation
        rv_preferred_locations_role_details.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rv_preferred_locations_role_details.addItemDecoration(
            HorizontaltemDecoration(
                requireContext(),
                R.dimen.size_11
            )
        )

    }

    private fun getDataFromSavedState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mRoleID = it.getString(StringConstants.ROLE_ID.value) ?: return@let
        }

        arguments?.let {
            mRoleID = it.getString(StringConstants.ROLE_ID.value) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.ROLE_ID.value, mRoleID)
    }
}