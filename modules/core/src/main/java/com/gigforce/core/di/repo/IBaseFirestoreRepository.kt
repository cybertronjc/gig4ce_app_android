package com.gigforce.core.di.repo

import com.google.firebase.firestore.DocumentReference

interface IBaseFirestoreRepository {
    fun getDBCollection(): DocumentReference
}