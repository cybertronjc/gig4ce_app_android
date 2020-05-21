package com.gigforce.app.core.base.basefirestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


abstract public class BaseFirestoreDBRepository {
    private var firebaseDB = FirebaseFirestore.getInstance()
    private var uid = FirebaseAuth.getInstance().currentUser?.uid!!

    abstract public fun getCollectionName(): String

    // for set DB data
    //set data object
    public fun setDefaultData(data: Any) {
        getDBCollection().set(data)
    }

    public fun <M : BaseFirestoreDataModel> setData(arrData: ArrayList<M>) {
        for (obj in arrData) {
            setData(obj)
        }
    }

    public fun <M : BaseFirestoreDataModel> setData(obj: M) {
        getDBCollection().update(obj.tableName, FieldValue.arrayUnion(obj))
    }

    // set data object end
    //set data string
    public fun setData(tableName: String, arrData: ArrayList<String>) {
        for (obj in arrData) {
            setData(tableName, obj)
        }
    }

    public fun setData(tableName: String, data: String) {
        getDBCollection().update(tableName, FieldValue.arrayUnion(data))
    }

    public fun setDataAndDeleteOldData(tableName: String, data: String) {
        getDBCollection().update(tableName, FieldValue.delete())
        setData(tableName, data)
    }

    public fun setDataAndDeleteOldData(tableName: String, arrData: ArrayList<String>) {
        getDBCollection().update(tableName, FieldValue.delete())
        for (obj in arrData) {
            setData(tableName, obj)
        }
    }
    public fun setDataAsKeyValue(tableName: String, data: String) {
        getDBCollection().update(tableName, data)
    }
    //set data string end
    // set data boolean
    public fun setData(tableName: String, data: Boolean) {
        getDBCollection().update(tableName, data)
    }
    //set data boolean end
    // for set DB data--------end

    //for remove data
    fun <M : BaseFirestoreDataModel> removeData(arrData: ArrayList<M>) {
        for (obj in arrData) {
            removeData(obj)
        }
    }

    fun <M : BaseFirestoreDataModel> removeData(obj: M) {
        getDBCollection().update(obj.tableName, FieldValue.arrayRemove(obj))
    }

    public fun removeData(tableName: String, arrData: ArrayList<String>) {
        for (obj in arrData) {
            removeData(tableName, obj)
        }
    }

    fun removeData(tableName: String, data: String) {
        getDBCollection().update(tableName, FieldValue.arrayRemove(data))
    }
    //for remove data------------------

    // for get collection data
    fun getDBCollection(): DocumentReference {
        return firebaseDB.collection(getCollectionName())
            .document(uid)
    }
    // for get collection data ----------------

}