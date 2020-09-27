package com.gigforce.app.modules.explore_by_role

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.landingscreen.models.Role
import com.gigforce.app.utils.SingleLiveEvent
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class ExploreByRoleViewModel(private val callbacks: ExploreByRoleCallbacks) : ViewModel(),
    ExploreByRoleCallbacks.ResponseCallbacks {
    private val _observerRoleList: SingleLiveEvent<ArrayList<Role>> by lazy {
        SingleLiveEvent<ArrayList<Role>>();
    }
    val observerRoleList: SingleLiveEvent<ArrayList<Role>> get() = _observerRoleList

    private val _observerError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observerError: SingleLiveEvent<String> get() = _observerError

    fun getRoles() {
        callbacks.getRoles(this)
    }

    override fun getRolesResponse(
        querySnapshot: QuerySnapshot?,
        error: FirebaseFirestoreException?
    ) {
        if (error != null) {
            observerError.value = error.message
            return
        }
        val roles = querySnapshot?.toObjects(Role::class.java)
        querySnapshot?.documents?.forEachIndexed { index, element ->
            roles?.get(index)?.id = element.id
        }
        val arrayList = ArrayList<Role>()
        arrayList.addAll(roles!!)
        observerRoleList.value = arrayList
    }


}