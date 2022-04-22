package com.gigforce.lead_management.ui.DynamicFields

import android.content.Context
import android.text.SpannedString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.gigforce.common_ui.R
import com.gigforce.common_ui.dynamic_fields.DynamicFieldView
import com.gigforce.common_ui.dynamic_fields.DynamicScreenFieldView
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicScreenField
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.dynamic_fields.data.FieldTypes
import com.gigforce.common_ui.ext.addMandatorySymbolToTextEnd
import com.gigforce.common_ui.utils.ViewModelProviderFactory
import com.gigforce.common_ui.viewdatamodels.leadManagement.OtherCityClusterItem
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.lead_management.databinding.LayoutDynamicSelectOtherCitiesViewBinding
import com.gigforce.lead_management.ui.new_selection_form_2.NewSelectionForm2Events
import com.gigforce.lead_management.ui.new_selection_form_2.NewSelectionForm2ViewModel

class DynamicSelectOtherCitiesView(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
), DynamicScreenFieldView {
    private var viewBinding: LayoutDynamicSelectOtherCitiesViewBinding
    private lateinit var viewData: DynamicField
    private var editTextString: String = ""
    private var selectedOtherCities: List<OtherCityClusterItem>? = null


//    private val viewModel: NewSelectionForm2ViewModel  = ViewModelProviderFactory(NewSelectionForm2ViewModel)

    override val fieldType: String
        get() = FieldTypes.OTHER_CITIES

    override fun setError(error: SpannedString) {
        viewBinding.errorLayout.root.visible()
        viewBinding.errorLayout.errorTextview.text = error
    }

    override fun removeError() {
        viewBinding.errorLayout.errorTextview.text = null
        viewBinding.errorLayout.root.gone()
    }

    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = LayoutDynamicSelectOtherCitiesViewBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
        //viewBinding.root.setOnClickListener(this)
    }


    override fun bind(
        fieldDetails: DynamicField
    ) {
        viewData = fieldDetails
        tag = id //setting id of dynamic view as view tag to identify layout at runtime
        setTitle(fieldDetails.title)
        settingFieldAsOptionalOrMandatory(fieldDetails)
    }

    override fun setData(data: Any?) {
        data?.let {
            selectedOtherCities = data as List<OtherCityClusterItem>
            removeError()
        }
    }

    private fun setTitle(title: String?) {
        viewBinding.titleTextview.text = title
    }

    private fun settingFieldAsOptionalOrMandatory(fieldDetails: DynamicField) {
        if (fieldDetails.mandatory) {
            viewBinding.optionalTextview.gone()
            viewBinding.titleTextview.addMandatorySymbolToTextEnd()
        } else {
            viewBinding.optionalTextview.visible()
        }
    }

    override fun isEnteredOrSelectedDataValid(): Boolean {
        return if (viewData.mandatory) {
            selectedOtherCities != null && selectedOtherCities!!.isNotEmpty()
        } else {
            true
        }
    }

    override fun validateDataAndReturnDataElseNull(): DataFromDynamicScreenField? {
        return if (isEnteredOrSelectedDataValid()) {
            removeError()
            return DataFromDynamicScreenField(
                id = viewData.id,
                fieldType = fieldType,
                title = viewData.title,
                valueId = null,
                value = selectedOtherCities
            )
        } else {
            checkDataAndSetError()
            null
        }
    }

    private fun checkDataAndSetError() {
        if (viewData.mandatory) {

            if (!isEnteredOrSelectedDataValid()) {

                setError(buildSpannedString {
                    bold {
                        append(
                            resources.getString(R.string.common_note_with_colon)
                        )
                    }
                    append(" Please select ${viewData.title}")
                })
            } else {
                removeError()
            }
        }
    }

}