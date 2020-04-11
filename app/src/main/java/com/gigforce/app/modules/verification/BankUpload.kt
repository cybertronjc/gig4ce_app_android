package com.gigforce.app.modules.verification

import android.os.Build
import android.os.Bundle
import android.util.Log
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
import kotlinx.android.synthetic.main.layout_verification_bank.view.*
import java.util.regex.Matcher
import java.util.regex.Pattern

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
            layout = inflater.inflate(R.layout.layout_verification_bank, container, false)
            layout.pbBarBank.setProgress(80,true)
            return layout
        }

    private fun validateFields(bankAcName:String, bankName:String, bankAcNo:String, bankAcNoRe:String, bankIfsc:String):Boolean {
        //TODO Instead of toast msg we can put text msg on top of missing edit text or turn the edit text box to red!
        if (bankAcName.isEmpty())
        {
            Toast.makeText(this.context, "Please enter Bank Ac Name", Toast.LENGTH_SHORT).show()
            return false
        }
        else{
            match = NAME.matcher(bankAcName);
            if(!match.matches()) {
                Toast.makeText(this.context, "Please enter valid Bank Ac Name", Toast.LENGTH_SHORT).show()
                Log.d("Verification: ", bankAcName)
                return false
            }
        }
        if(bankName.isEmpty())
        {
            Toast.makeText(this.context, "Please enter Bank name", Toast.LENGTH_SHORT).show()
            return false
        }
        else
        {
            match = NAME.matcher(bankName);
            if(!match.matches()) {
                Toast.makeText(this.context, "Please enter valid Bank Name", Toast.LENGTH_SHORT).show()
                Log.d("Verification: ", bankName)
                return false
            }
        }

        if(bankAcNo.isEmpty())
        {
            Toast.makeText(this.context, "Please enter Bank Ac No", Toast.LENGTH_SHORT).show()
            return false
        }
        else
        {
            match = BANKAC.matcher(bankAcNo);
            if(!match.matches()) {
                Toast.makeText(this.context, "Please enter valid Bank Ac No", Toast.LENGTH_SHORT).show()
                Log.d("Verification: ", bankAcNo)
                return false
            }
        }

        if(bankAcNoRe.isEmpty())
        {
            Toast.makeText(this.context, "Please enter Bank Ac No", Toast.LENGTH_SHORT).show()
            return false
        }
        else
        {
            match = BANKAC.matcher(bankAcNoRe);
            if(!match.matches()) {
                Toast.makeText(this.context, "Please enter valid Bank Ac No", Toast.LENGTH_SHORT).show()
                Log.d("Verification: ", bankAcNoRe)
                return false
            }
        }

        if(bankIfsc.isEmpty())
        {
            Toast.makeText(this.context, "Please enter Bank IFSC", Toast.LENGTH_SHORT).show()
            return false
        }
        else
        {
            match = IFSC.matcher(bankIfsc);
            if(!match.matches()) {
                Toast.makeText(this.context, "Please enter valid Bank IFSC", Toast.LENGTH_SHORT).show()
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

            layout.button_veri_bank_back.setOnClickListener {
                resetLayout()
                findNavController().navigate(R.id.uploadDropDown)
            }

            layout.button_veri_bank_save.setOnClickListener {
                /*
                if fields on empty - validate fields (check for email regex and phone regex)
                and toast the missing fields before proceeding
                 */
                bankAcName = layout.add_veri_bank_acname.text.toString()
                bankName = layout.add_veri_bank_name.text.toString()
                bankAcNo = layout.add_veri_bank_acnum.text.toString()
                bankAcNoRe = layout.add_veri_bank_acnum_re.text.toString()
                bankIfsc = layout.add_veri_bank_ifsc.text.toString()

                var areValid = validateFields(bankAcName, bankName, bankAcNo, bankAcNoRe, bankIfsc);

                //if(TextUtils.isEmpty(bankAcName) || TextUtils.isEmpty(bankName) || TextUtils.isEmpty(bankAcNo) || TextUtils.isEmpty(bankAcNoRe) || TextUtils.isEmpty(bankIfsc))
                if(!areValid)
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
                    layout.pbBarBank.setProgress(100,true)
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
            layout.add_veri_bank_acname.setText("")
            layout.add_veri_bank_name.setText("")
            layout.add_veri_bank_acnum.setText("")
            layout.add_veri_bank_ifsc.setText("")
        }

        private fun saveNewBanks() {
            viewModel.setBank(updates)
        }
}