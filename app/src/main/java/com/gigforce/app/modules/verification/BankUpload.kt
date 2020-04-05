package com.gigforce.app.modules.verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.verification.models.Bank
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_verification.view.*
import kotlinx.android.synthetic.main.layout_verification_bank.view.*

class BankUpload : BottomSheetDialogFragment() {
        companion object {
            fun newInstance() = BankUpload()
        }

        lateinit var viewModel: VerificationViewModel
        lateinit var layout: View
        var updates: ArrayList<Bank> = ArrayList()

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            layout = inflater.inflate(R.layout.layout_verification_bank, container, false)
            return layout
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)

            layout.button_veri_address_cancel.setOnClickListener {
                findNavController().navigate(R.id.homeScreenIcons);
            }

            layout.button_veri_bank_save.setOnClickListener {
                /*
                if fields on empty - validate fields (check for email regex and phone regex)
                and toast the missing fields before proceeding
                 */
                if(layout.add_veri_bank_acname.equals("") || layout.add_veri_bank_acnum.equals("") || layout.add_veri_bank_acnum_re.equals("") || layout.add_veri_bank_ifsc.equals("") || layout.add_veri_bank_name.equals(""))
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
                    findNavController().navigate(R.id.panUpload)
                }
            }

            layout.button_veri_bank_cancel.setOnClickListener{
                findNavController().navigate(R.id.verification)
            }
        }

        private fun addNewBankAccount() {
            updates.add(
                Bank(
                    bankAcName = layout.add_veri_bank_acname.text.toString(),
                    bankName = layout.add_veri_bank_name.text.toString(),
                    bankAcNo = layout.add_veri_bank_acnum.text.toString(),
                    bankIfsc = layout.add_veri_bank_ifsc.text.toString()
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