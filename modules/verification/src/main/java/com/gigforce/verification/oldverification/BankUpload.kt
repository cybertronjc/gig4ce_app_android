package com.gigforce.verification.oldverification

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.datamodels.verification.Bank
import com.gigforce.core.navigation.INavigation
import com.gigforce.verification.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.layout_verification_bank.view.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class BankUpload : Fragment() {
    companion object {
        fun newInstance() =
            BankUpload()
    }

    lateinit var viewModel: VerificationViewModel
    var layout: View? = null
    var updates: ArrayList<Bank> = ArrayList()

    lateinit var bankAcName: String
    lateinit var bankAcNo: String
    lateinit var bankAcNoRe: String
    lateinit var bankName: String
    lateinit var bankIfsc: String

    private val BANKAC =
        Pattern.compile("^\\d{9,18}\$")
    private val IFSC =
        Pattern.compile("^[A-Za-z]{4}0[A-Z0-9a-z]{6}\$")
    private val NAME_bk =
        Pattern.compile("^[\\\\p{L} .'-]+\$")
    private val NAME =
        Pattern.compile("^[A-Za-z ,.'-]+\$")

    lateinit var match: Matcher

    @Inject
    lateinit var navigation: INavigation

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.layout_verification_bank, container, false)
        layout?.pbBarBank?.setProgress(80, true)
        return layout
    }

    private fun validateFields(
        bankAcName: String,
        bankName: String,
        bankAcNo: String,
        bankAcNoRe: String,
        bankIfsc: String
    ): Boolean {
        //TODO Instead of toast msg we can put text msg on top of missing edit text or turn the edit text box to red!
        if (bankAcName.isEmpty()) {
            showToast(getString(R.string.enter_bank_ac_name_veri))
            return false
        } else {
            match = NAME.matcher(bankAcName)
            if (!match.matches()) {
                showToast(getString(R.string.enter_valid_bank_ac_name_veri))
                Log.d("Verification: ", bankAcName)
                return false
            }
        }
        if (bankName.isEmpty()) {
            showToast(getString(R.string.please_enter_bank_name_veri))
            return false
        } else {
            match = NAME.matcher(bankName)
            if (!match.matches()) {
                showToast(getString(R.string.enter_valid_bank_ac_name_veri))
                Log.d("Verification: ", bankName)
                return false
            }
        }

        if (bankAcNo.isEmpty()) {
            showToast(getString(R.string.enter_bank_account_no_veri))
            return false
        } else {
            match = BANKAC.matcher(bankAcNo)
            if (!match.matches()) {
                showToast(getString(R.string.enter_valid_bank_ac_no_veri))
                Log.d("Verification: ", bankAcNo)
                return false
            }
        }

        if (bankAcNoRe.isEmpty()) {
            showToast(getString(R.string.enter_bank_account_no_veri))
            return false
        } else {
            match = BANKAC.matcher(bankAcNoRe)
            if (!match.matches()) {
                showToast(getString(R.string.enter_valid_bank_ac_no_veri))
                Log.d("Verification: ", bankAcNoRe)
                return false
            }
        }

        if (bankIfsc.isEmpty()) {
            showToast(getString(R.string.enter_bank_ifsc_veri))
            return false
        } else {
            match = IFSC.matcher(bankIfsc)
            if (!match.matches()) {
                showToast(getString(R.string.enter_valid_bank_ifsc_veri))
                Log.d("Verification: ", bankIfsc)
                return false
            }
        }

        return true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)

        layout?.button_veri_bank_back?.setOnClickListener {
            //resetLayout() //CHECK
//            findNavController().navigate(R.id.uploadDropDown)
            navigation.navigateTo("verification/uploadDropDown")
        }

        layout?.button_veri_bank_save?.setOnClickListener {
            /*
            if fields on empty - validate fields (check for email regex and phone regex)
            and toast the missing fields before proceeding
             */
            bankAcName = layout?.add_veri_bank_acname?.text.toString()
            bankName = layout?.add_veri_bank_name?.text.toString()
            bankAcNo = layout?.add_veri_bank_acnum?.text.toString()
            bankAcNoRe = layout?.add_veri_bank_acnum_re?.text.toString()
            bankIfsc = layout?.add_veri_bank_ifsc?.text.toString()

            var areValid = validateFields(bankAcName, bankName, bankAcNo, bankAcNoRe, bankIfsc)

//                addNewBankAccount()
//                saveNewBanks()
//                resetLayout()
//                layout.pbBarBank.setProgress(60,true)
//                findNavController().navigate(R.id.panUpload)

            //if(!areValid)
            if (bankAcName.isEmpty() || bankName.isEmpty() || bankAcNo.isEmpty() || bankAcNoRe.isEmpty() || bankIfsc.isEmpty()) {
                showToast(getString(R.string.fill_up_missing_fields_veri))
            } else {
                addNewBankAccount()
                saveNewBanks()
                //resetLayout() //CHECK
                layout?.pbBarBank?.setProgress(60, true)
//                findNavController().navigate(R.id.panUpload)
                navigation.navigateTo("verification/panUpload")

            }
        }


    }

    private fun addNewBankAccount() {
        updates.add(
            Bank(
                bankAcName = bankAcName,
                bankName = bankName,
                bankAcNo = bankAcNo,
                bankIfsc = bankIfsc
            )
        )
    }

    private fun resetLayout() {
        layout?.add_veri_bank_acname?.setText("")
        layout?.add_veri_bank_name?.setText("")
        layout?.add_veri_bank_acnum?.setText("")
        layout?.add_veri_bank_ifsc?.setText("")
    }

    private fun saveNewBanks() {
        viewModel.setBank(updates)
    }
}