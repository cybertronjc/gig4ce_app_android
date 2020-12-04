package com.gigforce.app.modules.client_activation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.client_activation.models.PartnerSchoolDetails
import com.gigforce.app.utils.HorizontaltemDecoration
import com.gigforce.app.utils.StringConstants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.layout_select_partner_bottom_sheet.*

class SelectPartnerSchoolBottomSheet : BottomSheetDialogFragment(), AdapterPartnerSchool.AdapterPartnerSchoolCallbacks {
    private lateinit var callbacks: SelectPartnerBsCallbacks
    private lateinit var mWordOrderID: String

    private lateinit var viewModel: SelectPartnerSchoolViewModel
    private val adapter = AdapterPartnerSchool()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_select_partner_bottom_sheet, container, false
        )
    }

    private fun setupRecyclerView() {
        rv_partner_school_address.adapter = adapter
        adapter.setCallbacks(this)
        rv_partner_school_address.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rv_partner_school_address.addItemDecoration(
                HorizontaltemDecoration(
                        requireContext(),
                        R.dimen.size_16
                )
        )

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        viewModel =
                ViewModelProvider(
                        this,
                        SavedStateViewModelFactory(requireActivity().application, this)
                ).get(SelectPartnerSchoolViewModel::class.java)
        setupRecyclerView()
        initObservers()
        initClicks()

    }

    private fun initClicks() {

        slider_okay_partner_school.onSlideCompleteListener =
                object : SlideToActView.OnSlideCompleteListener {

                    override fun onSlideComplete(view: SlideToActView) {
                        callbacks.setPartnerAddress(adapter.getSelectedItem())
                        this@SelectPartnerSchoolBottomSheet.dismiss()
                    }
                }
    }

    private fun initObservers() {
        viewModel.observablePartnerSchool.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            pb_select_partner_bottom_sheet.gone()
            rv_partner_school_address.visible()
            adapter.addData(it.addressList)

        })
        viewModel.observableError.observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), it ?: "", Toast.LENGTH_LONG).show()
            pb_select_partner_bottom_sheet.gone()

        })
        viewModel.getPartnerSchoolDetails("driving_certificate", mWordOrderID)
    }

    companion object {

        fun newInstance(bundle: Bundle): SelectPartnerSchoolBottomSheet {
            val selectPartnerSchoolBottomSheet = SelectPartnerSchoolBottomSheet()
            selectPartnerSchoolBottomSheet.arguments = bundle
            return selectPartnerSchoolBottomSheet
        }
    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
        }

        arguments?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
        }
    }

    fun setCallbacks(callbacks: SelectPartnerBsCallbacks) {
        this.callbacks = callbacks;
    }

    public interface SelectPartnerBsCallbacks {

        fun setPartnerAddress(address: PartnerSchoolDetails);
    }

    override fun onItemClick(position: Int) {
        slider_okay_partner_school.isLocked = position != -1
    }
}