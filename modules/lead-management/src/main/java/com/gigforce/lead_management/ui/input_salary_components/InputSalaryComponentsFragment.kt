package com.gigforce.lead_management.ui.input_salary_components

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.core.view.forEachIndexed
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.components.cells.SearchTextChangeListener
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewdatamodels.leadManagement.InputSalaryResponse
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentInputSalaryComponentsBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.gigforce.lead_management.ui.input_salary_components.views.InputSalaryComponentView
import com.gigforce.lead_management.ui.new_selection_form_2.NewSelectionForm2Fragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InputSalaryComponentsFragment : BaseFragment2<FragmentInputSalaryComponentsBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_input_salary_components,
    statusBarColor = R.color.lipstick_2
) {
    companion object {
        private const val TAG = "InputSalaryComponentsFragment"
        const val INTENT_EXTRA_BUSINESS_ID = "business_id"
        const val INTENT_EXTRA_SALARY_DATA = "salary_data"
    }

    @Inject
    lateinit var navigation: INavigation
    private val sharedViewModel: LeadManagementSharedViewModel by activityViewModels()
    private var businessId: String? = null
    var totalAmount = 0
    private var enteredSalaryData: InputSalaryResponse? = null
    private val viewModel: InputSalaryViewModel by viewModels()

    override fun viewCreated(
        viewBinding: FragmentInputSalaryComponentsBinding,
        savedInstanceState: Bundle?
    ) {
        getDataFrom(
            arguments,
            savedInstanceState
        )
        initViewModel()
        initListeners()

    }

    private fun initListeners() = viewBinding.apply{
        toolbar.apply {
            titleText.text = getString(R.string.input_salary_component_common_ui)
            setBackButtonListener {
                navigation.popBackStack()
            }
            setBackButtonDrawable(R.drawable.ic_chevron)
        }
        okayButton.isEnabled = true
        okayButton.setOnClickListener {

        salaryComponentsLayout.forEachIndexed { index, view ->
            val editText = view.findViewById<EditText>(R.id.edit_text)
            val amountValue = editText.text.toString()
            if (amountValue.isEmpty()){
                totalAmount += 0
            } else {
                totalAmount += editText.text.toString().toInt()
            }
        }
        Log.d("TotalAmount", "$totalAmount")
        if (totalAmount != 0){
            salaryComponentsLayout.forEachIndexed { index, view ->
                val editText = view.findViewById<EditText>(R.id.edit_text)
                val amountValue = editText.text.toString()
                if (amountValue.isEmpty()){
                    enteredSalaryData?.data?.get(index)?.amount = 0
                } else {
                    enteredSalaryData?.data?.get(index)?.amount = editText.text.toString().toInt()
                }
            }
            enteredSalaryData?.let { it1 -> sharedViewModel.salaryAmountEntered(it1) }
            navigation.popBackStack(
                LeadManagementNavDestinations.FRAGMENT_SELECTION_FORM_2,
                false
            )
        } else {
            showToast("Enter at least one salary input")
        }
        }
    }

    private fun getDataFrom(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            businessId = it.getString(INTENT_EXTRA_BUSINESS_ID) ?: return@let
            enteredSalaryData = it.getParcelable(INTENT_EXTRA_SALARY_DATA) ?: return@let
        }

        savedInstanceState?.let {
            businessId = it.getString(INTENT_EXTRA_BUSINESS_ID) ?: return@let
            enteredSalaryData = it.getParcelable(INTENT_EXTRA_SALARY_DATA) ?: return@let
        }
    }

    private fun initViewModel() {
        enteredSalaryData?.let { showSalaryComponents(it) }
            ?: viewModel.getSalaryComponents(businessId.toString(), "salary")


        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            val state = it ?: return@Observer
            when(state) {
                Lce.Loading -> showDataLoading()

                is Lce.Content -> showSalaryComponents(state.content)

                is Lce.Error -> showError(state.error)
            }
        })
    }

    private fun showError(error: String) = viewBinding.apply{
        this.infoLayout.root.visible()
        this.infoLayout.infoMessageTv.text = error
        this.infoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
    }

    private fun showSalaryComponents(salaryResponse: InputSalaryResponse) = viewBinding.apply{
        if (salaryResponse.data?.isEmpty() == true) {
            this.infoLayout.root.visible()
            this.infoLayout.infoMessageTv.text = getString(R.string.no_salary_components_lead)
            this.infoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        } else {
            this.infoLayout.root.gone()
            enteredSalaryData = salaryResponse
            //add views into linear layout
            salaryComponentsLayout.removeAllViewsInLayout()
            salaryResponse.data?.forEachIndexed { index, inputSalaryDataItem ->
                val view = InputSalaryComponentView(requireContext(), null)
                view.showData(inputSalaryDataItem)
//                view.amountTextChangeListener = object : InputSalaryComponentView.AmountTextChangeListener {
//                    override fun onAmountTextChanged(text: String) {
//                        if (text.isNotEmpty()){
//                            totalAmount
//                        } else {
//
//                        }
//                    }
//
//                }
                salaryComponentsLayout.addView(view)
            }
        }
    }

    private fun showDataLoading() {

    }


}