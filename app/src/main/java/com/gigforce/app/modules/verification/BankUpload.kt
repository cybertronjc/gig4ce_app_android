package com.gigforce.app.modules.verification

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.core.datamodels.verification.Bank
import kotlinx.android.synthetic.main.layout_verification_bank.view.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class BankUpload : BaseFragment() {
        companion object {
            fun newInstance() = BankUpload()
        }

        lateinit var viewModel: VerificationViewModel
        var layout: View? = null
        var updates: ArrayList<Bank> = ArrayList()

        lateinit var bankAcName:String;
        lateinit var bankAcNo:String;
        lateinit var bankAcNoRe:String;
        lateinit var bankName:String;
        lateinit var bankIfsc:String;

        private val BANKAC =
            Pattern.compile("^\\d{9,18}\$")
        private val IFSC =
            Pattern.compile("^[A-Za-z]{4}0[A-Z0-9a-z]{6}\$")
        private val NAME_bk =
            Pattern.compile("^[\\\\p{L} .'-]+\$")
        private val NAME =
            Pattern.compile("^[A-Za-z ,.'-]+\$")

        lateinit var match: Matcher;

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            layout = inflateView(R.layout.layout_verification_bank,inflater, container)
            layout?.pbBarBank?.setProgress(80,true)
            return layout
        }

    private fun validateFields(bankAcName:String, bankName:String, bankAcNo:String, bankAcNoRe:String, bankIfsc:String):Boolean {
        //TODO Instead of toast msg we can put text msg on top of missing edit text or turn the edit text box to red!
        if (bankAcName.isEmpty())
        {
            showToast("Please enter Bank Ac Name")
            return false
        }
        else{
            match = NAME.matcher(bankAcName);
            if(!match.matches()) {
                showToast("Please enter valid Bank Ac Name")
                Log.d("Verification: ", bankAcName)
                return false
            }
        }
        if(bankName.isEmpty())
        {
            showToast("Please enter Bank name")
            return false
        }
        else
        {
            match = NAME.matcher(bankName);
            if(!match.matches()) {
                showToast( "Please enter valid Bank Name")
                Log.d("Verification: ", bankName)
                return false
            }
        }

        if(bankAcNo.isEmpty())
        {
            showToast( "Please enter Bank Ac No")
            return false
        }
        else
        {
            match = BANKAC.matcher(bankAcNo);
            if(!match.matches()) {
                showToast("Please enter valid Bank Ac No")
                Log.d("Verification: ", bankAcNo)
                return false
            }
        }

        if(bankAcNoRe.isEmpty())
        {
            showToast("Please enter Bank Ac No")
            return false
        }
        else
        {
            match = BANKAC.matcher(bankAcNoRe);
            if(!match.matches()) {
                showToast("Please enter valid Bank Ac No")
                Log.d("Verification: ", bankAcNoRe)
                return false
            }
        }

        if(bankIfsc.isEmpty())
        {
            showToast("Please enter Bank IFSC")
            return false
        }
        else
        {
            match = IFSC.matcher(bankIfsc);
            if(!match.matches()) {
                showToast("Please enter valid Bank IFSC")
                Log.d("Verification: ", bankIfsc)
                return false
            }
        }

        return true;
    }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)

            layout?.button_veri_bank_back?.setOnClickListener {
                //resetLayout() //CHECK
                findNavController().navigate(R.id.uploadDropDown)
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

                var areValid = validateFields(bankAcName, bankName, bankAcNo, bankAcNoRe, bankIfsc);

//                addNewBankAccount()
//                saveNewBanks()
//                resetLayout()
//                layout.pbBarBank.setProgress(60,true)
//                findNavController().navigate(R.id.panUpload)

                //if(!areValid)
                if(bankAcName.isEmpty() || bankName.isEmpty() || bankAcNo.isEmpty() || bankAcNoRe.isEmpty() || bankIfsc.isEmpty())
                {
                        showToast("Please fill up all the missing fields")
                }
                else{
                    addNewBankAccount()
                    saveNewBanks()
                    //resetLayout() //CHECK
                    layout?.pbBarBank?.setProgress(60,true)
                    findNavController().navigate(R.id.panUpload)
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