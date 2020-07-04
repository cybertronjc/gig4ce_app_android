package com.gigforce.app.modules.wallet

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.wallet.models.Invoice
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InvoiceViewModel: ViewModel() {
    var pendingInvoices: MutableLiveData<ArrayList<Invoice>> = MutableLiveData(ArrayList())
    var generatedInvoice: MutableLiveData<ArrayList<Invoice>> = MutableLiveData(ArrayList())

    var monthlyInvoice: ArrayList<Invoice> = ArrayList()

    var allInvoices: MutableLiveData<ArrayList<Invoice>> = MutableLiveData()
    //lateinit var allInvoices: ArrayList<Invoice>

    companion object {
        fun newInstance() = InvoiceViewModel()
    }

    private fun queryInvoices() {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val collection = "Transactions"

        db.collection(collection).whereEqualTo("gigerId", uid)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                var userInvoices = ArrayList<Invoice>()
                if (querySnapshot != null) {
//                    gigsQuery.postValue(querySnapshot.documents.forEach { t -> t.data })
                    //Log.d("RosterViewModel", querySnapshot.documentstoString())
                    querySnapshot.documents.forEach { t ->
                        Log.d("RosterViewModel", t.toString())
                        t.toObject(Invoice::class.java)?.let { userInvoices.add(it) }
                    }

                }
                allInvoices.postValue(userInvoices)
            }
    }

    fun getPaymentDueAmount(invoices: ArrayList<Invoice>): Int {
        var paymentDue = 0
        for (invoice in invoices) {
            if (invoice.invoiceStatus == "rejected") {
                paymentDue += invoice.gigAmount
            }
        }
        return paymentDue
    }

    fun getDisputedInvoices(invoices: ArrayList<Invoice>): ArrayList<Invoice> {
        var result = ArrayList<Invoice>()

        invoices.forEach {
            if (it.invoiceStatus == "rejected") result.add(it)
        }

        return result
    }

    fun getMonthlyInvoices(invoices: ArrayList<Invoice>?, month: Int, year: Int): ArrayList<Invoice> {
        val result = ArrayList<Invoice>()
        invoices?.let {
            it.forEach {invoice ->
                if (invoice.month == month && invoice.year == year) {
                    result.add(invoice)
                }
            }
        }
        return result
    }
    init {
//        generatedInvoice.value = ArrayList(
//            listOf(
//                Invoice(
//                    gigId = "GFP1234",
//                    agentName = "P&G Retail",
//                    date = 16,
//                    month = 6,
//                    year = 2020,
//                    gigTiming = "16 June, 10:00",
//                    gigAmount = 1200,
//                    invoiceStatus = "processed",
//                    isInvoiceGenerated = true,
//                    invoiceGeneratedTime = "16 June, 17:00"
//                ),
//                Invoice(
//                    gigId = "GFG1230",
//                    agentName = "P&G Retail",
//                    date = 15,
//                    month = 6,
//                    year = 2020,
//                    gigTiming = "15 June, 10:00",
//                    gigAmount = 1200,
//                    invoiceStatus = "processed",
//                    isInvoiceGenerated = true,
//                    invoiceGeneratedTime = "16 June, 19:00"
//                ),
//                Invoice (
//                    gigId = "GPG1201",
//                    agentName = "P&G Retail",
//                    date = 14,
//                    month = 6,
//                    year = 2020,
//                    gigTiming = "14 June, 14:00",
//                    gigAmount = 400,
//                    invoiceStatus = "processed",
//                    isInvoiceGenerated = true,
//                    invoiceGeneratedTime = "16 June, 19:00"
//                ),
//                Invoice(
//                    gigId = "GPG1220",
//                    agentName = "P&G Retail",
//                    date = 11,
//                    month = 6,
//                    year = 2020,
//                    gigTiming = "11 June, 10:00",
//                    gigAmount = 1200,
//                    invoiceStatus = "processed",
//                    isInvoiceGenerated = true,
//                    invoiceGeneratedTime = "13 June, 17:00"
//                )
//            )
//        )
//        pendingInvoices.value = ArrayList(
//            listOf(Invoice(), Invoice(), Invoice())
//        )
//
////        generatedInvoice.value = ArrayList(
////            listOf(Invoice(), Invoice(), Invoice(), Invoice())
////        )
//
//        monthlyInvoice = generatedInvoice.value!!
//
//        allInvoices = ArrayList(
//            listOf(
//                Invoice(
//                    date = 12,
//                    month = 6,
//                    year = 2020
//                ),
//                Invoice(
//                    date = 1,
//                    month = 6,
//                    year = 2020
//                ),
//                Invoice(
//                    date = 15,
//                    month = 5,
//                    year = 2020
//                ),
//                Invoice(
//                    date = 1,
//                    month = 5,
//                    year = 2020
//                ),
//                Invoice(
//                    date = 5,
//                    month = 3,
//                    year = 2020
//                ),
//                Invoice(
//                    date = 2,
//                    month = 1,
//                    year = 2020
//                ),
//                Invoice(
//                    date = 12,
//                    month = 11,
//                    year = 2019
//                ),
//                Invoice(
//                    date = 13,
//                    month = 6,
//                    year = 2019
//                ),
//                Invoice(
//                    date = 15,
//                    month = 3,
//                    year = 2018
//                )
//            )
//        )

        queryInvoices()
    }

}