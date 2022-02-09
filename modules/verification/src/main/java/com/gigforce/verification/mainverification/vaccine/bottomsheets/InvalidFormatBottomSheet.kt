package com.gigforce.verification.mainverification.vaccine.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.verification.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.size_warning_bs.*
import javax.inject.Inject

class InvalidFormatBottomSheet : BottomSheetDialogFragment() {
    @Inject
    lateinit var navigation: INavigation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BSDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.invalid_fomat_bs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIntentData()
        cancel_doc_button.setOnClickListener {
            dismiss()
        }

        try_again_bn_bs.setOnClickListener {
            (activity as NavFragmentsData).setData(
                bundleOf(
                    "vaccine_doc" to "try_again",
                    "vaccineId" to vaccineId,
                    "vaccineLabel" to vaccineLabel
                )
            )
            dismiss()
            navigation.popBackStack("verification/VaccineMainFragment", true)
            navigation.navigateTo("verification/VaccineMainFragment")
        }
    }

    var vaccineId = ""
    var vaccineLabel = ""

    private fun getIntentData() {
        arguments?.let {
            vaccineId = it.getString("vaccineId") ?: ""
            vaccineLabel = it.getString("vaccineLabel") ?: ""
        }
    }
}