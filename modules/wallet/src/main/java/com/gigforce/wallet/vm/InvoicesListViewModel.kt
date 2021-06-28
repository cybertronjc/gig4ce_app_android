package com.gigforce.wallet.vm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.wallet.models.InvoiceDataModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InvoicesListViewModel: ViewModel() {
    var allInvoices: MutableLiveData<ArrayList<InvoiceDataModel>> = MutableLiveData()

    companion object {
        fun newInstance() = InvoicesListViewModel()
    }

    init {
        getInvoices()
    }

    private fun getInvoices() {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val collection = "Invoices"

        db.collection(collection).whereEqualTo("userId", uid)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                var userInvoices = ArrayList<InvoiceDataModel>()
                if (querySnapshot != null) {
                    querySnapshot.documents.forEach { t ->
                        Log.d("invoice", t.toString())
                        t.toObject(InvoiceDataModel::class.java)?.let { userInvoices.add(it) }
                    }

                }
                else {
                    Log.d("invoice", firebaseFirestoreException.toString())
                }
                Log.d("userInvoices", userInvoices.toString())
                allInvoices.postValue(userInvoices)
            }
    }
}