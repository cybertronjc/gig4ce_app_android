package com.gigforce.app.modules.explore_by_role

import com.google.android.gms.tasks.Task


interface AddBioViewModelCallbacks {
    fun saveBio(bio: String, responseCallbacks: ResponseCallbacks)


    interface ResponseCallbacks {
        fun saveBioResponse(
            task: Task<Void>
        )

    }
}