package com.gigforce.app.modules.client_activation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.StringConstants
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.*


class DocsSubSchedulerFragment : BaseFragment() {

    private lateinit var mWordOrderID: String

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        return inflateView(R.layout.fragment_docs_sub_scheduler, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        view7.setOnClickListener {
            SelectPartnerSchoolBottomSheet.newInstance(bundleOf(
                    StringConstants.WORK_ORDER_ID.value to mWordOrderID
            )).show(parentFragmentManager, SelectPartnerSchoolBottomSheet.javaClass.name)
        }
    }

    companion object {
        fun newInstance() = DocsSubSchedulerFragment()
    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
        }

        arguments?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.WORK_ORDER_ID.value, mWordOrderID)


    }
}