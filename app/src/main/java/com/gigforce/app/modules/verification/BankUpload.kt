package com.gigforce.app.modules.verification

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.verification.models.Bank
import kotlinx.android.synthetic.main.layout_verification.view.*
import kotlinx.android.synthetic.main.layout_verification_bank.view.*

class BankUpload : Fragment() {
        companion object {
            fun newInstance() = BankUpload()
        }

        lateinit var viewModel: VerificationViewModel
        lateinit var layout: View
        var updates: ArrayList<Bank> = ArrayList()

        lateinit var bankAcName:String;
        lateinit var bankAcNo:String;
        lateinit var bankAcNoRe:String;
        lateinit var bankName:String;
        lateinit var bankIfsc:String;

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            layout = inflater.inflate(R.layout.layout_verification_bank, container, false)
            layout.pbBarBank.setProgress(80,true)
            return layout
        }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)

            bankAcName = layout.add_veri_bank_acname.text.toString()
            bankName = layout.add_veri_bank_name.text.toString()
            bankAcNo = layout.add_veri_bank_acnum.text.toString()
            bankAcNoRe = layout.add_veri_bank_acnum_re.text.toString()
            bankIfsc = layout.add_veri_bank_ifsc.text.toString()

            layout.button_veri_bank_cancel.setOnClickListener {
                resetLayout()
                findNavController().navigate(R.id.DLUpload)
            }

            layout.button_veri_bank_save.setOnClickListener {
                /*
                if fields on empty - validate fields (check for email regex and phone regex)
                and toast the missing fields before proceeding
                 */

                if(TextUtils.isEmpty(bankAcName) || TextUtils.isEmpty(bankName) || TextUtils.isEmpty(bankAcNo) || TextUtils.isEmpty(bankAcNoRe) || TextUtils.isEmpty(bankIfsc))
                {
                    Toast.makeText(
                        this.context,
                        "Please fill up all the missing fields",
                        Toast.LENGTH_LONG).show()
                }
                else{
                    addNewBankAccount()
                    saveNewBanks()
                    resetLayout()
                    layout.pbAddress.setProgress(100,true)
                    findNavController().navigate(R.id.verificationDone)
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
            layout.add_veri_bank_acname.setText("")
            layout.add_veri_bank_name.setText("")
            layout.add_veri_bank_acnum.setText("")
            layout.add_veri_bank_ifsc.setText("")
        }

        private fun saveNewBanks() {
            viewModel.setBank(updates)
        }
}