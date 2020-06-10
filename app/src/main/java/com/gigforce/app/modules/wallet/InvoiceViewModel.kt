package com.gigforce.app.modules.wallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.wallet.models.Invoice

class InvoiceViewModel: ViewModel() {
    var pendingInvoices: MutableLiveData<ArrayList<Invoice>> = MutableLiveData(ArrayList())
    var generatedInvoice: MutableLiveData<ArrayList<Invoice>> = MutableLiveData(ArrayList())

    companion object {
        fun newInstance() = InvoiceViewModel()
    }

    init {
        pendingInvoices.value = ArrayList(
            listOf(Invoice(), Invoice(), Invoice())
        )

        generatedInvoice.value = ArrayList(
            listOf(Invoice(), Invoice(), Invoice(), Invoice())
        )
    }

}