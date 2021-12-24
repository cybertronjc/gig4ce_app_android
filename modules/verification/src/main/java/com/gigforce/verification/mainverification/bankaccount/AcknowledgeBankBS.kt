package com.gigforce.verification.mainverification.bankaccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.utils.GlideApp
import com.gigforce.core.utils.Lce
import com.gigforce.verification.R
import com.gigforce.verification.util.VerificationEvents
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_acknowledge_bank_b_s.*
import javax.inject.Inject
import javax.inject.Singleton


@AndroidEntryPoint
class AcknowledgeBankBS : BottomSheetDialogFragment() {

    private val viewModel: BankAccountViewModel by viewModels()

    @Inject
    lateinit var eventTracker: IEventTracker

    private val user: String?
        get() {
            return FirebaseAuth.getInstance().currentUser?.uid
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BSDialogStyle)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_acknowledge_bank_b_s, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (user == null) dismiss()
        else {
            isCancelable = false
            initializeAll()
            requestBankDetailData()
            observer()

            okay_bn_bs.setOnClickListener {
                user?.let {
                    eventTracker.pushEvent(
                        TrackingEventArgs(
                            eventName = VerificationEvents.BANK_MISMATCH,
                            props = null
                        )
                    )
                    viewModel.setUserAknowledge(user.toString())
                }

            }

        }

    }

    private fun initializeAll() {
        context?.let {
            GlideApp.with(it).load(R.drawable.green_tik_gif).into(greenTickImage)

        }
    }

    private fun requestBankDetailData() {
        user?.let {
            viewModel.getBankDetailsStatus(it)
        }
    }

    private fun observer() {
        viewModel.userConsentModel.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Lce.Loading -> {

                }
                is Lce.Content -> {
                    dismiss()
                }
                is Lce.Error -> {
                    showToast(it.error)
                }
            }
        })

        viewModel.bankDetailedObject.observe(viewLifecycleOwner, Observer {
            if (it.bankBeneficiaryName == null) {
                dismiss()
            } else {
                it.bankBeneficiaryName?.let { bnName ->
                    bn_tv.text = bnName
                    account_no_tv.text = it.accountNo
                    ifsc_tv.text = it.ifscCode
                }
            }

        })

    }
}