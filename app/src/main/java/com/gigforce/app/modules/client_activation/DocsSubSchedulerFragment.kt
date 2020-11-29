package com.gigforce.app.modules.client_activation

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.client_activation.models.PartnerSchoolDetails
import com.gigforce.app.modules.preferences.SharedPreferenceViewModel
import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel
import com.gigforce.app.utils.StringConstants
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.*


class DocsSubSchedulerFragment : BaseFragment(), SelectPartnerSchoolBottomSheet.SelectPartnerBsCallbacks, TimeSlotsDialog.TimeSlotDialogCallbacks {

    private var partnerAddress: PartnerSchoolDetails? = null
    private lateinit var mWordOrderID: String
    private lateinit var viewModel: SharedPreferenceViewModel
    private lateinit var viewDataModel: PreferencesDataModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        return inflateView(R.layout.fragment_docs_sub_scheduler, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SharedPreferenceViewModel::class.java)
        viewDataModel = viewModel.getPreferenceDataModel()
        getDataFromIntents(savedInstanceState)
        view7.setOnClickListener {
            val newInstance = SelectPartnerSchoolBottomSheet.newInstance(bundleOf(
                    StringConstants.WORK_ORDER_ID.value to mWordOrderID
            ))
            newInstance.setCallbacks(this)
            newInstance.show(parentFragmentManager, SelectPartnerSchoolBottomSheet.javaClass.name)
        }
        initViews()
        initClicks()
    }

    private fun initClicks() {
        view_select_time_slots.setOnClickListener {
            val newInstance = TimeSlotsDialog.newInstance()
            newInstance
            newInstance.setCallbacks(this)
            newInstance.show(parentFragmentManager, TimeSlotsDialog::class.java.name)
        }

    }

    private fun initViews() {
        if (partnerAddress != null) {
            setPartnerAddress(partnerAddress!!)
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

    override fun setPartnerAddress(address: PartnerSchoolDetails) {
        this.partnerAddress = address;
        textView137.text = Html.fromHtml(address.schoolName + "<br>" + address.landmark + "<br>" + address.city + "<br>"
                + address.schoolTiming + "<br>" + address.contact.map { "<b>" + it.name + "</b>" }.reduce { a, o -> a + o }
        )
        imageView34.gone()
        iv_contact.visible()
        iv_location.visible()


    }

    override fun setSelectedTimeSlot(time: String) {
        textView143.text = time
    }


}