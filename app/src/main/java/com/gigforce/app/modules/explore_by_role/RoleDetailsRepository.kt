package com.gigforce.app.modules.explore_by_role

import android.annotation.SuppressLint
import android.util.Log
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.explore_by_role.models.Completed
import com.gigforce.app.modules.gigerVerfication.VerificationBaseModel
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.modules.profile.models.RoleInterests
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class RoleDetailsRepository : BaseFirestoreDBRepository(), RoleDetailsCallbacks {
    override fun getCollectionName(): String {
        return "Roles"
    }


    override fun getRoleDetails(
        id: String?,
        responseCallbacks: RoleDetailsCallbacks.ResponseCallbacks
    ) {
        getCollectionReference().document(id!!).addSnapshotListener { success, error ->
            run {
                responseCallbacks.getRoleDetailsResponse(success, error)
            }
        }
    }

    override fun markAsInterest(
        roleID: String?,
        responseCallbacks: RoleDetailsCallbacks.ResponseCallbacks
    ) {
        db.collection("Profiles").document(getUID())
            .update("role_interests", FieldValue.arrayUnion(RoleInterests(roleID)))
            .addOnCompleteListener {
                responseCallbacks.markedAsInterestSuccess(it)
            }
    }

    @SuppressLint("CheckResult")
    override fun checkForProfileCompletionAndVerification(responseCallbacks: RoleDetailsCallbacks.ResponseCallbacks) {

        Observable.fromIterable(
            arrayListOf(
                Completed("Profiles", ProfileData::class.java),
                Completed("Verification", VerificationBaseModel::class.java)
            )
        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .concatMap(fun(item: Completed<out Any>): Observable<out Any> {
                return getCollectionObservable(item.collectionName, item.classToParse)
            })
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

            .subscribe{success->
                responseCallbacks.checkDataResponse(success)

            };
    }


    fun <T> getCollectionObservable(
        collectionName: String,
        clazz: Class<T>
    ): Observable<T> {

        return Observable.create { emitter ->
            var listenerRegistration: ListenerRegistration? = null
            listenerRegistration = db.collection(collectionName).document(getUID())
                .addSnapshotListener(EventListener<DocumentSnapshot> { snapshot, e ->
                    if (e != null) {
                        listenerRegistration?.remove()
                        emitter.onError(e)
                        emitter.onComplete()
                        return@EventListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val documentModel = snapshot.toObject(clazz)
                        listenerRegistration?.remove()
                        emitter.onNext(documentModel!!)
                        emitter.onComplete()
                    }
                })

        }

    }
}