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
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.verification.R
import com.gigforce.verification.util.VerificationEvents
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_confirm_bn_bank_bs.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@AndroidEntryPoint
class ConfirmBNBankBS : BottomSheetDialogFragment() {

    private val viewModel: BankAccountViewModel by viewModels()

    @Inject
    lateinit var eventTracker: IEventTracker

    @Inject
    lateinit var navigation: INavigation

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
        return inflater.inflate(R.layout.fragment_confirm_bn_bank_bs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (user == null) dismiss()
        else {
            isCancelable = false
            requestBankDetailData()
            observer()

            confirm_bn_bs.setOnClickListener {
                user?.let {
                    var props = HashMap<String, Any>()
                    props.put("Bank verified", true)
                    eventTracker.setUserProperty(props)
                    eventTracker.pushEvent(
                        TrackingEventArgs(
                            eventName = VerificationEvents.BANK_VERIFIED,
                            props = null
                        )
                    )
                    viewModel.setVerificationStatusInDB(true, it)
                }

            }

            cancel_button.setOnClickListener {
                user?.let {
                    eventTracker.pushEvent(
                        TrackingEventArgs(
                            eventName = VerificationEvents.BANK_MISMATCH,
                            props = null
                        )
                    )
                    viewModel.setVerificationStatusInDB(false, it)
                }

            }
        }

    }

    private fun requestBankDetailData() {
        user?.let {
            viewModel.getBankDetailsStatus(it)
        }
    }

    private fun observer() {
        viewModel.userConsentModel.observe(viewLifecycleOwner, Observer { userconsent ->
            when (userconsent) {
                is Lce.Loading -> {

                }
                is Lce.Content -> {
                    dismiss()
                    userconsent.content.optionSelected?.let {
                        if (it) {
                            navigation.navigateTo("verification/acknowledgeBankBS")

                        } else {
                            navigation.navigateTo("verification/bank_account_fragment")

                        }

                    }

                }
                is Lce.Error -> {
                    showToast(userconsent.error)
                }
            }
        })

        viewModel.bankDetailedObject.observe(viewLifecycleOwner, Observer {
            if (it.status.equals("verified") || it.bankBeneficiaryName == null) {
                dismiss()
            } else {
                it.bankBeneficiaryName.let { bnName ->
                    bn_tv.text = bnName
                    account_no_tv.text = it.accountNo
                    ifsc_tv.text = it.ifscCode
                }
            }

        })

    }
}