package com.gigforce.wallet.vm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.wallet.models.Invoice
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InvoiceViewModel : ViewModel() {
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

    fun getMonthlyInvoices(
        invoices: ArrayList<Invoice>?,
        month: Int,
        year: Int
    ): ArrayList<Invoice> {
        val result = ArrayList<Invoice>()
        invoices?.let {
            it.forEach { invoice ->
                if (invoice.month == month && invoice.year == year) {
                    result.add(invoice)
                }
            }
        }
        return result
    }

    fun getGeneratedInvoices(invoices: ArrayList<Invoice>): ArrayList<Invoice> {
        var result = ArrayList<Invoice>()
        invoices.forEach {
            if (it.isInvoiceGenerated) result.add(it)
        }
        return result
    }

    fun getPendingInvoices(invoices: ArrayList<Invoice>): ArrayList<Invoice> {
        var result = ArrayList<Invoice>()

        invoices.forEach { if (it.invoiceStatus == "pending") result.add(it) }

        return result
    }

    init {

        queryInvoices()
    }

}