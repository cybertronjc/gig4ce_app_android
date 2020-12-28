package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.verify_mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.gigforce.app.R
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.ambassador_user_enrollment.models.AmbassadorApplication
import com.gigforce.app.modules.ambassador_user_enrollment.models.AmbassadorEnrollmentProfile
import com.gigforce.app.utils.StringConstants
import kotlinx.android.synthetic.main.fragment_user_already_exist_dialog.*

interface UserAlreadyExistDialogFragmentActionListener {

    fun onOkayClicked()
}

class UserAlreadyExistDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "DeclineGigDialogFragment"

        fun launch(
            fragmentManager: FragmentManager,
            okayClickListener: UserAlreadyExistDialogFragmentActionListener, bundle: Bundle
        ) {
            val frag = UserAlreadyExistDialogFragment()
            frag.arguments = bundle
            frag.mOkayResultListener = okayClickListener
            frag.show(fragmentManager, TAG)
        }

    }

    private lateinit var mAmbObj: AmbassadorEnrollmentProfile

    private lateinit var mOkayResultListener: UserAlreadyExistDialogFragmentActionListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_already_exist_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        initView()
    }

    override fun isCancelable(): Boolean {
        return false
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {

            setBackgroundDrawableResource(R.drawable.dialog_round_bg)

            setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }


    private fun initView() {
        tv_user_already_exists_title.text = mAmbObj.alreadyExistsDialogTitle
        congrats_text.text = mAmbObj.alreadyExistsDialogContent
        submitBtn.text = mAmbObj.alreadyExistsDialogAction
        submitBtn.setOnClickListener {
            mOkayResultListener.onOkayClicked()
            dismiss()
        }
    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mAmbObj =
                it.getParcelable(StringConstants.AMB_APPLICATION_OBJ.value)
                    ?: AmbassadorEnrollmentProfile()


        }

        arguments?.let {
            mAmbObj =
                it.getParcelable(StringConstants.AMB_APPLICATION_OBJ.value)
                    ?: AmbassadorEnrollmentProfile()


        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(StringConstants.AMB_APPLICATION_OBJ.value, mAmbObj)


    }
}