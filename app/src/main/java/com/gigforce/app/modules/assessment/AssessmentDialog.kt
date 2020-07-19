package com.gigforce.app.modules.assessment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.gigforce.app.R
import com.gigforce.app.utils.getScreenWidth


/**
 * @author Rohit Sharma
 * date 19/7/2020
 */
class AssessmentDialog : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_assessment_dialog, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window
            ?.setLayout(
                (getScreenWidth(requireActivity()).width - resources.getDimension(R.dimen.size_32)).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        dialog?.window
            ?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));


    }


    companion object {
        fun newInstance(): AssessmentDialog {
            return AssessmentDialog()
        }

        const val STATE_INIT = 1;
    }


}