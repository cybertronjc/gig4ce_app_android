package com.gigforce.core.base.basefirestore

import com.gigforce.core.StringConstants
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


abstract class BaseFirestoreDBRepository {

    private var firebaseDB = FirebaseFirestore.getInstance()
    val db: FirebaseFirestore get() = firebaseDB
    private val user: FirebaseUser?
        get() {
            return FirebaseAuth.getInstance().currentUser
        }

    abstract fun getCollectionName(): String

    // for set DB data
    //set data object
    public fun setDefaultData(data: Any) {
        getDBCollection().set(data)
        setUpdatedOnUpdatedBy()
    }


    public fun <M : BaseFirestoreDataModel> setData(arrData: ArrayList<M>) {
        for (obj in arrData) {
            setData(obj)
        }
    }

    public fun <M : BaseFirestoreDataModel> setData(obj: M) {
        getDBCollection().update(obj.tableName, FieldValue.arrayUnion(obj))
        setUpdatedOnUpdatedBy()
    }

    public fun <M : BaseFirestoreDataModel> setDataAsKeyValue(obj: M) {
        getDBCollection().update(obj.tableName, obj)
        setUpdatedOnUpdatedBy()
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
        setUpdatedOnUpdatedBy()
    }

    public fun setDataAndDeleteOldData(tableName: String, data: String) {
        getDBCollection().update(tableName, FieldValue.delete())
        setData(tableName, data)
    }

    public fun <M : BaseFirestoreDataModel> setDataAndDeleteOldData(obj: M) {
        removeData(obj)
        setData(obj)
    }

    public fun setDataAndDeleteOldData(tableName: String, arrData: ArrayList<String>) {
        getDBCollection().update(tableName, FieldValue.delete())
        for (obj in arrData) {
            setData(tableName, obj)
        }
    }

    public fun setDataAsKeyValue(tableName: String, data: String) {
        getDBCollection().update(tableName, data)
        setUpdatedOnUpdatedBy()
    }

    //set data string end
    // set data boolean
    public fun setData(tableName: String, data: Boolean) {
        getDBCollection().update(tableName, data)
        setUpdatedOnUpdatedBy()
    }

    private fun setUpdatedOnUpdatedBy(){
        getDBCollection().update("updatedOn", Timestamp.now())
        getDBCollection().update("updatedBy", StringConstants.APP.value)
    }

    public fun setDataAsKeyValue(tableName: String, data: Boolean) {
        getDBCollection().update(tableName, data)
        setUpdatedOnUpdatedBy()
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
        setUpdatedOnUpdatedBy()
    }

    public fun removeData(tableName: String, arrData: ArrayList<String>) {
        for (obj in arrData) {
            removeData(tableName, obj)
        }
    }

    fun removeData(tableName: String, data: String) {
        getDBCollection().update(tableName, FieldValue.arrayRemove(data))
        setUpdatedOnUpdatedBy()
    }
    //for remove data------------------

    // for get collection data
    fun getCollectionReference(): CollectionReference {
        return firebaseDB.collection(getCollectionName())
    }

    fun getUID(): String {
        return user!!.uid
    }

    fun getDBCollection(): DocumentReference {
        return firebaseDB.collection(getCollectionName())
            .document(getUID())
    }

    // for get collection data ----------------

    open fun getCustomUid(): String? {
        return ""
    }

    fun getCustomDBCollection(): DocumentReference {
        return firebaseDB.collection(getCollectionName())
            .document(getCustomUid()!!)
    }
}