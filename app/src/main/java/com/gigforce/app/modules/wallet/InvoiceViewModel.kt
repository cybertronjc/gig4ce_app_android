package com.gigforce.app.modules.wallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.wallet.models.Invoice

class InvoiceViewModel: ViewModel() {
    var pendingInvoices: MutableLiveData<ArrayList<Invoice>> = MutableLiveData(ArrayList())
    var generatedInvoice: MutableLiveData<ArrayList<Invoice>> = MutableLiveData(ArrayList())

    var monthlyInvoice: ArrayList<Invoice> = ArrayList()

    lateinit var allInvoices: ArrayList<Invoice>

    companion object {
        fun newInstance() = InvoiceViewModel()
    }

    init {
        generatedInvoice.value = ArrayList(
            listOf(
                Invoice(
                    gigId = "GFP1234",
                    agentName = "P&G Retail",
                    date = 16,
                    month = 6,
                    year = 2020,
                    gigTiming = "16 June, 10:00",
                    gigAmount = 1200,
                    invoiceStatus = "processed",
                    isInvoiceGenerated = true,
                    invoiceGeneratedTime = "16 June, 17:00"
                ),
                Invoice(
                    gigId = "GFG1230",
                    agentName = "P&G",
                    date = 15,
                    month = 6,
                    year = 2020,
                    gigTiming = "15 June, 10:00",
                    gigAmount = 1200,
                    invoiceStatus = "processed",
                    isInvoiceGenerated = true,
                    invoiceGeneratedTime = "16 June, 19:00"
                ),
                Invoice (
                    gigId = "GPG1201",
                    agentName = "P&G",
                    date = 14,
                    month = 6,
                    year = 2020,
                    gigTiming = "14 June, 14:00",
                    gigAmount = 400,
                    invoiceStatus = "processed",
                    isInvoiceGenerated = true,
                    invoiceGeneratedTime = "16 June, 19:00"
                ),
                Invoice(
                    gigId = "GPG1220",
                    agentName = "P&G",
                    date = 11,
                    month = 6,
                    year = 2020,
                    gigTiming = "11 June, 10:00",
                    gigAmount = 1200,
                    invoiceStatus = "processed",
                    isInvoiceGenerated = true,
                    invoiceGeneratedTime = "13 June, 17:00"
                )
            )
        )
        pendingInvoices.value = ArrayList(
            listOf(Invoice(), Invoice(), Invoice())
        )

//        generatedInvoice.value = ArrayList(
//            listOf(Invoice(), Invoice(), Invoice(), Invoice())
//        )

        monthlyInvoice = generatedInvoice.value!!

        allInvoices = ArrayList(
            listOf(
                Invoice(
                    date = 12,
                    month = 6,
                    year = 2020
                ),
                Invoice(
                    date = 1,
                    month = 6,
                    year = 2020
                ),
                Invoice(
                    date = 15,
                    month = 5,
                    year = 2020
                ),
                Invoice(
                    date = 1,
                    month = 5,
                    year = 2020
                ),
                Invoice(
                    date = 5,
                    month = 3,
                    year = 2020
                ),
                Invoice(
                    date = 2,
                    month = 1,
                    year = 2020
                ),
                Invoice(
                    date = 12,
                    month = 11,
                    year = 2019
                ),
                Invoice(
                    date = 13,
                    month = 6,
                    year = 2019
                ),
                Invoice(
                    date = 15,
                    month = 3,
                    year = 2018
                )
            )
        )
    }

}