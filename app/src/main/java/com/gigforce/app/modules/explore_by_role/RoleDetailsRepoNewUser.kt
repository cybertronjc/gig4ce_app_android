package com.gigforce.app.modules.explore_by_role

import android.annotation.SuppressLint
import com.google.firebase.firestore.FirebaseFirestore

class RoleDetailsRepoNewUser : RoleDetailsCallbacks {


    override fun getRoleDetails(
        id: String?,
        responseCallbacks: RoleDetailsCallbacks.ResponseCallbacks
    ) {
        FirebaseFirestore.getInstance().collection("Roles").document(id!!)
            .addSnapshotListener { success, error ->
                run {
                    responseCallbacks.getRoleDetailsResponse(success, error)
                }
            }
    }

    override fun markAsInterest(
        roleID: String?,
        responseCallbacks: RoleDetailsCallbacks.ResponseCallbacks
    ) {
//        db.collection("Profiles").document(getUID())
//            .update("role_interests", FieldValue.arrayUnion(RoleInterests(roleID)))
//            .addOnCompleteListener {
//                responseCallbacks.markedAsInterestSuccess(it)
//            }
    }

    @SuppressLint("CheckResult")
    override fun checkForProfileCompletionAndVerification(responseCallbacks: RoleDetailsCallbacks.ResponseCallbacks) {

//        Observable.fromIterable(
//            arrayListOf(
//                Completed("Profiles", ProfileData::class.java),
//                Completed("Verification", VerificationBaseModel::class.java)
//            )
//        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
//            .concatMap(fun(item: Completed<out Any>): Observable<out Any> {
//                return getCollectionObservable(item.collectionName, item.classToParse)
//            })
//            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
//
//            .subscribe { success ->
//                responseCallbacks.checkDataResponse(success)
//
//            };
    }

    override fun getUID(): String {
        return ""
    }


//    fun <T> getCollectionObservable(
//        collectionName: String,
//        clazz: Class<T>
//    ): Observable<T> {
//
//        return Observable.create { emitter ->
//            var listenerRegistration: ListenerRegistration? = null
//            listenerRegistration = db.collection(collectionName).document(getUID())
//                .addSnapshotListener(EventListener<DocumentSnapshot> { snapshot, e ->
//                    if (e != null) {
//                        listenerRegistration?.remove()
//                        emitter.onError(e)
//                        emitter.onComplete()
//                        return@EventListener
//                    }
//
//                    if (snapshot != null && snapshot.exists()) {
//                        val documentModel = snapshot.toObject(clazz)
//                        listenerRegistration?.remove()
//                        emitter.onNext(documentModel!!)
//                        emitter.onComplete()
//                    }
//                })
//
//        }
//
//    }
}