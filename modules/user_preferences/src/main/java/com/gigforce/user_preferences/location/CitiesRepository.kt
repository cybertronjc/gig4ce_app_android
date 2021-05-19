package com.gigforce.user_preferences.location

import android.util.Log
import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

class CitiesRepository : BaseFirestoreDBRepository() {
    var COLLECTION_NAME = "Cities"
    var firestoreDB = FirebaseFirestore.getInstance()

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    fun getCities(): ArrayList<String> {
        var list: ArrayList<String> = ArrayList<String>()
        firestoreDB.collection("root_collection").get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot>() {
                fun onComplete(task: Task<QuerySnapshot>) {
                    if (task.isSuccessful()) {
                        for (document: QueryDocumentSnapshot in task.result!!) {
                            list.add(document.getId());
                        }
                        Log.d("CITITES_REPO", list.toString());
                    } else {
                        Log.d("CITITES_REPO", "Error getting documents: ", task.exception);
                    }
                }
            })
        return list
    }

}