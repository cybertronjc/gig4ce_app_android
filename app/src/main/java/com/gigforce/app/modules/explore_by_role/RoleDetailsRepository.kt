package com.gigforce.app.modules.explore_by_role

import android.annotation.SuppressLint
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.gigforce.app.modules.explore_by_role.models.Completed
import com.gigforce.core.StringConstants
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.datamodels.profile.RoleInterests
import com.google.firebase.Timestamp
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
        val map = mapOf("role_interests" to FieldValue.arrayUnion(RoleInterests(roleID)), "updatedAt" to Timestamp.now(), "updatedBy" to getUID())
        db.collection("Profiles").document(getUID())
            .update(map)
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

                    if (snapshot != null ) {
                        val documentModel = snapshot.toObject(clazz)
                        listenerRegistration?.remove()
                        emitter.onNext(documentModel?:clazz.newInstance())
                        emitter.onComplete()
                    }
                })

        }

    }
}